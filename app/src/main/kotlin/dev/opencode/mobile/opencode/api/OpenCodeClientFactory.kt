package dev.opencode.mobile.opencode.api

import dev.opencode.mobile.core.network.OpenCodeHttpClientFactory
import dev.opencode.mobile.core.storage.CredentialStore
import dev.opencode.mobile.opencode.models.ServerProfile

/**
 * Builds and caches an [OpenCodeClient] per [ServerProfile]. Remote is the
 * only mode implemented today; the factory is the single place that would
 * need to change to route a given profile to [FutureLocalOpenCodeClient]
 * once an embedded runtime exists (e.g. a profile flag `mode = "local"`).
 */
class OpenCodeClientFactory(private val credentialStore: CredentialStore) {

    private val cache = mutableMapOf<String, OpenCodeClient>()

    fun clientFor(profile: ServerProfile): OpenCodeClient = cache.getOrPut(profile.id) {
        val password = profile.credentialRef?.let { credentialStore.getPassword(it) }
        val httpClient = OpenCodeHttpClientFactory.create(profile, password)
        RemoteOpenCodeClient(baseUrl = profile.baseUrl, httpClient = httpClient)
    }

    fun invalidate(profileId: String) {
        cache.remove(profileId)
    }
}
