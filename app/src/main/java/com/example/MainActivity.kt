package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.BookEntity
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Bind system theme & VM dark mode states
            MyApplicationTheme(darkTheme = viewModel.isAppDarkMode) {
                var hasStartedApp by remember { mutableStateOf(true) }
                var requestOpenLogin by remember { mutableStateOf(false) }
                val userState by viewModel.loggedInUser.collectAsState()
                val fallbackUser = remember {
                    com.example.data.UserEntity(
                        id = "usr_student_default",
                        name = "Siswa MTs Al-Khairaat",
                        email = "siswa@mtsalkhairaat.sch.id",
                        role = "STUDENT",
                        token = "jwt_student_token",
                        password = "",
                        gradeClass = "Semua Kelas",
                        isLoggedIn = true
                    )
                }
                val currentUser = userState ?: fallbackUser

                // Ensure full access user exists on launch
                LaunchedEffect(Unit) {
                    viewModel.ensureFullAccessUser()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!hasStartedApp) {
                        // Opening Welcome Dashboard Screen with "Mulai Aplikasi" button
                        com.example.ui.WelcomeDashboardScreen(
                            viewModel = viewModel,
                            onStartApp = {
                                viewModel.ensureFullAccessUser()
                                hasStartedApp = true
                            },
                            onLoginWithCard = {
                                viewModel.ensureFullAccessUser()
                                hasStartedApp = true
                                requestOpenLogin = true
                            }
                        )
                    } else {
                        // Central E-Book App Layout with full access
                        MainAppContent(
                            viewModel = viewModel,
                            user = currentUser,
                            requestOpenLogin = requestOpenLogin,
                            onResetOpenLogin = { requestOpenLogin = false },
                            onBackToWelcome = {
                                hasStartedApp = false
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class SchoolAppNavigator {
    DASHBOARD,
    NOTES
}

enum class LayoutState {
    MAIN_SHELL,
    BOOK_DETAIL,
    PDF_READER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: AppViewModel,
    user: com.example.data.UserEntity,
    requestOpenLogin: Boolean = false,
    onResetOpenLogin: () -> Unit = {},
    onBackToWelcome: () -> Unit = {}
) {
    var activeLayoutState by rememberSaveable { mutableStateOf(LayoutState.MAIN_SHELL) }
    var currentTabSelected by rememberSaveable { mutableStateOf(SchoolAppNavigator.DASHBOARD) }
    var selectedBookDetail by remember { mutableStateOf<BookEntity?>(null) }
    var showTeacherPinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showStudentLoginDialog by remember { mutableStateOf(false) }
    val isTeacher = user.role == "TEACHER" || user.role == "ADMIN"
    val isNamedStudent = !isTeacher && user.id != "usr_student_default"

    LaunchedEffect(requestOpenLogin) {
        if (requestOpenLogin) {
            showStudentLoginDialog = true
            onResetOpenLogin()
        }
    }

    // Dynamic edge-to-edge layout styling
    AnimatedContent(
        targetState = activeLayoutState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "LayerTransitions"
    ) { layout ->
        when (layout) {
            LayoutState.MAIN_SHELL -> {
                BackHandler(enabled = currentTabSelected != SchoolAppNavigator.DASHBOARD) {
                    currentTabSelected = SchoolAppNavigator.DASHBOARD
                }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        currentTabSelected = SchoolAppNavigator.DASHBOARD
                                    }
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_alkhairaat),
                                        contentDescription = "Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = "E-Book MTs Al-Khairaat Bunyu", fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            actions = {
                                // Mode Indicator & Switcher Button
                                if (isTeacher) {
                                    FilledTonalButton(
                                        onClick = { viewModel.activateStudentMode() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 4.dp).testTag("btn_mode_teacher")
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mode Guru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showTeacherPinDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 4.dp).testTag("btn_switch_to_teacher")
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mode Guru", fontSize = 11.sp)
                                    }
                                }

                                // Dynamic theme toggle button
                                IconButton(
                                    onClick = { viewModel.isAppDarkMode = !viewModel.isAppDarkMode },
                                    modifier = Modifier.testTag("theme_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.isAppDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme"
                                    )
                                }
                                
                                var userMenuExpanded by remember { mutableStateOf(false) }

                                IconButton(onClick = { userMenuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu Opsi")
                                }

                                DropdownMenu(
                                    expanded = userMenuExpanded,
                                    onDismissRequest = { userMenuExpanded = false }
                                ) {
                                    if (isTeacher) {
                                        DropdownMenuItem(
                                            text = { Text("Kunci & Mode Siswa") },
                                            onClick = {
                                                userMenuExpanded = false
                                                viewModel.activateStudentMode()
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Lock, contentDescription = null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Ubah PIN Guru") },
                                            onClick = {
                                                userMenuExpanded = false
                                                showChangePinDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Password, contentDescription = null)
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Buka Mode Guru (PIN)") },
                                            onClick = {
                                                userMenuExpanded = false
                                                showTeacherPinDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.LockOpen, contentDescription = null)
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text(if (isNamedStudent) "Ganti Akun (${user.name})" else "Masuk Kartu Siswa") },
                                        onClick = {
                                            userMenuExpanded = false
                                            showStudentLoginDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, contentDescription = null)
                                        }
                                    )

                                    if (isNamedStudent) {
                                        DropdownMenuItem(
                                            text = { Text("Keluar Akun (Akses Bebas)") },
                                            onClick = {
                                                userMenuExpanded = false
                                                viewModel.performLogout()
                                            },
                                            leadingIcon = {
                                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Dashboard Awal") },
                                        onClick = {
                                            userMenuExpanded = false
                                            onBackToWelcome()
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Home,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        // Standard interactive M3 navigation bar with active pills
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTabSelected == SchoolAppNavigator.DASHBOARD,
                                onClick = { currentTabSelected = SchoolAppNavigator.DASHBOARD },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTabSelected == SchoolAppNavigator.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                        contentDescription = "Dashboard"
                                    )
                                },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTabSelected == SchoolAppNavigator.NOTES,
                                onClick = { currentTabSelected = SchoolAppNavigator.NOTES },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTabSelected == SchoolAppNavigator.NOTES) Icons.Default.EditNote else Icons.Outlined.EditNote,
                                        contentDescription = "Catatan"
                                    )
                                },
                                label = { Text("Catatan") },
                                modifier = Modifier.testTag("nav_notes")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTabSelected) {
                            SchoolAppNavigator.DASHBOARD -> {
                                BookDashboard(
                                    viewModel = viewModel,
                                    user = user,
                                    onBookSelected = { book ->
                                        selectedBookDetail = book
                                        activeLayoutState = LayoutState.BOOK_DETAIL
                                    },
                                    onOpenLogin = {
                                        showStudentLoginDialog = true
                                    }
                                )
                            }
                            SchoolAppNavigator.NOTES -> {
                                NotesScreen(
                                    viewModel = viewModel,
                                    user = user,
                                    onBookSelected = { book ->
                                        selectedBookDetail = book
                                        activeLayoutState = LayoutState.BOOK_DETAIL
                                    }
                                )
                            }
                        }
                    }
                }

                if (showStudentLoginDialog) {
                    StudentLoginDialog(
                        viewModel = viewModel,
                        onDismiss = { showStudentLoginDialog = false },
                        onLoginSuccess = {
                            showStudentLoginDialog = false
                        }
                    )
                }
                if (showTeacherPinDialog) {
                    TeacherPinDialog(
                        onDismiss = { showTeacherPinDialog = false },
                        onVerifyPin = { pin -> viewModel.activateTeacherMode(pin) },
                        onSuccess = { showTeacherPinDialog = false }
                    )
                }
                if (showChangePinDialog) {
                    ChangeTeacherPinDialog(
                        currentPin = viewModel.teacherPin,
                        onDismiss = { showChangePinDialog = false },
                        onPinChanged = { newPin -> viewModel.updateTeacherPin(newPin) }
                    )
                }
            }

            LayoutState.BOOK_DETAIL -> {
                BackHandler {
                    activeLayoutState = LayoutState.MAIN_SHELL
                    selectedBookDetail = null
                }
                if (selectedBookDetail != null) {
                    BookDetailScreen(
                        book = selectedBookDetail!!,
                        viewModel = viewModel,
                        onRead = {
                            activeLayoutState = LayoutState.PDF_READER
                        },
                        onBack = {
                            activeLayoutState = LayoutState.MAIN_SHELL
                            selectedBookDetail = null
                        }
                    )
                }
            }

            LayoutState.PDF_READER -> {
                BackHandler {
                    activeLayoutState = LayoutState.MAIN_SHELL
                }
                if (selectedBookDetail != null) {
                    val book = selectedBookDetail!!
                    // Prepare the physical file path indicator (Downloaded or Programmatically generated cache)
                    val pdfFile = File(
                        viewModel.getApplication<android.app.Application>().filesDir,
                        "${book.id}.pdf"
                    )

                    var fileExists by remember(book.id) { mutableStateOf(pdfFile.exists()) }
                    // Automated lazy physical file creation helper so it is fully readable out-of-the-box!
                    LaunchedEffect(book.id) {
                        if (!pdfFile.exists()) {
                            // Programmatic background builder rendering for fast demo compatibility!
                            viewModel.unduhBuku(book.id)
                            while (!pdfFile.exists()) {
                                kotlinx.coroutines.delay(100)
                            }
                        }
                        fileExists = true
                    }

                    if (fileExists) {
                        PdfPageReader(
                            file = pdfFile,
                            bookId = book.id,
                            bookTitle = book.title,
                            bookGradeClass = book.gradeClass,
                            userId = user.id,
                            isDarkMode = viewModel.isAppDarkMode,
                            viewModel = viewModel,
                            onBack = {
                                activeLayoutState = LayoutState.MAIN_SHELL
                            }
                        )
                    } else {
                        // Cozy rendering progress spinner
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Mempersiapkan Berkas Buku...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Membuat format render acuan halaman PDF secara aman.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
