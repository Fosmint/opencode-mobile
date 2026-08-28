package dev.opencode.mobile

import android.app.Application
import dev.opencode.mobile.core.database.OpenCodeDatabase
import dev.opencode.mobile.core.storage.AppPreferences
import dev.opencode.mobile.core.storage.CredentialStore
import dev.opencode.mobile.opencode.api.OpenCodeClientFactory
import dev.opencode.mobile.opencode.repository.ServerRepository
import dev.opencode.mobile.opencode.repository.SessionRepository

/**
 * Manual DI container. This project deliberately avoids a DI framework
 * (Hilt/Koin) to keep the Gradle/KSP surface area small and reduce the
 * chance of annotation-processor version drift breaking CI builds; the
 * container is small enough that this stays readable at this project's
 * current size. If it grows meaningfully, migrating to Hilt is a
 * contained, low-risk change since consumers only see constructor
 * injection today.
 */
class OpenCodeApplication : Application() {

    lateinit var database: OpenCodeDatabase
        private set
    lateinit var credentialStore: CredentialStore
        private set
    lateinit var appPreferences: AppPreferences
        private set
    lateinit var clientFactory: OpenCodeClientFactory
        private set
    lateinit var serverRepository: ServerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = OpenCodeDatabase.get(this)
        credentialStore = CredentialStore(this)
        appPreferences = AppPreferences(this)
        clientFactory = OpenCodeClientFactory(credentialStore)
        serverRepository = ServerRepository(
            serverDao = database.serverDao(),
            credentialStore = credentialStore,
            appPreferences = appPreferences,
            clientFactory = clientFactory,
        )
    }

    /**
     * Builds a [SessionRepository] bound to the currently active server, or
     * null if no server is configured/selected yet. This is intentionally
     * built on demand (not cached at the Application level) since the
     * active server can change from Settings while other screens are open.
     */
    suspend fun sessionRepositoryForActiveServer(): SessionRepository? {
        val profile = serverRepository.getActiveServerProfile() ?: return null
        val client = serverRepository.clientFor(profile)
        return SessionRepository(
            serverId = profile.id,
            client = client,
            sessionDao = database.sessionDao(),
            messageDao = database.messageDao(),
        )
    }
}
