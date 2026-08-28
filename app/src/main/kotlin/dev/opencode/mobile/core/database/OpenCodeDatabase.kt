package dev.opencode.mobile.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ServerEntity::class, SessionEntity::class, MessageEntity::class, ProjectEntity::class],
    version = 1,
    // No schema export directory is configured for this module (no
    // `room.schemaLocation` ksp arg, no Room Gradle plugin applied), so
    // exportSchema=true just produces a build warning every run with
    // nothing to show for it. Flip to true once schema history/migration
    // testing is actually needed and room.schemaLocation is wired up.
    exportSchema = false,
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
