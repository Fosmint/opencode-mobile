package dev.opencode.mobile.core.network

import kotlinx.serialization.json.Json

object JsonConfig {
    val default: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
        encodeDefaults = true
    }
}
