package dev.opencode.mobile.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ServerEntity::class, SessionEntity::class, MessageEntity::class, ProjectEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OpenCodeDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile private var instance: OpenCodeDatabase? = null

        fun get(context: Context): OpenCodeDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                OpenCodeDatabase::class.java,
                "opencode.db",
            ).build().also { instance = it }
        }
    }
}
