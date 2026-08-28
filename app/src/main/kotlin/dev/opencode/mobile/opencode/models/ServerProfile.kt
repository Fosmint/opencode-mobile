package dev.opencode.mobile.opencode.models

/**
 * A saved connection to an OpenCode server (Remote mode). Credentials are
 * stored separately via EncryptedSharedPreferences / Android Keystore —
 * this model only carries the non-secret connection metadata plus a
 * reference (credentialRef) to look up the password if one is set.
 */
data class ServerProfile(
    val id: String,
    val name: String,
    val baseUrl: String,
    val useBasicAuth: Boolean,
    val username: String? = null,
    val credentialRef: String? = null,
    val isDefault: Boolean = false,
)

enum class ConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    UNAUTHORIZED,
    UNREACHABLE,
    TIMEOUT,
    ERROR,
}

sealed class OpenCodeError(override val message: String) : Throwable(message) {
    data class ConnectionFailed(val detail: String) : OpenCodeError("Connection failed: $detail")
    data class ServerUnavailable(val detail: String) : OpenCodeError("Server unavailable: $detail")
    data class AuthenticationFailed(val detail: String) : OpenCodeError("Authentication failed: $detail")
    data class Timeout(val detail: String) : OpenCodeError("Request timed out: $detail")
    data class InvalidResponse(val detail: String) : OpenCodeError("Invalid response from server: $detail")
    data class StreamDisconnected(val detail: String) : OpenCodeError("Streaming disconnected: $detail")
    data class PermissionDenied(val detail: String) : OpenCodeError("Permission denied: $detail")
    data class NotFound(val detail: String) : OpenCodeError("Not found: $detail")
    data class Unknown(val detail: String) : OpenCodeError("Unexpected error: $detail")
}
