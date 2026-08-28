package dev.opencode.mobile.opencode.repository

import dev.opencode.mobile.core.database.ServerDao
import dev.opencode.mobile.core.database.ServerEntity
import dev.opencode.mobile.core.storage.AppPreferences
import dev.opencode.mobile.core.storage.CredentialStore
import dev.opencode.mobile.opencode.api.OpenCodeClientFactory
import dev.opencode.mobile.opencode.models.ConnectionState
import dev.opencode.mobile.opencode.models.OpenCodeError
import dev.opencode.mobile.opencode.models.ServerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ServerRepository(
    private val serverDao: ServerDao,
    private val credentialStore: CredentialStore,
    private val appPreferences: AppPreferences,
    private val clientFactory: OpenCodeClientFactory,
) {

    fun observeServers(): Flow<List<ServerProfile>> = serverDao.observeAll().map { entities ->
        entities.map { it.toProfile() }
    }

    val activeServerId: Flow<String?> = appPreferences.activeServerId

    suspend fun setActiveServer(id: String?) = appPreferences.setActiveServerId(id)

    /** Resolves the currently active server's profile, if any is set and still exists. */
    suspend fun getActiveServerProfile(): ServerProfile? {
        val id = activeServerId.first() ?: return null
        return getServerById(id)
    }

    /** Builds a live [dev.opencode.mobile.opencode.api.OpenCodeClient] for a given server id. */
    fun clientFor(profile: ServerProfile) = clientFactory.clientFor(profile)

    suspend fun getServerById(id: String): ServerProfile? = serverDao.getById(id)?.toProfile()

    suspend fun addOrUpdateServer(
        id: String? = null,
        name: String,
        baseUrl: String,
        useBasicAuth: Boolean,
        username: String?,
        password: String?,
        isDefault: Boolean = false,
    ): ServerProfile {
        val serverId = id ?: UUID.randomUUID().toString()
        val credentialRef = if (useBasicAuth && !password.isNullOrEmpty()) "cred_$serverId" else null

        if (credentialRef != null) {
            credentialStore.savePassword(credentialRef, password!!)
        } else if (id != null) {
            // auth was disabled/cleared for an existing server — drop any stored password
            serverDao.getById(id)?.credentialRef?.let { credentialStore.deletePassword(it) }
        }

        val entity = ServerEntity(
            id = serverId,
            name = name,
            baseUrl = baseUrl.trimEnd('/'),
            useBasicAuth = useBasicAuth,
            username = username,
            credentialRef = credentialRef,
            isDefault = isDefault,
        )
        serverDao.upsert(entity)
        clientFactory.invalidate(serverId)
        return entity.toProfile()
    }

    suspend fun deleteServer(id: String) {
        serverDao.getById(id)?.credentialRef?.let { credentialStore.deletePassword(it) }
        serverDao.delete(id)
        clientFactory.invalidate(id)
    }

    /**
     * Performs a real health-check call against the server. Used by "Test
     * connection" in Settings. Returns the resolved [ConnectionState] on
     * success; on failure returns the underlying [OpenCodeError] so the UI
     * can show a specific, human-readable message rather than a generic
     * failure state.
     */
    suspend fun testConnection(profile: ServerProfile): Result<ConnectionState> {
        val client = clientFactory.clientFor(profile)
        val result = client.health()
        return if (result.isSuccess) {
            Result.success(ConnectionState.CONNECTED)
        } else {
            Result.failure(result.exceptionOrNull() ?: OpenCodeError.Unknown("unknown failure"))
        }
    }

    private fun ServerEntity.toProfile() = ServerProfile(
        id = id,
        name = name,
        baseUrl = baseUrl,
        useBasicAuth = useBasicAuth,
        username = username,
        credentialRef = credentialRef,
        isDefault = isDefault,
    )
}
