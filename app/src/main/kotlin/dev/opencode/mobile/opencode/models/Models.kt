package dev.opencode.mobile.opencode.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * These models mirror the OpenCode server protocol as defined upstream in
 * packages/protocol/src/groups/*.ts and packages/schema/src/*.ts.
 *
 * Field names and shapes are intentionally kept close to the wire format
 * (snake/camel as emitted by the server) rather than "Android-ified", so
 * that diffs against the upstream OpenAPI spec stay easy to review.
 */

@Serializable
data class LocationRef(
    val directory: String? = null,
    val workspace: String? = null,
)

@Serializable
data class LocationInfo(
    val directory: String,
    val workspace: String? = null,
    val project: ProjectInfo? = null,
)

@Serializable
data class ProjectIcon(
    val url: String? = null,
    val override: String? = null,
    val color: String? = null,
)

@Serializable
data class ProjectCommands(
    val start: String? = null,
)

@Serializable
data class ProjectTime(
    val created: Long,
    val updated: Long,
    val initialized: Long? = null,
)

@Serializable
data class ProjectInfo(
    val id: String,
    val worktree: String,
    val vcs: String? = null,
    val name: String? = null,
    val icon: ProjectIcon? = null,
    val commands: ProjectCommands? = null,
    val time: ProjectTime,
    val sandboxes: List<String> = emptyList(),
)

@Serializable
data class SessionTokens(
    val input: Double = 0.0,
    val output: Double = 0.0,
    val reasoning: Double = 0.0,
    val cache: SessionCache = SessionCache(),
)

@Serializable
data class SessionCache(
    val read: Double = 0.0,
    val write: Double = 0.0,
)

@Serializable
data class SessionTime(
    val created: Long,
    val updated: Long,
    val archived: Long? = null,
)

@Serializable
data class ModelRef(
    val providerID: String,
    val modelID: String,
)

@Serializable
data class SessionInfo(
    val id: String,
    val parentID: String? = null,
    val projectID: String,
    val agent: String? = null,
    val model: ModelRef? = null,
    val cost: Double = 0.0,
    val tokens: SessionTokens = SessionTokens(),
    val time: SessionTime,
    val title: String,
    val location: LocationRef,
    val subpath: String? = null,
)

@Serializable
data class SessionListResponse(
    val data: List<SessionInfo>,
    val cursor: SessionCursor = SessionCursor(),
)

@Serializable
data class SessionCursor(
    val previous: String? = null,
    val next: String? = null,
)

@Serializable
data class SessionCreateRequest(
    val id: String? = null,
    val agent: String? = null,
    val model: ModelRef? = null,
    val location: LocationRef? = null,
)

@Serializable
data class SessionCreateResponse(val data: SessionInfo)

@Serializable
data class SessionGetResponse(val data: SessionInfo)

@Serializable
data class PromptFile(
    val filename: String? = null,
    val mime: String? = null,
    val url: String? = null,
)

@Serializable
data class PromptInput(
    val text: String,
    val files: List<PromptFile> = emptyList(),
)

@Serializable
data class SessionPromptRequest(
    val id: String? = null,
    val prompt: PromptInput,
    val resume: Boolean? = null,
)

@Serializable
data class SessionAdmitted(val data: SessionAdmittedData)

@Serializable
data class SessionAdmittedData(
    val id: String? = null,
    val sessionID: String? = null,
)

@Serializable
data class ModelInfo(
    val providerID: String,
    val modelID: String,
    val name: String? = null,
    val releaseDate: String? = null,
)

@Serializable
data class ModelListResponse(
    val data: List<ModelInfo> = emptyList(),
)

// ---- Message content (assistant streaming parts) ----

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class AssistantContentPart {
    abstract val id: String

    @Serializable
    @SerialName("text")
    data class Text(override val id: String, val text: String) : AssistantContentPart()

    @Serializable
    @SerialName("reasoning")
    data class Reasoning(override val id: String, val text: String) : AssistantContentPart()

    @Serializable
    @SerialName("tool")
    data class Tool(
        override val id: String,
        val name: String,
        val state: ToolState,
    ) : AssistantContentPart()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("status")
sealed class ToolState {
    @Serializable
    @SerialName("pending")
    data class Pending(val input: String = "") : ToolState()

    @Serializable
    @SerialName("running")
    data class Running(
        val input: Map<String, String> = emptyMap(),
    ) : ToolState()

    @Serializable
    @SerialName("completed")
    data class Completed(
        val input: Map<String, String> = emptyMap(),
    ) : ToolState()

    @Serializable
    @SerialName("error")
    data class Error(
        val input: Map<String, String> = emptyMap(),
        val error: ToolError? = null,
    ) : ToolState()
}

@Serializable
data class ToolError(val type: String = "unknown", val message: String = "")

@Serializable
data class MessageTime(
    val created: Long,
    val completed: Long? = null,
)

/**
 * Session message envelope — a subset of the discriminated union defined in
 * session-message.ts (user / assistant / system / agent-switched / etc).
 * Only "user" and "assistant" are modeled in full detail; other types are
 * captured generically so the UI can at least render a label instead of
 * silently dropping the event.
 */
@Serializable
data class SessionMessage(
    val id: String,
    val type: String,
    val text: String? = null,
    val agent: String? = null,
    val model: ModelRef? = null,
    val content: List<AssistantContentPart> = emptyList(),
    val cost: Double? = null,
    val tokens: SessionTokens? = null,
    val time: MessageTime,
)

@Serializable
data class SessionContextResponse(val data: List<SessionMessage>)

@Serializable
data class HealthResponse(val healthy: Boolean)

// ---- Filesystem ----
//
// Wire format confirmed against upstream OpenCode source:
// packages/opencode/src/server/routes/instance/httpapi/groups/file.ts
// (Schema identifiers `FileNode` / `FileContent`). There is no `/api/fs/*`
// namespace — the real endpoints are `GET /file` (list) and
// `GET /file/content` (read), both taking `path` + optional `directory`
// query params, no wrapping `{ data: [...] }` envelope like /api/model has.

@Serializable
data class FileSystemEntry(
    val name: String,
    val path: String,
    val absolute: String,
    val type: String, // "file" | "directory"
    val ignored: Boolean = false,
)

/**
 * Response of `GET /file/content`. `type` is "text" or "binary".
 * For text files, `content` holds the raw text. For binary files, `content`
 * is empty/irrelevant and the real bytes come base64-encoded when
 * `encoding == "base64"` — callers should check `type`/`encoding` before
 * assuming `content` is displayable text.
 */
@Serializable
data class FileContentResponse(
    val type: String, // "text" | "binary"
    val content: String,
    val encoding: String? = null, // "base64" when content is binary-in-text form
    val mimeType: String? = null,
)

@Serializable
data class FileStatusEntry(
    val path: String,
    val added: Int,
    val removed: Int,
    val status: String, // "added" | "deleted" | "modified"
)
