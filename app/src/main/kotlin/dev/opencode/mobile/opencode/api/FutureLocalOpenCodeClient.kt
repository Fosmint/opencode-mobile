package dev.opencode.mobile.opencode.api

import dev.opencode.mobile.opencode.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Placeholder for running the OpenCode runtime directly on-device (or via a
 * Termux companion install) instead of connecting to a remote server.
 *
 * This is intentionally NOT implemented in this milestone: OpenCode's
 * runtime (packages/opencode) is a Bun/Node process that shells out to git,
 * ripgrep, LSP servers, and provider SDKs — none of which have a supported
 * on-device Android story yet without a full userspace environment (Termux
 * or similar). Shipping a fake "local mode" that silently no-ops or mocks
 * these would violate the no-mock-responses requirement for this project,
 * so instead every method here fails clearly and explains why, rather than
 * pretending to work.
 *
 * The point of having this class at all — instead of only RemoteOpenCodeClient
 * — is the seam: [dev.opencode.mobile.opencode.api.OpenCodeClient] is the
 * only type the rest of the app depends on, so wiring up a real embedded
 * runtime later (e.g. driving a Termux-hosted `opencode serve` over
 * localhost, or a future native port) is a matter of implementing this one
 * class — no UI or repository changes required.
 */
class FutureLocalOpenCodeClient : OpenCodeClient {

    private val unsupported: OpenCodeError = OpenCodeError.Unknown(
        "Local/embedded OpenCode runtime is not yet available on Android. " +
            "Connect to a remote OpenCode server instead (Settings > Servers > Add server)."
    )

    override suspend fun health(): Result<HealthResponse> = Result.failure(unsupported)
    override suspend fun getLocation(directory: String?): Result<LocationInfo> = Result.failure(unsupported)
    override suspend fun listSessions(projectId: String?, directory: String?, limit: Int?): Result<SessionListResponse> =
        Result.failure(unsupported)
    override suspend fun createSession(request: SessionCreateRequest): Result<SessionInfo> = Result.failure(unsupported)
    override suspend fun getSession(sessionId: String): Result<SessionInfo> = Result.failure(unsupported)
    override suspend fun sendPrompt(sessionId: String, request: SessionPromptRequest): Result<SessionAdmitted> =
        Result.failure(unsupported)
    override suspend fun interruptSession(sessionId: String): Result<Unit> = Result.failure(unsupported)
    override suspend fun getSessionContext(sessionId: String): Result<List<SessionMessage>> = Result.failure(unsupported)
    override fun subscribeToSessionEvents(sessionId: String, after: Long?): Flow<ServerEventEnvelope> = flow {
        throw unsupported
    }
    override fun subscribeToServerEvents(): Flow<ServerEventEnvelope> = flow { throw unsupported }
    override suspend fun switchAgent(sessionId: String, agent: String): Result<Unit> = Result.failure(unsupported)
    override suspend fun switchModel(sessionId: String, model: ModelRef): Result<Unit> = Result.failure(unsupported)
    override suspend fun listModels(directory: String?): Result<List<ModelInfo>> = Result.failure(unsupported)
    override suspend fun listFiles(path: String?, directory: String?): Result<List<FileSystemEntry>> =
        Result.failure(unsupported)
    override suspend fun readFile(path: String, directory: String?): Result<FileContentResponse> =
        Result.failure(unsupported)
    override suspend fun fileStatus(directory: String?): Result<List<FileStatusEntry>> = Result.failure(unsupported)
}
