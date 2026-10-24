package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserSync(): UserEntity?
    
    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY name ASC")
    fun getAllStudents(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY lastActive DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearSession()
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdSync(id: String): BookEntity?

    @Query("SELECT * FROM books WHERE (title LIKE :query OR author LIKE :query OR subject LIKE :query) AND (:gradeClass = '' OR :gradeClass = 'Semua Kelas' OR gradeClass = :gradeClass OR gradeClass = 'Semua Kelas') AND (:category = '' OR category = :category) ORDER BY title ASC")
    fun searchAndFilterBooks(query: String, gradeClass: String, category: String): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND userId = :userId ORDER BY pageNumber ASC")
    fun getBookmarksForBook(bookId: String, userId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND userId = :userId AND pageNumber = :page")
    suspend fun deleteBookmarkForPage(bookId: String, userId: String, page: Int)
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT reading_history.*, books.title as bookTitle, books.author as bookAuthor, books.subject as bookSubject FROM reading_history INNER JOIN books ON reading_history.bookId = books.id WHERE reading_history.userId = :userId ORDER BY reading_history.lastReadTime DESC")
    fun getReadingHistory(userId: String): Flow<List<HistoryWithBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ReadingHistoryEntity)
}

data class HistoryWithBook(
    @Embedded val history: ReadingHistoryEntity,
    val bookTitle: String,
    val bookAuthor: String,
    val bookSubject: String
)

@Dao
interface DownloadTaskDao {
    @Query("SELECT * FROM download_tasks WHERE bookId = :bookId")
    fun getDownloadTask(bookId: String): Flow<DownloadTaskEntity?>

    @Query("SELECT * FROM download_tasks")
    fun getAllDownloadTasks(): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadTask(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET progress = :progress, status = :status, localPath = :localPath, error = :error WHERE bookId = :bookId")
    suspend fun updateProgress(bookId: String, progress: Int, status: String, localPath: String?, error: String?)

    @Query("DELETE FROM download_tasks WHERE bookId = :bookId")
    suspend fun deleteDownloadTask(bookId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllNotesForUser(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE gradeClass = :gradeClass ORDER BY createdAt DESC")
    fun getNotesForGrade(gradeClass: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE bookId = :bookId AND userId = :userId ORDER BY createdAt DESC")
    fun getNotesForBook(bookId: String, userId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
