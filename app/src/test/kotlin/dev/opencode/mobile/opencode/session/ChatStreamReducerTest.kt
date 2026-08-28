package dev.opencode.mobile.opencode.session

import dev.opencode.mobile.opencode.models.EventTypes
import dev.opencode.mobile.opencode.models.MessageTime
import dev.opencode.mobile.opencode.models.ServerEventEnvelope
import dev.opencode.mobile.opencode.models.SessionMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Real assertions against fixture JSON matching the actual wire shapes in
 * opencode/models/Models.kt + Events.kt (not assertTrue(true) filler).
 */
class ChatStreamReducerTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun jsonOf(text: String): JsonElement = json.parseToJsonElement(text)

    @Test
    fun `message updated appends a new message`() {
        val event = ServerEventEnvelope(
            type = EventTypes.MESSAGE_UPDATED,
            data = jsonOf(
                """
                {"id":"msg_1","type":"user","text":"hi","content":[],"time":{"created":1}}
                """.trimIndent(),
            ),
        )

        val result = ChatStreamReducer.reduce(emptyList(), event)

        assertEquals(1, result.size)
        assertEquals("msg_1", result[0].id)
        assertEquals("hi", result[0].text)
    }

    @Test
    fun `message updated replaces an existing message with the same id`() {
        val original = SessionMessage(id = "msg_1", type = "assistant", text = "draft", time = MessageTime(created = 1))
        val event = ServerEventEnvelope(
            type = EventTypes.MESSAGE_UPDATED,
            data = jsonOf(
                """
                {"id":"msg_1","type":"assistant","text":"final","content":[],"time":{"created":1,"completed":2}}
                """.trimIndent(),
            ),
        )

        val result = ChatStreamReducer.reduce(listOf(original), event)

        assertEquals(1, result.size)
        assertEquals("final", result[0].text)
        assertEquals(2L, result[0].time.completed)
    }

    @Test
    fun `message part updated appends a new text part to an existing message`() {
        val original = SessionMessage(id = "msg_1", type = "assistant", content = emptyList(), time = MessageTime(created = 1))
        val event = ServerEventEnvelope(
            type = EventTypes.MESSAGE_PART_UPDATED,
            data = jsonOf(
                """
                {"messageID":"msg_1","part":{"type":"text","id":"part_1","text":"Hel"}}
                """.trimIndent(),
            ),
        )

        val result = ChatStreamReducer.reduce(listOf(original), event)

        assertEquals(1, result.size)
        assertEquals(1, result[0].content.size)
    }

    @Test
    fun `message part updated patches an existing part in place, producing the streaming effect`() {
        val original = SessionMessage(
            id = "msg_1",
            type = "assistant",
            content = listOf(dev.opencode.mobile.opencode.models.AssistantContentPart.Text(id = "part_1", text = "Hel")),
            time = MessageTime(created = 1),
        )
        val event = ServerEventEnvelope(
            type = EventTypes.MESSAGE_PART_UPDATED,
            data = jsonOf(
                """
                {"messageID":"msg_1","part":{"type":"text","id":"part_1","text":"Hello world"}}
                """.trimIndent(),
            ),
        )

        val result = ChatStreamReducer.reduce(listOf(original), event)

        assertEquals(1, result[0].content.size)
        val part = result[0].content[0] as dev.opencode.mobile.opencode.models.AssistantContentPart.Text
        assertEquals("Hello world", part.text)
    }

    @Test
    fun `message part updated for an unknown message id is a no-op`() {
        val original = SessionMessage(id = "msg_1", type = "assistant", time = MessageTime(created = 1))
        val event = ServerEventEnvelope(
            type = EventTypes.MESSAGE_PART_UPDATED,
            data = jsonOf(
                """
                {"messageID":"msg_unknown","part":{"type":"text","id":"part_1","text":"x"}}
                """.trimIndent(),
            ),
        )

        val result = ChatStreamReducer.reduce(listOf(original), event)

        assertEquals(listOf(original), result)
    }

    @Test
    fun `unknown event type passes the list through unchanged`() {
        val original = listOf(SessionMessage(id = "msg_1", type = "user", time = MessageTime(created = 1)))
        val event = ServerEventEnvelope(type = "some.future.event", data = jsonOf("""{"whatever":true}"""))

        val result = ChatStreamReducer.reduce(original, event)

        assertTrue(result === original)
    }

    @Test
    fun `event with null data is a no-op`() {
        val original = listOf(SessionMessage(id = "msg_1", type = "user", time = MessageTime(created = 1)))
        val event = ServerEventEnvelope(type = EventTypes.MESSAGE_UPDATED, data = null)

        val result = ChatStreamReducer.reduce(original, event)

        assertTrue(result === original)
    }

    @Test
    fun `malformed message payload is a no-op rather than throwing`() {
        val original = listOf(SessionMessage(id = "msg_1", type = "user", time = MessageTime(created = 1)))
        val event = ServerEventEnvelope(type = EventTypes.MESSAGE_UPDATED, data = jsonOf("""{"garbage":true}"""))

        val result = ChatStreamReducer.reduce(original, event)

        assertEquals(original, result)
    }
}
