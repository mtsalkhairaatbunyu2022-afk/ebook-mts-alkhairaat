package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // ADMIN, TEACHER, STUDENT
    val token: String,
    val password: String = "",
    val gradeClass: String = "",
    val isLoggedIn: Boolean = false,
    val lastActive: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "books",
    indices = [Index(value = ["category"]), Index(value = ["gradeClass"])]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val subject: String,
    val gradeClass: String, // SMP-7, SMP-8, SMP-9
    val teacherOwner: String,
    val year: String,
    val fileUrl: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val category: String,
    val description: String,
    val isOcrReady: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["bookId"]), Index(value = ["userId"])]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val userId: String,
    val pageNumber: Int,
    val markerTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_history",
    indices = [Index(value = ["bookId"]), Index(value = ["userId"])]
)
data class ReadingHistoryEntity(
    @PrimaryKey val bookId: String, // Shared key or compound
    val userId: String,
    val lastReadPage: Int,
    val lastReadTime: Long = System.currentTimeMillis(),
    val progressPercentage: Int = 0
)

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey val bookId: String,
    val title: String,
    val url: String,
    val progress: Int,
    val status: String, // PENDING, DOWNLOADING, COMPLETED, FAILED
    val localPath: String? = null,
    val error: String? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val action: String, // LOGIN, UPLOAD, VIEW, DOWNLOAD
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

@Entity(
    tableName = "notes",
    indices = [Index(value = ["bookId"]), Index(value = ["userId"]), Index(value = ["gradeClass"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val bookId: String,
    val bookTitle: String,
    val pageNumber: Int,
    val noteContent: String,
    val gradeClass: String = "Kelas VII",
    val createdAt: Long = System.currentTimeMillis()
)
