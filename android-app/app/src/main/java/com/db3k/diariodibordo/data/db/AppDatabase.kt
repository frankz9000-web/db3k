package com.db3k.diariodibordo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.db3k.diariodibordo.data.model.Notification
import com.db3k.diariodibordo.data.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for DB3K Blog
 */
@Database(
    entities = [Post::class, Notification::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "db3k_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with sample data if needed
                CoroutineScope(Dispatchers.IO).launch {
                    // Database created
                }
            }
        }
    }
}

/**
 * Type converters for Room
 */
class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @androidx.room.TypeConverter
    fun fromDate(date: java.util.Date?): Long? {
        return date?.time
    }

    @androidx.room.TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    @androidx.room.TypeConverter
    fun fromListString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}