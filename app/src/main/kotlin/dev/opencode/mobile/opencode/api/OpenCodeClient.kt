package dev.opencode.mobile.opencode.api

import dev.opencode.mobile.opencode.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Backend-agnostic contract for talking to an OpenCode server.
 *
 * Today only [dev.opencode.mobile.opencode.api.RemoteOpenCodeClient] is
 * implemented (talking HTTP/SSE to a headless `opencode serve` process).
 * [dev.opencode.mobile.opencode.api.FutureLocalOpenCodeClient] is a
 * deliberate placeholder for running the OpenCode runtime on-device
 * (directly or via Termux) — see its KDoc for why it's not implemented yet.
 *
 * All ViewModels and repositories depend on this interface only, so the
 * backend can be swapped (or a hybrid chosen at runtime per server profile)
 * without touching UI or repository code.
 */
interface OpenCodeClient {

    /** GET /api/health */
    suspend fun health(): Result<HealthResponse>

    /** GET /api/location — resolves the default location, including project info. */
    suspend fun getLocation(directory: String? = null): Result<LocationInfo>

    /** GET /api/session — list sessions, optionally scoped to a project/directory. */
    suspend fun listSessions(
        projectId: String? = null,
        directory: String? = null,
        limit: Int? = null,
    ): Result<SessionListResponse>

    /** POST /api/session — create a new session. */
    suspend fun createSession(request: SessionCreateRequest): Result<SessionInfo>

    /** GET /api/session/:sessionID */
    suspend fun getSession(sessionId: String): Result<SessionInfo>

    /** POST /api/session/:sessionID/prompt — send a message, schedules the agent loop. */
    suspend fun sendPrompt(sessionId: String, request: SessionPromptRequest): Result<SessionAdmitted>

    /** POST /api/session/:sessionID/interrupt — stop generation. */
    suspend fun interruptSession(sessionId: String): Result<Unit>

    /** GET /api/session/:sessionID/context — full active context messages. */
    suspend fun getSessionContext(sessionId: String): Result<List<SessionMessage>>

    /**
     * GET /api/session/:sessionID/event (SSE) — durable per-session event
     * stream, replayed from [after] then continued live. This is how
     * streaming assistant text, tool-call state, and reasoning updates
     * arrive.
     */
    fun subscribeToSessionEvents(sessionId: String, after: Long? = null): Flow<ServerEventEnvelope>

    /** GET /api/event (SSE) — server-wide event bus (project updates, etc). */
    fun subscribeToServerEvents(): Flow<ServerEventEnvelope>

    /** POST /api/session/:sessionID/agent */
    suspend fun switchAgent(sessionId: String, agent: String): Result<Unit>

    /** POST /api/session/:sessionID/model */
    suspend fun switchModel(sessionId: String, model: ModelRef): Result<Unit>

    /** GET /api/model */
    suspend fun listModels(directory: String? = null): Result<List<ModelInfo>>

    /**
     * GET /file — list files/directories at [path] (defaults to project
     * root when null). Confirmed against
     * `packages/opencode/src/server/routes/instance/httpapi/groups/file.ts`
     * (`FileApi`, identifier `file.list`) — there is no `/api/fs/*`
     * namespace upstream, that was an invented path in an earlier pass.
     */
    suspend fun listFiles(path: String? = null, directory: String? = null): Result<List<FileSystemEntry>>

    /**
     * GET /file/content — read a single file (identifier `file.read`).
     * Returns the raw response: `type` distinguishes text vs binary, and
     * binary content (when present) arrives base64-encoded via `encoding`
     * — this endpoint does not return a bare byte stream, so callers must
     * branch on [FileContentResponse.type] rather than assume text.
     */
    suspend fun readFile(path: String, directory: String? = null): Result<FileContentResponse>

    /** GET /file/status — git status of files in the project (identifier `file.status`). */
    suspend fun fileStatus(directory: String? = null): Result<List<FileStatusEntry>>
}
