package dev.opencode.mobile.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY name ASC")
    fun observeAll(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getById(id: String): ServerEntity?

    @Upsert
    suspend fun upsert(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions_cache WHERE serverId = :serverId AND projectId = :projectId ORDER BY updatedAt DESC")
    fun observeForProject(serverId: String, projectId: String): Flow<List<SessionEntity>>

    @Upsert
    suspend fun upsertAll(sessions: List<SessionEntity>)

    @Query("DELETE FROM sessions_cache WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages_cache WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeForSession(sessionId: String): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Upsert
    suspend fun upsertAll(messages: List<MessageEntity>)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects_cache WHERE serverId = :serverId")
    fun observeForServer(serverId: String): Flow<List<ProjectEntity>>

    @Upsert
    suspend fun upsert(project: ProjectEntity)
}
