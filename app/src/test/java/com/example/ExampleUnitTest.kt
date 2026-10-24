package com.example

import com.example.ui.PageCategory
import com.example.ui.PdfPageNumberDetector
import com.example.ui.fromRoman
import com.example.ui.generatePageMapping
import com.example.ui.toRoman
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun testRomanNumerals() {
        assertEquals("i", toRoman(1))
        assertEquals("ii", toRoman(2))
        assertEquals("iii", toRoman(3))
        assertEquals("iv", toRoman(4))
        assertEquals("v", toRoman(5))
        assertEquals("vi", toRoman(6))
        assertEquals("ix", toRoman(9))
        assertEquals("x", toRoman(10))
        assertEquals("xvi", toRoman(16))

        assertEquals(1, fromRoman("i"))
        assertEquals(2, fromRoman("ii"))
        assertEquals(6, fromRoman("vi"))
        assertEquals(16, fromRoman("xvi"))
    }

    @Test
    fun testInformatikaPdfAutoDetection() {
        val file = File("app/src/main/assets/books/informatika_7.pdf").takeIf { it.exists() }
            ?: File("src/main/assets/books/informatika_7.pdf")
        assertTrue("Informatika PDF file should exist", file.exists())
        val result = PdfPageNumberDetector.detect(file, "Informatika SMP/MTs", 288)
        // Informatika has 16 preliminary pages (Cover + ii..xvi), sheet 17 (index 16) is Halaman 1
        assertEquals(16, result.firstArabicIndex)
        val mapping = generatePageMapping(288, result.firstArabicIndex)
        assertEquals(PageCategory.COVER, mapping[0].category)
        assertEquals("ii", mapping[1].displayLabel)
        assertEquals("xvi", mapping[15].displayLabel)
        assertEquals(PageCategory.ARABIC, mapping[16].category)
        assertEquals(1, mapping[16].arabicNumber)
        assertEquals("1", mapping[16].displayLabel)
    }

    @Test
    fun testBahasaArabPdfAutoDetection() {
        val file = File("app/src/main/assets/books/bahasa_arab_7.pdf").takeIf { it.exists() }
            ?: File("src/main/assets/books/bahasa_arab_7.pdf")
        assertTrue("Bahasa Arab PDF file should exist", file.exists())
        val result = PdfPageNumberDetector.detect(file, "Bahasa Arab MTs", 151)
        // Bahasa Arab has 6 preliminary pages (Cover + ii..vi), sheet 7 (index 6) is Halaman 1
        assertEquals(6, result.firstArabicIndex)
        val mapping = generatePageMapping(151, result.firstArabicIndex)
        assertEquals(PageCategory.COVER, mapping[0].category)
        assertEquals("ii", mapping[1].displayLabel)
        assertEquals("vi", mapping[5].displayLabel)
        assertEquals(PageCategory.ARABIC, mapping[6].category)
        assertEquals(1, mapping[6].arabicNumber)
        assertEquals("1", mapping[6].displayLabel)
    }

    @Test
    fun testFallbacks() {
        val dummyFile = File("non_existent.pdf")
        val resultInfo = PdfPageNumberDetector.detect(dummyFile, "Buku Pelajaran Informatika", 100)
        assertEquals(16, resultInfo.firstArabicIndex)

        val resultArab = PdfPageNumberDetector.detect(dummyFile, "Pelajaran Bahasa Arab", 100)
        assertEquals(6, resultArab.firstArabicIndex)
    }
}
