package dev.opencode.mobile.opencode.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generic envelope for events coming off the SSE streams:
 *   GET /api/event                     (server-wide event bus)
 *   GET /api/session/:sessionID/event  (durable per-session events)
 *
 * The upstream schema (EventManifest.ServerDefinitions / SessionEvent.Durable)
 * is a large discriminated union that keeps growing as OpenCode adds event
 * types. Rather than hardcode every variant (and silently drop unknown ones
 * the moment upstream adds a new tool or event kind), we parse the envelope
 * generically and let call sites pattern-match on [type] and pull out the
 * pieces of [data] they understand. This is the same defensive approach the
 * TUI/web clients take against an evolving event stream.
 */
@Serializable
data class ServerEventEnvelope(
    val id: String? = null,
    val type: String,
    val data: JsonElement? = null,
    val durable: DurableRef? = null,
)

@Serializable
data class DurableRef(
    val aggregateID: String,
    val seq: Long,
    val version: Long,
)

/** Known event type discriminators we actively handle in the UI. */
object EventTypes {
    const val SERVER_CONNECTED = "server.connected"
    const val SESSION_UPDATED = "session.updated"
    const val MESSAGE_UPDATED = "message.updated"
    const val MESSAGE_PART_UPDATED = "message.part.updated"
    const val PERMISSION_ASKED = "permission.asked"
    const val PERMISSION_RESOLVED = "permission.resolved"
    const val PROJECT_UPDATED = "project.updated"
}
