package dev.opencode.mobile.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val useBasicAuth: Boolean,
    val username: String?,
    val credentialRef: String?,
    val isDefault: Boolean,
)

@Entity(tableName = "sessions_cache")
data class SessionEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val projectId: String,
    val title: String,
    val agent: String?,
    val modelProviderId: String?,
    val modelModelId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "messages_cache")
data class MessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: String,
    val rawJson: String,
    val createdAt: Long,
)

@Entity(tableName = "projects_cache")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val name: String?,
    val worktree: String,
    val vcs: String?,
)
