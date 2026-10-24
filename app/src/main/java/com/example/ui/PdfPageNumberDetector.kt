package com.example.ui

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.util.zip.Inflater

/**
 * Intelligent Engine to detect the boundary between Roman numeral preliminary pages
 * (Cover, Editorial, Kata Pengantar, Daftar Isi: i, ii, iii...) and Arabic numeral main body
 * chapters (Halaman 1, 2, 3...) in educational textbooks.
 */
object PdfPageNumberDetector {
    private const val TAG = "PdfPageNumberDetector"

    data class DetectionResult(
        val firstArabicIndex: Int, // 0-indexed physical sheet where Arabic page 1 begins
        val method: String,        // "PDF_PAGE_LABELS", "ROMAN_STREAM_ANALYSIS", "CURRICULUM_CATALOG", "DEFAULT"
        val description: String
    )

    private fun logI(tag: String, msg: String) {
        try { Log.i(tag, msg) } catch (_: Throwable) { println("[$tag] $msg") }
    }
    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) { println("[$tag] $msg") }
    }
    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try { Log.e(tag, msg, tr) } catch (_: Throwable) { println("[$tag] $msg: $tr") }
    }

    fun detectFirstArabicIndex(file: File, bookTitle: String = "", pageCount: Int = 0): Int {
        return detect(file, bookTitle, pageCount).firstArabicIndex
    }

    fun detect(file: File, bookTitle: String = "", pageCount: Int = 0): DetectionResult {
        if (!file.exists() || file.length() < 100L) {
            return fallback(bookTitle, pageCount, "File tidak ditemukan atau kosong")
        }

        try {
            // 1. Primary Strategy: Check ISO 32000 PDF /PageLabels tree
            val pageLabelsResult = detectFromPageLabels(file, pageCount)
            if (pageLabelsResult != null) {
                logI(TAG, "Deteksi cerdas via PDF PageLabels: index=${pageLabelsResult.firstArabicIndex}")
                return pageLabelsResult
            }

            // 2. Secondary Strategy: Sequential Roman numeral stream analysis in preliminary sheets
            val streamResult = detectFromStreams(file, pageCount)
            if (streamResult != null) {
                logI(TAG, "Deteksi cerdas via Roman Stream Analysis: index=${streamResult.firstArabicIndex}")
                return streamResult
            }
        } catch (e: Exception) {
            logE(TAG, "Error saat menganalisis struktur internal PDF: ${e.message}", e)
        }

        // 3. Tertiary Strategy: Curriculum catalog & Title Heuristics
        return fallback(bookTitle, pageCount, "Heuristik kurikulum mata pelajaran")
    }

    /**
     * Inspects the PDF file for /PageLabels dictionary and /Nums tree.
     * In standard Indonesian government school books (Kemendikbud/Kemenag),
     * /PageLabels defines /S /r (lowercase Roman) and /S /D (Decimal Arabic).
     */
    private fun detectFromPageLabels(file: File, pageCount: Int): DetectionResult? {
        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(file, "r")
            val length = raf.length()

            // Read the tail (last 512 KB) where trailer and xref often reside
            val tailSize = minOf(length, 512 * 1024L).toInt()
            val tailBytes = ByteArray(tailSize)
            raf.seek(length - tailSize)
            raf.readFully(tailBytes)
            val tailStr = String(tailBytes, Charsets.ISO_8859_1)

            // Look for /PageLabels reference (e.g. /PageLabels 2400 0 R or inline)
            val plRegex = Regex("""/PageLabels\s+(\d+)\s+0\s+R""")
            val plMatch = plRegex.find(tailStr)

            val numsPattern = Regex("""/Nums\s*\[\s*([^\]]+)\]""")
            var numsContent: String? = null

            if (plMatch != null) {
                val objId = plMatch.groupValues[1]
                val objBody = findObjectBody(raf, objId)
                if (objBody != null) {
                    val numsMatch = numsPattern.find(objBody)
                    if (numsMatch != null) {
                        numsContent = numsMatch.groupValues[1]
                    }
                }
            } else {
                val inlineMatch = numsPattern.find(tailStr)
                if (inlineMatch != null) {
                    numsContent = inlineMatch.groupValues[1]
                }
            }

            if (numsContent == null) {
                // Also search first 500 KB just in case linearized
                val headSize = minOf(length, 500 * 1024L).toInt()
                val headBytes = ByteArray(headSize)
                raf.seek(0)
                raf.readFully(headBytes)
                val headStr = String(headBytes, Charsets.ISO_8859_1)
                val headMatch = numsPattern.find(headStr)
                if (headMatch != null) {
                    numsContent = headMatch.groupValues[1]
                }
            }

            if (numsContent != null) {
                val tokens = numsContent.trim().split(Regex("""\s+"""))
                for (i in tokens.indices) {
                    val token = tokens[i]
                    val idxVal = token.toIntOrNull()
                    if (idxVal != null && idxVal > 0) {
                        // Check if following token points to an obj containing /S /D
                        if (i + 3 < tokens.size && tokens[i + 2] == "0" && tokens[i + 3] == "R") {
                            val refId = tokens[i + 1]
                            val refBody = findObjectBody(raf, refId)
                            if (refBody != null && refBody.contains("/S") && (refBody.contains("/D") || refBody.contains("/d"))) {
                                val sheetNum = idxVal + 1
                                return DetectionResult(
                                    firstArabicIndex = idxVal,
                                    method = "PDF_PAGE_LABELS",
                                    description = "Terdeteksi otomatis via metadata PDF /PageLabels: Lembar ke-$sheetNum adalah Halaman 1 ($idxVal halaman romawi/sampul)"
                                )
                            }
                        } else if (i + 1 < tokens.size) {
                            val next = tokens[i + 1]
                            if (next.contains("/D") || next.contains("/d")) {
                                val sheetNum = idxVal + 1
                                return DetectionResult(
                                    firstArabicIndex = idxVal,
                                    method = "PDF_PAGE_LABELS",
                                    description = "Terdeteksi otomatis via PDF /PageLabels inline: Lembar ke-$sheetNum adalah Halaman 1"
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logD(TAG, "PageLabels check skipped: ${e.message}")
        } finally {
            try { raf?.close() } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Inspects text content streams of the preliminary pages (first 25 sheets)
     * looking for progressive sequences of Roman numerals (ii, iii, iv, v, vi, vii...).
     * When the Roman sequence concludes, the following sheet is Chapter 1 (Halaman 1).
     */
    private fun detectFromStreams(file: File, pageCount: Int): DetectionResult? {
        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(file, "r")
            val length = raf.length()
            val scanLimit = minOf(length, 3 * 1024 * 1024L).toInt() // inspect up to first 3 MB
            val buffer = ByteArray(scanLimit)
            raf.seek(0)
            raf.readFully(buffer)
            val contentStr = String(buffer, Charsets.ISO_8859_1)

            // Locate /Pages object
            val pagesMatch = Regex("""/Pages\s+(\d+)\s+0\s+R""").find(contentStr) ?: return null
            val pagesObjId = pagesMatch.groupValues[1]
            val pagesBody = findObjectBody(raf, pagesObjId) ?: return null

            // Extract kid page references
            val kidRefs = Regex("""(\d+)\s+0\s+R""").findAll(pagesBody).map { it.groupValues[1] }.toList()
            if (kidRefs.isEmpty()) return null

            val romanSequence = listOf(
                "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x",
                "xi", "xii", "xiii", "xiv", "xv", "xvi", "xvii", "xviii", "xix", "xx"
            )

            var expectedRomanIdx = 0 // looking for "ii" on sheet 2 (index 1)
            var lastMatchedSheet = -1

            val maxCheckPages = minOf(25, kidRefs.size)
            for (pageIdx in 1 until maxCheckPages) {
                val kidId = kidRefs[pageIdx]
                val pageObj = findObjectBody(raf, kidId) ?: continue

                // Find stream contents referenced in this page
                val contentRefs = Regex("""(\d+)\s+0\s+R""").findAll(pageObj).map { it.groupValues[1] }.toList()
                var pageMatched = false

                for (cRef in contentRefs) {
                    val decompressed = extractAndDecompressStream(raf, cRef) ?: continue
                    val textTokens = extractTextTokens(decompressed)
                    val expectedRoman = romanSequence.getOrNull(expectedRomanIdx)
                    if (expectedRoman != null && textTokens.contains(expectedRoman)) {
                        lastMatchedSheet = pageIdx + 1
                        expectedRomanIdx++
                        pageMatched = true
                        break
                    }
                }

                if (!pageMatched) {
                    // If we previously matched at least 2 Roman pages (e.g. ii, iii, iv...),
                    // the sequence has successfully ended here!
                    if (expectedRomanIdx >= 2) {
                        break
                    }
                }
            }

            if (lastMatchedSheet > 1) {
                val arabicSheet = lastMatchedSheet + 1
                val firstArabicIdx = lastMatchedSheet
                return DetectionResult(
                    firstArabicIndex = firstArabicIdx,
                    method = "ROMAN_STREAM_ANALYSIS",
                    description = "Terdeteksi cerdas melalui analisis teks romawi (i s/d ${romanSequence[expectedRomanIdx - 1]}): Lembar ke-$arabicSheet adalah Halaman 1"
                )
            }
        } catch (e: Exception) {
            logD(TAG, "Stream detection skipped: ${e.message}")
        } finally {
            try { raf?.close() } catch (_: Exception) {}
        }
        return null
    }

    private fun findObjectBody(raf: RandomAccessFile, objId: String): String? {
        val pattern = "\n$objId 0 obj".toByteArray(Charsets.ISO_8859_1)
        val patternAlt = "\r$objId 0 obj".toByteArray(Charsets.ISO_8859_1)

        val pos = findBytes(raf, pattern) ?: findBytes(raf, patternAlt) ?: return null
        raf.seek(pos)
        val readLen = 4096
        val buf = ByteArray(readLen)
        val bytesRead = raf.read(buf)
        if (bytesRead <= 0) return null
        val str = String(buf, 0, bytesRead, Charsets.ISO_8859_1)
        val endPos = str.indexOf("endobj")
        return if (endPos != -1) str.substring(0, endPos) else str
    }

    private fun extractAndDecompressStream(raf: RandomAccessFile, objId: String): ByteArray? {
        val pattern = "\n$objId 0 obj".toByteArray(Charsets.ISO_8859_1)
        val patternAlt = "\r$objId 0 obj".toByteArray(Charsets.ISO_8859_1)
        val pos = findBytes(raf, pattern) ?: findBytes(raf, patternAlt) ?: return null

        raf.seek(pos)
        val headerBuf = ByteArray(512)
        val read = raf.read(headerBuf)
        if (read <= 0) return null
        val headerStr = String(headerBuf, 0, read, Charsets.ISO_8859_1)
        val streamIdx = headerStr.indexOf("stream")
        if (streamIdx == -1) return null

        val streamStartPos = pos + streamIdx + 6 + (if (headerStr.getOrNull(streamIdx + 6) == '\r' && headerStr.getOrNull(streamIdx + 7) == '\n') 2 else 1)
        raf.seek(streamStartPos)

        val streamChunk = ByteArray(64 * 1024)
        val chunkRead = raf.read(streamChunk)
        if (chunkRead <= 0) return null

        return try {
            val inflater = Inflater()
            inflater.setInput(streamChunk, 0, chunkRead)
            val output = ByteArray(64 * 1024)
            val resultLength = inflater.inflate(output)
            inflater.end()
            output.copyOf(resultLength)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractTextTokens(streamBytes: ByteArray): Set<String> {
        val text = String(streamBytes, Charsets.ISO_8859_1)
        val tokens = mutableSetOf<String>()
        val regex = Regex("""\(([^)]+)\)""")
        regex.findAll(text).forEach { m ->
            val clean = m.groupValues[1].trim().lowercase()
            if (clean.isNotEmpty()) {
                tokens.add(clean)
            }
        }
        return tokens
    }

    private fun findBytes(raf: RandomAccessFile, target: ByteArray): Long? {
        val fileLength = raf.length()
        val buffer = ByteArray(32 * 1024)
        var pos = 0L

        while (pos < fileLength) {
            raf.seek(pos)
            val bytesRead = raf.read(buffer)
            if (bytesRead <= 0) break

            for (i in 0..(bytesRead - target.size)) {
                var found = true
                for (j in target.indices) {
                    if (buffer[i + j] != target[j]) {
                        found = false
                        break
                    }
                }
                if (found) {
                    return pos + i
                }
            }
            pos += bytesRead - target.size
        }
        return null
    }

    private fun fallback(bookTitle: String, pageCount: Int, reason: String): DetectionResult {
        val lower = bookTitle.lowercase()
        return when {
            lower.contains("informatika") -> {
                DetectionResult(
                    firstArabicIndex = 16,
                    method = "CURRICULUM_CATALOG",
                    description = "Berdasarkan format kurikulum Buku Informatika SMP/MTs: 16 halaman pendahuluan (Sampul & Romawi i..xvi), Halaman 1 pada Lembar ke-17"
                )
            }
            lower.contains("bahasa arab") || lower.contains("arab") -> {
                DetectionResult(
                    firstArabicIndex = 6,
                    method = "CURRICULUM_CATALOG",
                    description = "Berdasarkan format kurikulum Buku Bahasa Arab MTs Kemenag: 6 halaman pendahuluan (Sampul & Romawi i..vi), Halaman 1 pada Lembar ke-7"
                )
            }
            pageCount > 6 -> {
                DetectionResult(
                    firstArabicIndex = 4,
                    method = "DEFAULT",
                    description = "Format standar buku umum: 4 lembar pengantar (Sampul & Romawi i..iii), Halaman 1 pada Lembar ke-5 ($reason)"
                )
            }
            else -> {
                DetectionResult(
                    firstArabicIndex = 0,
                    method = "DEFAULT",
                    description = "Buku ringkas/tanpa romawi: Halaman 1 dimulai langsung dari Lembar ke-1"
                )
            }
        }
    }
}
