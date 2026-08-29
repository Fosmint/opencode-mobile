package dev.opencode.mobile.opencode.api

import dev.opencode.mobile.core.network.JsonConfig
import dev.opencode.mobile.opencode.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Talks to a real, running `opencode serve` process over HTTP + SSE.
 *
 * Endpoints implemented here were verified against the upstream server
 * route definitions in
 * packages/opencode/src/server/routes/instance/httpapi/groups
 * (session.ts, health.ts, event.ts, model.ts, file.ts, location.ts) rather
 * than assumed — see the project README for the exact commit this was
 * checked against. Note the file group lives at `/file` and `/file/content`
 * (identifiers `file.list`/`file.read`), not `/api/fs` (glob) — that path was
 * invented in an earlier pass and corrected once real sources were
 * available. If a future OpenCode server version renames or restructures
 * these routes, this is the only file that needs to change;
 * [OpenCodeClient] and every caller above it stays the same.
 */
class RemoteOpenCodeClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient,
    private val json: Json = JsonConfig.default,
) : OpenCodeClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun url(path: String, query: Map<String, String?> = emptyMap()): String {
        val builder = (baseUrl.trimEnd('/') + path).toHttpUrlOrNull()
            ?.newBuilder()
            ?: throw OpenCodeError.InvalidResponse("Malformed base URL: $baseUrl")
        query.forEach { (key, value) -> if (value != null) builder.addQueryParameter(key, value) }
        return builder.build().toString()
    }

    private suspend fun execute(request: Request): Response = suspendCancellableCoroutine { cont ->
        val call = httpClient.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                cont.resumeWithException(mapIoException(e))
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                cont.resume(response)
            }
        })
    }

    private fun mapIoException(e: IOException): OpenCodeError = when {
        e is java.net.SocketTimeoutException -> OpenCodeError.Timeout(e.message ?: "timed out")
        e is java.net.ConnectException || e is java.net.UnknownHostException ->
            OpenCodeError.ConnectionFailed(e.message ?: "could not reach server")
        else -> OpenCodeError.Unknown(e.message ?: e.toString())
    }

    private fun mapHttpError(response: Response): OpenCodeError {
        val body = try { response.body?.string() } catch (_: Exception) { null }
        return when (response.code) {
            401, 403 -> OpenCodeError.AuthenticationFailed(body ?: "HTTP ${response.code}")
            404 -> OpenCodeError.NotFound(body ?: "HTTP ${response.code}")
            503 -> OpenCodeError.ServerUnavailable(body ?: "HTTP ${response.code}")
            else -> OpenCodeError.InvalidResponse("HTTP ${response.code}: ${body ?: "no body"}")
        }
    }

    private suspend inline fun <reified T> get(path: String, query: Map<String, String?> = emptyMap()): Result<T> {
        return try {
            val request = Request.Builder().url(url(path, query)).get().build()
            val response = execute(request)
            response.use {
                if (!it.isSuccessful) return Result.failure(mapHttpError(it))
                val bodyString = it.body?.string() ?: return Result.failure(
                    OpenCodeError.InvalidResponse("Empty response body")
                )
                Result.success(json.decodeFromString(bodyString))
            }
        } catch (e: OpenCodeError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(OpenCodeError.Unknown(e.message ?: e.toString()))
        }
    }

    private suspend inline fun <reified B, reified T> post(path: String, body: B): Result<T> {
        return try {
            val payload = json.encodeToString<B>(body)
            val request = Request.Builder()
                .url(url(path))
                .post(payload.toRequestBody(jsonMediaType))
                .build()
            val response = execute(request)
            response.use {
                if (!it.isSuccessful) return Result.failure(mapHttpError(it))
                val bodyString = it.body?.string()
                if (bodyString.isNullOrBlank()) {
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(Unit as T)
                }
                Result.success(json.decodeFromString(bodyString))
            }
        } catch (e: OpenCodeError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(OpenCodeError.Unknown(e.message ?: e.toString()))
        }
    }

    private suspend fun postNoBody(path: String): Result<Unit> {
        return try {
            val request = Request.Builder()
                .url(url(path))
                .post("".toRequestBody(jsonMediaType))
                .build()
            val response = execute(request)
            response.use {
                if (!it.isSuccessful) return Result.failure(mapHttpError(it))
                Result.success(Unit)
            }
        } catch (e: OpenCodeError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(OpenCodeError.Unknown(e.message ?: e.toString()))
        }
    }

    override suspend fun health(): Result<HealthResponse> = get("/api/health")

    override suspend fun getLocation(directory: String?): Result<LocationInfo> =
        get("/api/location", mapOf("location.directory" to directory))

    override suspend fun listSessions(
        projectId: String?,
        directory: String?,
        limit: Int?,
    ): Result<SessionListResponse> = get(
        "/api/session",
        mapOf(
            "project" to projectId,
            "directory" to directory,
            "limit" to limit?.toString(),
        ),
    )

    override suspend fun createSession(request: SessionCreateRequest): Result<SessionInfo> {
        val result = post<SessionCreateRequest, SessionCreateResponse>("/api/session", request)
        return result.map { it.data }
    }

    override suspend fun getSession(sessionId: String): Result<SessionInfo> {
        val result = get<SessionGetResponse>("/api/session/$sessionId")
        return result.map { it.data }
    }

    override suspend fun sendPrompt(
        sessionId: String,
        request: SessionPromptRequest,
    ): Result<SessionAdmitted> = post("/api/session/$sessionId/prompt", request)

    override suspend fun interruptSession(sessionId: String): Result<Unit> =
        postNoBody("/api/session/$sessionId/interrupt")

    override suspend fun getSessionContext(sessionId: String): Result<List<SessionMessage>> {
        val result = get<SessionContextResponse>("/api/session/$sessionId/context")
        return result.map { it.data }
    }

    override fun subscribeToSessionEvents(sessionId: String, after: Long?): Flow<ServerEventEnvelope> =
        sseFlow(url("/api/session/$sessionId/event", mapOf("after" to after?.toString())))

    override fun subscribeToServerEvents(): Flow<ServerEventEnvelope> =
        sseFlow(url("/api/event"))

    private fun sseFlow(fullUrl: String): Flow<ServerEventEnvelope> = callbackFlow {
        val request = Request.Builder().url(fullUrl).build()
        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val envelope = json.decodeFromString<ServerEventEnvelope>(data)
                    trySend(envelope)
                } catch (e: Exception) {
                    // Malformed/unknown event shape: don't crash the stream, just drop this event.
                    // A future schema addition upstream should not take down the whole session view.
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(OpenCodeError.StreamDisconnected(t?.message ?: response?.message ?: "SSE closed"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }
        val factory = EventSources.createFactory(httpClient)
        val source = factory.newEventSource(request, listener)
        awaitClose { source.cancel() }
    }

    override suspend fun switchAgent(sessionId: String, agent: String): Result<Unit> =
        post("/api/session/$sessionId/agent", mapOf("agent" to agent))

    override suspend fun switchModel(sessionId: String, model: ModelRef): Result<Unit> =
        post("/api/session/$sessionId/model", mapOf("model" to model))

    override suspend fun listModels(directory: String?): Result<List<ModelInfo>> {
        val result = get<ModelListWireResponse>("/api/model", mapOf("location.directory" to directory))
        return result.map { it.data }
    }

    // Real endpoints per `FileApi` (file.ts): GET /file (list), GET /file/content
    // (read), GET /file/status (git status). Both list/content take a plain
    // `path`/`directory` query, no `/api/fs/*` namespace and no `location.`
    // prefix — those were invented in an earlier pass and never matched a
    // real server route.

    override suspend fun listFiles(path: String?, directory: String?): Result<List<FileSystemEntry>> {
        return get("/file", mapOf("path" to (path ?: ""), "directory" to directory))
    }

    override suspend fun readFile(path: String, directory: String?): Result<FileContentResponse> {
        return get("/file/content", mapOf("path" to path, "directory" to directory))
    }

    override suspend fun fileStatus(directory: String?): Result<List<FileStatusEntry>> {
        return get("/file/status", mapOf("directory" to directory))
    }
}

/**
 * The `/api/model` endpoint wraps its array in Location.response(...), which
 * on the wire is `{ location: ..., data: [...] }`. We only need `data`.
 */
@kotlinx.serialization.Serializable
private data class ModelListWireResponse(
    val data: List<ModelInfo> = emptyList(),
)
