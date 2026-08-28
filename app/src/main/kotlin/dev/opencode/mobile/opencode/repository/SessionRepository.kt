package dev.opencode.mobile.opencode.repository

import dev.opencode.mobile.core.database.MessageDao
import dev.opencode.mobile.core.database.MessageEntity
import dev.opencode.mobile.core.database.SessionDao
import dev.opencode.mobile.core.database.SessionEntity
import dev.opencode.mobile.core.network.JsonConfig
import dev.opencode.mobile.opencode.api.OpenCodeClient
import dev.opencode.mobile.opencode.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Mediates between a live [OpenCodeClient] and the local Room cache so the
 * UI has something to show immediately on cold start (from cache) while a
 * fresh fetch is in flight, and so the app can restore session/message
 * state after an app restart (requirement: "восстанавливать состояние
 * после перезапуска приложения").
 */
class SessionRepository(
    private val serverId: String,
    val client: OpenCodeClient,
    private val sessionDao: SessionDao,
    private val messageDao: MessageDao,
) {

    fun observeCachedSessions(projectId: String): Flow<List<SessionInfo>> =
        sessionDao.observeForProject(serverId, projectId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshSessions(projectId: String): Result<List<SessionInfo>> {
        val result = client.listSessions(projectId = projectId)
        result.onSuccess { response ->
            sessionDao.upsertAll(response.data.map { it.toEntity(serverId) })
        }
        return result.map { it.data }
    }

    suspend fun createSession(projectId: String? = null, agent: String? = null, model: ModelRef? = null): Result<SessionInfo> {
        val result = client.createSession(
            SessionCreateRequest(agent = agent, model = model)
        )
        result.onSuccess { session -> sessionDao.upsertAll(listOf(session.toEntity(serverId))) }
        return result
    }

    fun observeCachedMessages(sessionId: String): Flow<List<SessionMessage>> =
        messageDao.observeForSession(sessionId).map { entities ->
            entities.mapNotNull { entity ->
                runCatching { JsonConfig.default.decodeFromString<SessionMessage>(entity.rawJson) }.getOrNull()
            }
        }

    suspend fun refreshContext(sessionId: String): Result<List<SessionMessage>> {
        val result = client.getSessionContext(sessionId)
        result.onSuccess { messages -> cacheMessages(sessionId, messages) }
        return result
    }

    suspend fun cacheMessages(sessionId: String, messages: List<SessionMessage>) {
        messageDao.upsertAll(
            messages.map { message ->
                MessageEntity(
                    id = message.id,
                    sessionId = sessionId,
                    type = message.type,
                    rawJson = JsonConfig.default.encodeToString<SessionMessage>(message),
                    createdAt = message.time.created,
                )
            }
        )
    }

    suspend fun sendMessage(sessionId: String, text: String): Result<SessionAdmitted> =
        client.sendPrompt(sessionId, SessionPromptRequest(prompt = PromptInput(text = text)))

    suspend fun interrupt(sessionId: String): Result<Unit> = client.interruptSession(sessionId)

    suspend fun switchModel(sessionId: String, model: ModelRef): Result<Unit> = client.switchModel(sessionId, model)

    suspend fun switchAgent(sessionId: String, agent: String): Result<Unit> = client.switchAgent(sessionId, agent)

    suspend fun listModels(): Result<List<ModelInfo>> = client.listModels()

    fun sessionEvents(sessionId: String, after: Long? = null) = client.subscribeToSessionEvents(sessionId, after)

    private fun SessionInfo.toEntity(serverId: String) = SessionEntity(
        id = id,
        serverId = serverId,
        projectId = projectID,
        title = title,
        agent = agent,
        modelProviderId = model?.providerID,
        modelModelId = model?.modelID,
        createdAt = time.created,
        updatedAt = time.updated,
    )

    private fun SessionEntity.toDomain() = SessionInfo(
        id = id,
        projectID = projectId,
        agent = agent,
        model = if (modelProviderId != null && modelModelId != null) ModelRef(modelProviderId, modelModelId) else null,
        time = SessionTime(created = createdAt, updated = updatedAt),
        title = title,
        location = LocationRef(),
    )
}
