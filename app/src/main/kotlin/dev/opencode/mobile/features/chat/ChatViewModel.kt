package dev.opencode.mobile.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.models.ModelInfo
import dev.opencode.mobile.opencode.models.SessionMessage
import dev.opencode.mobile.opencode.repository.SessionRepository
import dev.opencode.mobile.opencode.session.ChatStreamReducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ChatUiState(
    val sessionId: String,
    val messages: List<SessionMessage> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val isGenerating: Boolean = false,
    val streamError: String? = null,
    val availableModels: List<ModelInfo> = emptyList(),
    val loadError: String? = null,
)

class ChatViewModel(
    private val sessionId: String,
    private val repository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState(sessionId = sessionId))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    init {
        loadInitialContext()
        observeCache()
        subscribeToStream()
        loadModels()
    }

    private fun observeCache() {
        repository.observeCachedMessages(sessionId)
            .onEach { cached ->
                // Cache is the source of truth for restart-recovery; live stream
                // events are merged in on top via the reducer as they arrive so
                // cache doesn't fight with in-flight streaming state.
                if (_state.value.messages.isEmpty() && cached.isNotEmpty()) {
                    _state.value = _state.value.copy(messages = cached)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialContext() {
        viewModelScope.launch {
            repository.refreshContext(sessionId).fold(
                onSuccess = { messages -> _state.value = _state.value.copy(messages = messages) },
                onFailure = { error -> _state.value = _state.value.copy(loadError = error.message) },
            )
        }
    }

    private fun subscribeToStream() {
        repository.sessionEvents(sessionId)
            .onEach { event ->
                val updated = ChatStreamReducer.reduce(_state.value.messages, event)
                _state.value = _state.value.copy(messages = updated, streamError = null)
                viewModelScope.launch { repository.cacheMessages(sessionId, updated) }
            }
            .catch { throwable ->
                _state.value = _state.value.copy(streamError = throwable.message ?: "Streaming disconnected")
            }
            .launchIn(viewModelScope)
    }

    private fun loadModels() {
        viewModelScope.launch {
            repository.listModels().onSuccess { models ->
                _state.value = _state.value.copy(availableModels = models)
            }
        }
    }

    fun updateDraft(text: String) {
        _state.value = _state.value.copy(draft = text)
    }

    fun sendMessage() {
        val text = _state.value.draft.trim()
        if (text.isEmpty() || _state.value.isSending) return
        _state.value = _state.value.copy(isSending = true, draft = "", streamError = null)
        viewModelScope.launch {
            repository.sendMessage(sessionId, text).fold(
                onSuccess = { _state.value = _state.value.copy(isSending = false, isGenerating = true) },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        isGenerating = false,
                        streamError = error.message ?: "Failed to send message",
                        draft = text, // restore draft so the user doesn't lose their message
                    )
                },
            )
        }
    }

    fun retrySend(previousText: String) {
        _state.value = _state.value.copy(draft = previousText)
        sendMessage()
    }

    fun stopGeneration() {
        viewModelScope.launch {
            repository.interrupt(sessionId).onSuccess {
                _state.value = _state.value.copy(isGenerating = false)
            }
        }
    }

    fun switchModel(model: ModelInfo) {
        viewModelScope.launch {
            repository.switchModel(
                sessionId,
                dev.opencode.mobile.opencode.models.ModelRef(model.providerID, model.modelID),
            )
        }
    }
}
