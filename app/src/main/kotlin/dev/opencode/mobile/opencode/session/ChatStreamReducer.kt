package dev.opencode.mobile.opencode.session

import dev.opencode.mobile.core.network.JsonConfig
import dev.opencode.mobile.opencode.models.EventTypes
import dev.opencode.mobile.opencode.models.ServerEventEnvelope
import dev.opencode.mobile.opencode.models.SessionMessage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Pure, testable reducer: given the current list of messages for a session
 * and one incoming [ServerEventEnvelope] from the durable session event
 * stream, returns the updated list. Kept side-effect free (no coroutines,
 * no I/O) so it can be unit tested directly against fixture JSON payloads
 * without spinning up a fake server.
 *
 * Event shapes handled:
 *  - message.updated        -> upsert the full message (covers new user
 *                               messages and coarse-grained assistant
 *                               updates, e.g. when non-streaming servers
 *                               just push the finished message once)
 *  - message.part.updated   -> patch one content part (text/reasoning/tool)
 *                               of an in-flight assistant message, which is
 *                               what produces the token-by-token streaming
 *                               feel in the UI
 *
 * Any other/unknown event type is passed through unchanged (returns the
 * input list as-is) rather than erroring, since the event manifest upstream
 * is explicitly documented as growing over time.
 */
object ChatStreamReducer {

    fun reduce(current: List<SessionMessage>, event: ServerEventEnvelope): List<SessionMessage> {
        val data = event.data ?: return current
        return when (event.type) {
            EventTypes.MESSAGE_UPDATED -> reduceMessageUpdated(current, data)
            EventTypes.MESSAGE_PART_UPDATED -> reduceMessagePartUpdated(current, data)
            else -> current
        }
    }

    private fun reduceMessageUpdated(current: List<SessionMessage>, data: JsonElement): List<SessionMessage> {
        val message = runCatching { JsonConfig.default.decodeFromJsonElement(SessionMessage.serializer(), data) }
            .getOrNull() ?: return current
        val index = current.indexOfFirst { it.id == message.id }
        return if (index >= 0) current.toMutableList().apply { this[index] = message }
        else current + message
    }

    private fun reduceMessagePartUpdated(current: List<SessionMessage>, data: JsonElement): List<SessionMessage> {
        val obj = data.jsonObject
        val messageId = obj["messageID"]?.jsonPrimitive?.content ?: return current
        val partJson = obj["part"] ?: return current
        val part = runCatching {
            JsonConfig.default.decodeFromJsonElement(
                dev.opencode.mobile.opencode.models.AssistantContentPart.serializer(),
                partJson,
            )
        }.getOrNull() ?: return current

        val index = current.indexOfFirst { it.id == messageId }
        if (index < 0) return current // part for a message we haven't seen yet; wait for message.updated

        val message = current[index]
        val contentIndex = message.content.indexOfFirst { it.id == part.id }
        val newContent = if (contentIndex >= 0) {
            message.content.toMutableList().apply { this[contentIndex] = part }
        } else {
            message.content + part
        }
        val updated = message.copy(content = newContent)
        return current.toMutableList().apply { this[index] = updated }
    }
}
