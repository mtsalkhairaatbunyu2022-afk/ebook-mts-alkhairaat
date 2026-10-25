package com.example.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPageReader(
    file: File,
    bookId: String,
    bookTitle: String,
    bookGradeClass: String = "Kelas VII",
    userId: String,
    isDarkMode: Boolean,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as? Activity

    var isLandscapeMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLandscapeMode) {
        activity?.requestedOrientation = if (isLandscapeMode) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Configure PdfRenderer safety with self-healing auto-regeneration
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(file, bookId) {
        withContext(Dispatchers.IO) {
            var workingFile = file
            if (!workingFile.exists() || workingFile.length() < 100) {
                try {
                    workingFile = viewModel.generateSamplePdf(bookId)
                } catch (e: Exception) {
                    Log.e("PdfPageReader", "Failed to generate sample PDF: ${e.message}")
                }
            }

            var success = false
            for (attempt in 1..2) {
                try {
                    if (workingFile.exists() && workingFile.length() > 100) {
                        var isPdf = false
                        try {
                            workingFile.inputStream().use { stream ->
                                val header = ByteArray(5)
                                val read = stream.read(header)
                                if (read >= 4) {
                                    val text = String(header, 0, read, Charsets.US_ASCII)
                                    if (text.startsWith("%PDF")) isPdf = true
                                }
                            }
                        } catch (_: Exception) {}

                        if (!isPdf) {
                            workingFile.delete()
                            workingFile = viewModel.generateSamplePdf(bookId)
                        }

                        val pfd = ParcelFileDescriptor.open(workingFile, ParcelFileDescriptor.MODE_READ_ONLY)
                        val r = PdfRenderer(pfd)
                        withContext(Dispatchers.Main) {
                            renderer = r
                        }
                        success = true
                        break
                    }
                } catch (e: Exception) {
                    Log.e("PdfPageReader", "Attempt $attempt error opening PDF: ${e.message}")
                    try {
                        workingFile.delete()
                        workingFile = viewModel.generateSamplePdf(bookId)
                    } catch (_: Exception) {}
                }
            }
            if (!success) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Gagal memuat dokumen PDF. Berkas tidak valid."
                }
            }
        }
    }

    val currentRenderer = renderer
    if (currentRenderer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                if (errorMessage == null) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memuat Dokumen PDF...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gagal Membuka Berkas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(errorMessage ?: "File PDF rusak atau tidak kompatibel.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack) {
                        Text("Kembali")
                    }
                }
            }
        }
        return
    }

    val pageCount = currentRenderer.pageCount
    val listState = rememberLazyListState()

    // Listen to bookmarks and saved position
    val bookmarks by viewModel.getBookmarksForBook(bookId, userId).collectAsState(initial = emptyList())
    val savedPosition by viewModel.getReadingPositionLive(bookId, userId).collectAsState(initial = null)

    var initializedPage by remember { mutableStateOf(false) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Page numbering and Roman vs Arabic mapping with intelligent auto-detection and SharedPreferences persistence
    val prefs = remember(context) { context.getSharedPreferences("book_page_prefs", android.content.Context.MODE_PRIVATE) }
    val autoDetection = remember(file, bookTitle, pageCount) {
        PdfPageNumberDetector.detect(file, bookTitle, pageCount)
    }

    var firstArabicIndex by remember(bookId) {
        val saved = prefs.getInt("first_arabic_$bookId", -1)
        val initialVal = if (saved in 0 until pageCount) {
            saved
        } else {
            val autoVal = autoDetection.firstArabicIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            prefs.edit().putInt("first_arabic_$bookId", autoVal).apply()
            autoVal
        }
        mutableIntStateOf(initialVal)
    }

    val pageList = remember(pageCount, firstArabicIndex) {
        generatePageMapping(pageCount, firstArabicIndex)
    }

    val currentPageInfo = pageList.getOrNull(currentPageIndex) ?: BookPageInfo(
        physicalIndex = currentPageIndex,
        category = PageCategory.ARABIC,
        displayLabel = (currentPageIndex + 1).toString(),
        shortTag = "Hal.",
        fullTitle = "Halaman ${currentPageIndex + 1}",
        arabicNumber = currentPageIndex + 1
    )

    var showJumpDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Semua, 1: Romawi, 2: Angka Biasa
    val bottomRowState = rememberLazyListState()

    // Auto-scroll the bottom LazyRow to follow reading position
    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex in 0 until pageCount) {
            bottomRowState.animateScrollToItem((currentPageIndex - 2).coerceAtLeast(0))
        }
    }

    // On start, jump to last read page
    LaunchedEffect(savedPosition) {
        if (!initializedPage && savedPosition != null && currentRenderer.pageCount > 0) {
            val pageToScroll = savedPosition!!.lastReadPage
            if (pageToScroll in 0 until pageCount) {
                listState.scrollToItem(pageToScroll)
                currentPageIndex = pageToScroll
            }
            initializedPage = true
        }
    }

    // Monitor current visible page indices reactively
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex in 0 until pageCount) {
            currentPageIndex = listState.firstVisibleItemIndex
            // Debounce database writing during scrolling to prevent UI lagging and state updates while dragging/sweeping pages
            kotlinx.coroutines.delay(500)
            viewModel.saveLastReadPage(bookId, userId, currentPageIndex, pageCount)
        }
    }

    var showNoteDialog by remember { mutableStateOf(false) }

    // Invert matrix colors filter for high contrast comfortable dark reading
    val pdfColorFilter = if (isDarkMode) {
        ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            -1.0f,  0.0f,  0.0f,  0.0f, 255.0f,
             0.0f, -1.0f,  0.0f,  0.0f, 255.0f,
             0.0f,  0.0f, -1.0f,  0.0f, 255.0f,
             0.0f,  0.0f,  0.0f,  1.0f,   0.0f
        )))
    } else null

    Scaffold(
        floatingActionButton = {
            if (!isLandscapeMode) {
                FloatingActionButton(
                    onClick = { showNoteDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = "Buat Catatan")
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = bookTitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentPageInfo.category) {
                                PageCategory.COVER -> "Sampul Depan • Lembar 1 dari $pageCount"
                                PageCategory.ROMAN -> "Hal. Romawi ${currentPageInfo.displayLabel.uppercase()} (${currentPageInfo.fullTitle.substringBefore(" (")}) • Lembar ${currentPageIndex + 1}/$pageCount"
                                PageCategory.ARABIC -> "Halaman ${currentPageInfo.arabicNumber} • Lembar ${currentPageIndex + 1} dari $pageCount"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_reader")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Search & Jump page button
                    IconButton(
                        onClick = { showJumpDialog = true },
                        modifier = Modifier.testTag("top_search_page")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Halaman",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Mode Baca (Lanskap / Potret) Toggle
                    IconButton(
                        onClick = {
                            isLandscapeMode = !isLandscapeMode
                            activity?.requestedOrientation = if (isLandscapeMode) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            Toast.makeText(
                                context,
                                if (isLandscapeMode) "Mode Baca: Lanskap" else "Mode Baca: Potret",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("mode_baca_toggle")
                    ) {
                        Icon(
                            imageVector = if (isLandscapeMode) Icons.Default.CropLandscape else Icons.Default.CropPortrait,
                            contentDescription = "Mode Baca",
                            tint = if (isLandscapeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isLandscapeMode) {
                        // Zoom Out Button (-)
                        IconButton(
                            onClick = {
                                scale = (scale - 0.25f).coerceAtLeast(1f)
                                if (scale == 1f) offset = Offset.Zero
                            },
                            enabled = scale > 1f,
                            modifier = Modifier.testTag("zoom_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Perkecil (Zoom Out)"
                            )
                        }

                        // Zoom In Button (+)
                        IconButton(
                            onClick = {
                                scale = (scale + 0.25f).coerceAtMost(4f)
                            },
                            enabled = scale < 4f,
                            modifier = Modifier.testTag("zoom_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Perbesar (Zoom In)"
                            )
                        }
                    }

                    // Bookmark toggle
                    val isCurrentBookmarked = bookmarks.any { it.pageNumber == currentPageIndex }
                    IconButton(
                        onClick = {
                            viewModel.toggleBookmark(
                                bookId = bookId,
                                userId = userId,
                                page = currentPageIndex,
                                title = "Penanda ${currentPageInfo.fullTitle}",
                                isNowBookmarked = isCurrentBookmarked
                            )
                        },
                        modifier = Modifier.testTag("bookmark_reader")
                    ) {
                        Icon(
                            imageVector = if (isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Simpan Penanda",
                            tint = if (isCurrentBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reset zoom level button
                    if (scale != 1f || offset != Offset.Zero) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF121212) else MaterialTheme.colorScheme.background
    ) { padding ->

        // Zoomable Canvas Box
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clipToBounds()
        ) {
            val density = LocalDensity.current
            val containerWidthPx = with(density) { maxWidth.toPx() }
            val containerHeightPx = with(density) { maxHeight.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            scale = newScale
                            if (newScale == 1f) {
                                offset = Offset.Zero
                            } else {
                                val maxOffsetX = (containerWidthPx * (newScale - 1f)) / 2f
                                val maxOffsetY = (containerHeightPx * (newScale - 1f)) / 2f
                                val newX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                val newY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                offset = Offset(newX, newY)
                            }
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                            clip = false
                        ),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pageCount) { pageIndex ->
                        PdfPageItem(
                            renderer = currentRenderer,
                            pageIndex = pageIndex,
                            colorFilter = pdfColorFilter,
                            isLandscapeMode = isLandscapeMode
                        )
                    }
                }
            }

            if (!isLandscapeMode) {
                // Floating Navigation Bar for Zoom In/Out & Mode Baca
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Mode Baca Toggle Button
                        TextButton(
                            onClick = {
                                isLandscapeMode = !isLandscapeMode
                                activity?.requestedOrientation = if (isLandscapeMode) {
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                                Toast.makeText(
                                    context,
                                    if (isLandscapeMode) "Mode Baca: Lanskap" else "Mode Baca: Potret",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isLandscapeMode) Icons.Default.CropLandscape else Icons.Default.CropPortrait,
                                contentDescription = "Mode Baca",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLandscapeMode) "Lanskap" else "Potret",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier
                                .height(18.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )

                        // Zoom Out (-) Button
                        IconButton(
                            onClick = {
                                scale = (scale - 0.25f).coerceAtLeast(1f)
                                if (scale == 1f) offset = Offset.Zero
                            },
                            enabled = scale > 1f,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Perkecil (Zoom Out)",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Zoom Level %
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Zoom In (+) Button
                        IconButton(
                            onClick = {
                                scale = (scale + 0.25f).coerceAtMost(4f)
                            },
                            enabled = scale < 4f,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Perbesar (Zoom In)",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (scale != 1f || offset != Offset.Zero) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    contentDescription = "Reset Zoom",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJumpDialog) {
        JumpToPageDialog(
            pageCount = pageCount,
            firstArabicIndex = firstArabicIndex,
            currentPageIndex = currentPageIndex,
            pageList = pageList,
            onDismiss = { showJumpDialog = false },
            onJumpToPage = { targetIndex ->
                coroutineScope.launch {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        )
    }

    if (showCalibrationDialog) {
        PageCalibrationDialog(
            currentFirstArabicIndex = firstArabicIndex,
            currentPageIndex = currentPageIndex,
            pageCount = pageCount,
            autoDetection = autoDetection,
            onDismiss = { showCalibrationDialog = false },
            onSaveOffset = { newOffset ->
                prefs.edit().putInt("first_arabic_$bookId", newOffset).apply()
                firstArabicIndex = newOffset
                Toast.makeText(
                    context,
                    "Kalibrasi berhasil disimpan: Halaman 1 diatur pada lembar ke-${newOffset + 1}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onResetToAuto = {
                val autoIdx = autoDetection.firstArabicIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                prefs.edit().putInt("first_arabic_$bookId", autoIdx).apply()
                firstArabicIndex = autoIdx
                Toast.makeText(
                    context,
                    "Deteksi otomatis diterapkan: Halaman 1 pada lembar ke-${autoIdx + 1}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showNoteDialog) {
        var noteDraft by remember { mutableStateOf("") }
        var selectedClass by remember {
            mutableStateOf(if (bookGradeClass.startsWith("Kelas")) bookGradeClass else "Kelas VII")
        }
        var expandedClassDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Catatan ${currentPageInfo.fullTitle}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Untuk Kelas") },
                            trailingIcon = {
                                IconButton(onClick = { expandedClassDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Kelas")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedClassDropdown = true }
                        )
                        DropdownMenu(
                            expanded = expandedClassDropdown,
                            onDismissRequest = { expandedClassDropdown = false }
                        ) {
                            listOf("Kelas VII", "Kelas VIII", "Kelas IX").forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        selectedClass = grade
                                        expandedClassDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        placeholder = { Text("Tulis poin penting diskusi di sini...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteDraft.isNotBlank()) {
                            viewModel.insertNote(
                                userId = userId,
                                bookId = bookId,
                                bookTitle = bookTitle,
                                pageNumber = currentPageIndex + 1,
                                noteContent = noteDraft,
                                gradeClass = selectedClass
                            )
                        }
                        showNoteDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Suppress("DEPRECATION")
@Composable
fun PdfPageItem(
    renderer: PdfRenderer,
    pageIndex: Int,
    colorFilter: ColorFilter?,
    isLandscapeMode: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val isScreenLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // When landscape mode is toggled, if screen is still portrait, rotate bitmap 90 degrees for landscape reading
    val shouldRotateBitmap = isLandscapeMode && !isScreenLandscape

    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var renderError by remember { mutableStateOf(false) }
    var pageAspectRatio by remember { mutableFloatStateOf(0.707f) }

    LaunchedEffect(pageIndex, isLandscapeMode, isScreenLandscape) {
        withContext(Dispatchers.IO) {
            try {
                var rawWidth: Int
                var rawHeight: Int
                val unrotatedBmp: Bitmap

                synchronized(renderer) {
                    val page = renderer.openPage(pageIndex)
                    rawWidth = page.width
                    rawHeight = page.height
                    val scaleFactor = if (isLandscapeMode) 2.5f else 2.2f
                    val bmpWidth = (rawWidth * scaleFactor).toInt().coerceAtLeast(1)
                    val bmpHeight = (rawHeight * scaleFactor).toInt().coerceAtLeast(1)

                    val bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    unrotatedBmp = bmp
                }

                val naturalRatio = rawWidth.toFloat() / rawHeight.toFloat()
                pageAspectRatio = if (shouldRotateBitmap) {
                    rawHeight.toFloat() / rawWidth.toFloat()
                } else {
                    naturalRatio
                }

                if (shouldRotateBitmap) {
                    val matrix = Matrix().apply {
                        postRotate(90f)
                    }
                    val rotatedBmp = Bitmap.createBitmap(
                        unrotatedBmp, 0, 0, unrotatedBmp.width, unrotatedBmp.height, matrix, true
                    )
                    unrotatedBmp.recycle()
                    bitmapState = rotatedBmp
                } else {
                    bitmapState = unrotatedBmp
                }
            } catch (e: Exception) {
                Log.e("PdfPageItem", "Failed mapping bitmap index: $pageIndex, ${e.message}")
                renderError = true
            }
        }
    }

    Card(
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(pageAspectRatio)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmapState != null) {
                Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = "Halaman ${pageIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.High,
                    colorFilter = colorFilter
                )
            } else if (renderError) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    Text("Kesalahan Membaca Halaman", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Memuat Halaman ${pageIndex + 1}...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// Precision Page Navigation & Numbering Models (Roman Preliminaries vs Arabic Content)
// -------------------------------------------------------------------------------------

enum class PageCategory {
    COVER,
    ROMAN,
    ARABIC
}

data class BookPageInfo(
    val physicalIndex: Int,
    val category: PageCategory,
    val displayLabel: String,
    val shortTag: String,
    val fullTitle: String,
    val arabicNumber: Int? = null,
    val romanNumber: String? = null
)

fun toRoman(number: Int): String {
    if (number <= 0) return ""
    val romanNumerals = listOf(
        1000 to "m", 900 to "cm", 500 to "d", 400 to "cd",
        100 to "c", 90 to "xc", 50 to "l", 40 to "xl",
        10 to "x", 9 to "ix", 5 to "v", 4 to "iv", 1 to "i"
    )
    var num = number
    val sb = StringBuilder()
    for ((value, roman) in romanNumerals) {
        while (num >= value) {
            sb.append(roman)
            num -= value
        }
    }
    return sb.toString()
}

fun fromRoman(input: String): Int? {
    val romanMap = mapOf('i' to 1, 'v' to 5, 'x' to 10, 'l' to 50, 'c' to 100, 'd' to 500, 'm' to 1000)
    val s = input.trim().lowercase()
    if (s.isEmpty() || !s.all { it in romanMap }) return null
    var total = 0
    var prev = 0
    for (char in s.reversed()) {
        val curr = romanMap[char] ?: return null
        if (curr < prev) {
            total -= curr
        } else {
            total += curr
            prev = curr
        }
    }
    return total
}

fun generatePageMapping(pageCount: Int, firstArabicIndex: Int): List<BookPageInfo> {
    return (0 until pageCount).map { idx ->
        when {
            idx == 0 && firstArabicIndex > 0 -> {
                BookPageInfo(
                    physicalIndex = idx,
                    category = PageCategory.COVER,
                    displayLabel = "Sampul",
                    shortTag = "Cover",
                    fullTitle = "Sampul Depan (Cover)"
                )
            }
            idx in 1 until firstArabicIndex -> {
                val romanNum = if (firstArabicIndex > 4) (idx + 1) else idx
                val romanStr = toRoman(romanNum)
                val title = when (romanNum) {
                    1 -> "Kata Pengantar"
                    2 -> if (firstArabicIndex > 4) "Redaksi / Hak Cipta" else "Petunjuk Penggunaan"
                    3 -> if (firstArabicIndex > 4) "Kata Pengantar" else "Daftar Isi"
                    4 -> "Daftar Isi"
                    else -> "Pra-Isi"
                }
                BookPageInfo(
                    physicalIndex = idx,
                    category = PageCategory.ROMAN,
                    displayLabel = romanStr,
                    shortTag = "Romawi",
                    fullTitle = "$title ($romanStr)",
                    romanNumber = romanStr
                )
            }
            else -> {
                val arabicNum = if (firstArabicIndex > 0) (idx - firstArabicIndex + 1) else (idx + 1)
                BookPageInfo(
                    physicalIndex = idx,
                    category = PageCategory.ARABIC,
                    displayLabel = arabicNum.toString(),
                    shortTag = "Hal.",
                    fullTitle = "Halaman $arabicNum",
                    arabicNumber = arabicNum
                )
            }
        }
    }
}

@Composable
fun JumpToPageDialog(
    pageCount: Int,
    firstArabicIndex: Int,
    currentPageIndex: Int,
    pageList: List<BookPageInfo>,
    onDismiss: () -> Unit,
    onJumpToPage: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Angka Biasa, 1: Angka Romawi
    var inputQuery by remember { mutableStateOf("") }
    val totalArabicPages = (pageCount - firstArabicIndex).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PinDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lompat ke Halaman Presisi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Aplikasi membedakan halaman pra-isi (Romawi) dan bab materi (Angka biasa) agar pencarian 100% presisi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tab Selector: Angka Biasa vs Romawi
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Halaman Angka", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Halaman Romawi", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                if (selectedTab == 0) {
                    // TAB 0: Halaman Angka Biasa (1, 2, 3...)
                    val parsedNumber = inputQuery.trim().toIntOrNull()
                    val targetPageInfo = if (parsedNumber != null && parsedNumber in 1..totalArabicPages) {
                        pageList.find { it.category == PageCategory.ARABIC && it.arabicNumber == parsedNumber }
                    } else null

                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it.filter { char -> char.isDigit() } },
                        label = { Text("Nomor Halaman (1 - $totalArabicPages)") },
                        placeholder = { Text("Ketik 1 untuk Halaman 1") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_jump_page_arabic"),
                        trailingIcon = {
                            if (inputQuery.isNotEmpty()) {
                                IconButton(onClick = { inputQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        }
                    )

                    // Quick Jump Chips
                    Text(
                        text = "Pilihan Cepat:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 25, 50, 100).filter { it <= totalArabicPages }.forEach { num ->
                            AssistChip(
                                onClick = { inputQuery = num.toString() },
                                label = { Text("Hal. $num", fontSize = 11.sp) },
                                modifier = Modifier.testTag("quick_jump_$num")
                            )
                        }
                    }

                    // Target Preview Card
                    Surface(
                        color = if (targetPageInfo != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (targetPageInfo != null) {
                                Text(
                                    text = "Target: ${targetPageInfo.fullTitle}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "• Lembar PDF ke-${targetPageInfo.physicalIndex + 1} dari $pageCount (Tepat pada bab materi)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            } else if (inputQuery.isNotBlank()) {
                                Text(
                                    text = "Nomor halaman tidak valid (Harus antara 1 - $totalArabicPages)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Contoh: Ketik '1' untuk langsung menuju Halaman 1 Bab Pertama (bukan halaman sampul).",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: Halaman Romawi & Sampul
                    Text(
                        text = "Pilih bagian pra-isi buku di bawah ini:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val romanAndCoverPages = pageList.filter { it.category != PageCategory.ARABIC }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(romanAndCoverPages) { pInfo ->
                            val isCurrent = pInfo.physicalIndex == currentPageIndex
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onJumpToPage(pInfo.physicalIndex)
                                        onDismiss()
                                    }
                                    .testTag("jump_roman_${pInfo.displayLabel}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            modifier = Modifier.size(36.dp),
                                            contentColor = MaterialTheme.colorScheme.tertiary
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = pInfo.displayLabel.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = if (pInfo.displayLabel.length > 3) 9.sp else 12.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(pInfo.fullTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Lembar berkas ke-${pInfo.physicalIndex + 1}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (isCurrent) {
                                        Text("Sedang Dibaca", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                val parsedNumber = inputQuery.trim().toIntOrNull()
                val targetPageInfo = if (parsedNumber != null && parsedNumber in 1..totalArabicPages) {
                    pageList.find { it.category == PageCategory.ARABIC && it.arabicNumber == parsedNumber }
                } else null

                Button(
                    onClick = {
                        targetPageInfo?.let {
                            onJumpToPage(it.physicalIndex)
                            onDismiss()
                        }
                    },
                    enabled = targetPageInfo != null,
                    modifier = Modifier.testTag("btn_confirm_jump_page")
                ) {
                    Text("Buka Halaman")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun PageCalibrationDialog(
    currentFirstArabicIndex: Int,
    currentPageIndex: Int,
    pageCount: Int,
    autoDetection: PdfPageNumberDetector.DetectionResult? = null,
    onDismiss: () -> Unit,
    onSaveOffset: (Int) -> Unit,
    onResetToAuto: (() -> Unit)? = null
) {
    var selectedOffset by remember { mutableIntStateOf(currentFirstArabicIndex) }
    var customInputText by remember { mutableStateOf((selectedOffset + 1).toString()) }

    val presetOptions = listOf(
        16 to "Buku Informatika SMP/MTs (Lembar ke-17 = Hal. 1 setelah Sampul & Romawi i..xvi)",
        6 to "Buku Bahasa Arab MTs Kemenag (Lembar ke-7 = Hal. 1 setelah Sampul & Romawi i..vi)",
        4 to "Buku Standar / Ringkas (Lembar ke-5 = Hal. 1 setelah Sampul & Romawi i..iii)",
        0 to "Tanpa Romawi (Lembar ke-1 = Hal. 1 dari awal berkas)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kalibrasi Penomoran Buku", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Aplikasi otomatis membedakan halaman Romawi (pendahuluan) dan halaman biasa (bab materi) sehingga saat Anda mengklik angka 1, buku langsung membuka Bab 1 yang sesungguhnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Auto-detection status card
                if (autoDetection != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hasil Deteksi Cerdas Sistem:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = autoDetection.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (selectedOffset != autoDetection.firstArabicIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            selectedOffset = autoDetection.firstArabicIndex
                                            customInputText = (autoDetection.firstArabicIndex + 1).toString()
                                            onResetToAuto?.invoke()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Kembalikan ke Deteksi Otomatis (Lembar ${autoDetection.firstArabicIndex + 1})", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 1-Click Quick Action: Current Viewed Page
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📍 Pintasan Cepat Lembar Aktif:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Halaman yang sedang Anda buka di layar saat ini adalah Lembar ke-${currentPageIndex + 1}.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedOffset = currentPageIndex
                                    customInputText = (currentPageIndex + 1).toString()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_calibrate_current_page"),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Jadikan Lembar ke-${currentPageIndex + 1} Sebagai Hal. 1", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Custom Sheet Input
                item {
                    Text(
                        text = "Atur Lembar Fisik Manual:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedOffset > 0) {
                                    selectedOffset--
                                    customInputText = (selectedOffset + 1).toString()
                                }
                            },
                            enabled = selectedOffset > 0
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurang")
                        }

                        OutlinedTextField(
                            value = customInputText,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                customInputText = filtered
                                val num = filtered.toIntOrNull()
                                if (num != null && num in 1..pageCount) {
                                    selectedOffset = num - 1
                                }
                            },
                            label = { Text("Lembar ke- (1-$pageCount)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_custom_sheet_offset")
                        )

                        IconButton(
                            onClick = {
                                if (selectedOffset < pageCount - 1) {
                                    selectedOffset++
                                    customInputText = (selectedOffset + 1).toString()
                                }
                            },
                            enabled = selectedOffset < pageCount - 1
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah")
                        }
                    }
                }

                // Presets
                item {
                    Text(
                        text = "Atau Pilih Rekomendasi Format:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(presetOptions) { (offset, label) ->
                    val isChecked = selectedOffset == offset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedOffset = offset
                                customInputText = (offset + 1).toString()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isChecked,
                                onClick = {
                                    selectedOffset = offset
                                    customInputText = (offset + 1).toString()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Live Preview Summary
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Hasil Penomoran Buku:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedOffset > 0) {
                                Text("• Lembar 1 : Sampul Depan (Cover)", fontSize = 11.sp)
                                if (selectedOffset > 1) {
                                    val lastRoman = toRoman(selectedOffset)
                                    Text("• Lembar 2 s/d $selectedOffset : Halaman Romawi (ii s/d $lastRoman)", fontSize = 11.sp)
                                }
                                Text("• Lembar ${selectedOffset + 1} s/d $pageCount : Halaman 1 s/d ${pageCount - selectedOffset}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("• Lembar 1 s/d $pageCount : Halaman 1 s/d $pageCount (Seluruhnya bernomor biasa)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveOffset(selectedOffset)
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_confirm_calibration")
            ) {
                Text("Terapkan & Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
