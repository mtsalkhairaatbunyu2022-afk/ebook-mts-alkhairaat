package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        CategoryEntity::class,
        BookmarkEntity::class,
        ReadingHistoryEntity::class,
        DownloadTaskEntity::class,
        AuditLogEntity::class,
        NoteEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun downloadTaskDao(): DownloadTaskDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ebook_sekolah_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
