package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(application, db)

    // App Share URL configuration (persisted in SharedPreferences)
    private val prefs = application.getSharedPreferences("school_ebook_prefs", android.content.Context.MODE_PRIVATE)
    var appShareUrl by mutableStateOf(
        prefs.getString("app_share_url", "https://ebook-mts-alkhairaat.pages.dev") ?: "https://ebook-mts-alkhairaat.pages.dev"
    )
        private set

    fun updateAppShareUrl(newUrl: String) {
        appShareUrl = newUrl
        prefs.edit().putString("app_share_url", newUrl).apply()
    }

    // Teacher PIN (Persisted in SharedPreferences, default "1926")
    var teacherPin by mutableStateOf(
        prefs.getString("teacher_pin", "1926") ?: "1926"
    )
        private set

    fun updateTeacherPin(newPin: String) {
        teacherPin = newPin
        prefs.edit().putString("teacher_pin", newPin).apply()
    }

    fun verifyTeacherPin(inputPin: String): Boolean {
        return inputPin.trim() == teacherPin.trim()
    }

    fun activateTeacherMode(pin: String): Boolean {
        if (verifyTeacherPin(pin)) {
            viewModelScope.launch {
                repository.switchUserRole("TEACHER", "Ustadz / Guru Pengajar")
            }
            return true
        }
        return false
    }

    fun activateStudentMode() {
        viewModelScope.launch {
            repository.switchUserRole("STUDENT", "Siswa MTs Al-Khairaat")
        }
    }

    fun ensureFullAccessUser(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.ensureDefaultUser()
            onSuccess()
        }
    }

    // Auth State
    val loggedInUser: StateFlow<UserEntity?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User State
    val allStudents: StateFlow<List<UserEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Session Input form states
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var authError by mutableStateOf<String?>(null)
    var isAuthenticating by mutableStateOf(false)

    // Search and Filters Flows
    val searchQueryState = MutableStateFlow("")
    val selectedGradeFilterState = MutableStateFlow("")
    val selectedCategoryFilterState = MutableStateFlow("")

    var searchQuery: String
        get() = searchQueryState.value
        set(value) { searchQueryState.value = value }

    var selectedGradeFilter: String
        get() = selectedGradeFilterState.value
        set(value) { selectedGradeFilterState.value = value }

    var selectedCategoryFilter: String
        get() = selectedCategoryFilterState.value
        set(value) { selectedCategoryFilterState.value = value }

    // Category Stream
    val allCategories: StateFlow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered books stream
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredBooks: StateFlow<List<BookEntity>> = combine(
        searchQueryState,
        selectedGradeFilterState,
        selectedCategoryFilterState,
        loggedInUser
    ) { query, grade, category, user ->
        val effectiveGrade = if (grade.isNotEmpty() && grade != "Semua Kelas") {
            grade
        } else if (grade == "Semua Kelas") {
            ""
        } else if (user?.role == "STUDENT" && user.gradeClass.isNotBlank() && user.gradeClass != "Semua Kelas") {
            user.gradeClass
        } else {
            ""
        }
        android.util.Log.d("AppViewModel", "Filtering books: query='$query', effectiveGrade='$effectiveGrade', category='$category', userRole='${user?.role}', userName='${user?.name}'")
        repository.searchAndFilter(query, effectiveGrade, category)
    }.flatMapLatest { flow ->
        flow.onEach { list ->
            android.util.Log.d("AppViewModel", "Filtered books emission size: ${list.size}")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit logs
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Download monitoring
    val activeDownloads: StateFlow<List<DownloadTaskEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Main dark mode toggle
    var isAppDarkMode by mutableStateOf(false)

    // Upload dialog fields
    var uploadTitle by mutableStateOf("")
    var uploadAuthor by mutableStateOf("")
    var uploadSubject by mutableStateOf("")
    var uploadGrade by mutableStateOf("")
    var uploadCategory by mutableStateOf("Mata Pelajaran")
    var uploadYear by mutableStateOf("2026")
    var uploadDesc by mutableStateOf("")
    var uploadFileExt by mutableStateOf("pdf") // pdf or docx
    var uploadProgress by mutableStateOf(0)
    var isUploading by mutableStateOf(false)

    // Remote Catalog Sync State
    var isSyncingCatalog by mutableStateOf(false)
    var syncStatusMessage by mutableStateOf<String?>(null)

    fun syncCatalogFromCloudflare() {
        isSyncingCatalog = true
        syncStatusMessage = null
        viewModelScope.launch {
            repository.syncRemoteCatalog(appShareUrl)
                .onSuccess { count ->
                    isSyncingCatalog = false
                    syncStatusMessage = if (count > 0) {
                        "Katalog berhasil diperbarui! $count buku siap digunakan."
                    } else {
                        "Katalog bersih (0 buku)."
                    }
                }
                .onFailure {
                    isSyncingCatalog = false
                    syncStatusMessage = "Katalog lokal offline aktif."
                }
        }
    }

    fun syncStudentsFromCloudflare() {
        viewModelScope.launch {
            repository.syncRemoteStudents(appShareUrl)
        }
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            repository.syncRemoteStudents(appShareUrl)
            syncCatalogFromCloudflare()
        }
    }

    // Auth Commands
    fun performLogin(onSuccess: () -> Unit = {}) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            authError = "Nama siswa dan kata sandi wajib diisi"
            return
        }
        isAuthenticating = true
        authError = null
        viewModelScope.launch {
            repository.login(loginEmail, loginPassword)
                .onSuccess { user ->
                    isAuthenticating = false
                    authError = null
                    // Clear search and filter states upon successful login
                    searchQueryState.value = ""
                    if (user.gradeClass.startsWith("Kelas")) {
                        selectedGradeFilterState.value = user.gradeClass
                    } else {
                        selectedGradeFilterState.value = ""
                    }
                    selectedCategoryFilterState.value = ""
                    onSuccess()
                }
                .onFailure { err ->
                    isAuthenticating = false
                    authError = err.localizedMessage ?: "Masuk gagal, periksa sandi Anda"
                }
        }
    }

    fun performLogout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            loginEmail = ""
            loginPassword = ""
            // Clear filter states on logout
            searchQueryState.value = ""
            selectedGradeFilterState.value = ""
            selectedCategoryFilterState.value = ""
            repository.ensureDefaultUser()
            onComplete()
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val adminId = loggedInUser.value?.id ?: "ADMIN"
            val adminName = loggedInUser.value?.name ?: "System Admin"
            repository.deleteUser(userId, adminId, adminName)
        }
    }

    var changePasswordError by mutableStateOf<String?>(null)
    var isChangingPassword by mutableStateOf(false)

    fun changeStudentPassword(
        userId: String,
        oldPass: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isChangingPassword = true
        changePasswordError = null
        viewModelScope.launch {
            repository.changeStudentPassword(userId, oldPass, newPass)
                .onSuccess {
                    isChangingPassword = false
                    changePasswordError = null
                    onSuccess()
                }
                .onFailure { err ->
                    isChangingPassword = false
                    val msg = err.localizedMessage ?: "Gagal mengubah kata sandi"
                    changePasswordError = msg
                    onError(msg)
                }
        }
    }

    fun resetStudentPassword(
        userId: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.resetStudentPassword(userId, newPass)
                .onSuccess { onSuccess() }
                .onFailure { err -> onError(err.localizedMessage ?: "Gagal mereset kata sandi") }
        }
    }

    // Download action
    fun unduhBuku(bookId: String) {
        viewModelScope.launch {
            repository.startDownload(bookId)
        }
    }

    // Delete Cache
    fun hapusOfflineCache(bookId: String) {
        viewModelScope.launch {
            repository.deleteOfflineBook(bookId)
        }
    }

    // Upload book flow
    fun performUpload(fileUri: android.net.Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val effectiveTitle = uploadTitle.ifBlank { uploadSubject.ifBlank { "Buku Pelajaran" } }
        if (uploadSubject.isBlank() || uploadGrade.isEmpty()) {
            onError("Sila lengkapi kolom Mata Pelajaran dan Jenjang Kelas.")
            return
        }
        if (fileUri == null) {
            onError("Memerlukan file PDF/DOCX")
            return
        }
        isUploading = true
        uploadProgress = 0
        viewModelScope.launch {
            val res = repository.uploadDocxOrPdf(
                title = effectiveTitle,
                author = uploadAuthor,
                subject = uploadSubject,
                gradeClass = uploadGrade,
                year = uploadYear,
                category = uploadCategory,
                description = uploadDesc,
                fileExtension = uploadFileExt,
                sourceUri = fileUri
            ) { progress ->
                uploadProgress = progress
            }

            res.onSuccess {
                isUploading = false
                // Reset fields
                uploadTitle = ""
                uploadAuthor = ""
                uploadSubject = ""
                uploadDesc = ""
                onSuccess()
            }
            res.onFailure {
                isUploading = false
                onError(it.localizedMessage ?: "Gagal mengunggah berkas")
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            repository.deleteOfflineBook(bookId)
        }
    }

    // Bookmark manager for active reader
    fun getBookmarksForBook(bookId: String, userId: String): Flow<List<BookmarkEntity>> {
        return repository.getBookmarks(bookId, userId)
    }

    fun toggleBookmark(bookId: String, userId: String, page: Int, title: String, isNowBookmarked: Boolean) {
        viewModelScope.launch {
            if (isNowBookmarked) {
                repository.removeBookmarkForPage(bookId, userId, page)
            } else {
                repository.addBookmark(bookId, userId, page, title)
            }
        }
    }

    fun removeSpecificBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.removeBookmark(bookmarkId)
        }
    }

    // Notes manager
    fun getAllNotesForUser(userId: String): Flow<List<com.example.data.NoteEntity>> {
        return repository.getAllNotesForUser(userId)
    }

    fun getNotesForGrade(gradeClass: String): Flow<List<com.example.data.NoteEntity>> {
        return repository.getNotesForGrade(gradeClass)
    }

    fun getAllNotes(): Flow<List<com.example.data.NoteEntity>> {
        return repository.getAllNotes()
    }

    fun getNotesForBook(bookId: String, userId: String): Flow<List<com.example.data.NoteEntity>> {
        return repository.getNotesForBook(bookId, userId)
    }

    fun insertNote(
        userId: String,
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        noteContent: String,
        gradeClass: String = "Kelas VII"
    ) {
        viewModelScope.launch {
            repository.insertNote(
                com.example.data.NoteEntity(
                    userId = userId,
                    bookId = bookId,
                    bookTitle = bookTitle,
                    pageNumber = pageNumber,
                    noteContent = noteContent,
                    gradeClass = gradeClass
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun updateNote(
        noteId: Long,
        userId: String,
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        noteContent: String,
        gradeClass: String = "Kelas VII",
        createdAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.updateNote(
                com.example.data.NoteEntity(
                    id = noteId,
                    userId = userId,
                    bookId = bookId,
                    bookTitle = bookTitle,
                    pageNumber = pageNumber,
                    noteContent = noteContent,
                    gradeClass = gradeClass,
                    createdAt = createdAt
                )
            )
        }
    }

    // Last read position logging
    fun saveLastReadPage(bookId: String, userId: String, page: Int, totalPages: Int) {
        viewModelScope.launch {
            repository.saveReadingPosition(bookId, userId, page, totalPages)
        }
    }

    fun getReadingPositionLive(bookId: String, userId: String): Flow<ReadingHistoryEntity?> {
        return db.readingHistoryDao().getReadingHistory(userId)
            .map { list -> list.firstOrNull { it.history.bookId == bookId }?.history }
    }
}
