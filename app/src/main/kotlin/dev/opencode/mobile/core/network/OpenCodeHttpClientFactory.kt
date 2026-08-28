package dev.opencode.mobile.core.network

import dev.opencode.mobile.opencode.models.ServerProfile
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Builds an OkHttpClient scoped to one [ServerProfile]. A fresh client is
 * built per-profile (rather than one global client) so that Basic Auth
 * headers and base timeouts can differ per saved server, and so a call in
 * flight against one server doesn't hold connections open against another.
 */
object OpenCodeHttpClientFactory {

    fun create(profile: ServerProfile, password: String?, verboseLogging: Boolean = false): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // SSE streams stay open indefinitely
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (profile.useBasicAuth && profile.username != null && password != null) {
            val credential = Credentials.basic(profile.username, password)
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", credential)
                    .build()
                chain.proceed(request)
            }
        }

        if (verboseLogging) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.HEADERS
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    /** Short-timeout client used only for the "Test connection" action. */
    fun createProbeClient(profile: ServerProfile, password: String?): OkHttpClient {
        val base = create(profile, password)
        return base.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }
}
