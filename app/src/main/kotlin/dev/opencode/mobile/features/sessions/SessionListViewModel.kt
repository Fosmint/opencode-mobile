package dev.opencode.mobile.features.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.models.ModelRef
import dev.opencode.mobile.opencode.models.SessionInfo
import dev.opencode.mobile.opencode.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionListUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val creating: Boolean = false,
)

class SessionListViewModel(
    private val projectId: String,
    private val repository: SessionRepository,
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _creating = MutableStateFlow(false)

    val state: StateFlow<SessionListUiState> = combine(
        repository.observeCachedSessions(projectId),
        _refreshing,
        _error,
        _creating,
    ) { sessions, refreshing, error, creating ->
        SessionListUiState(
            sessions = sessions.sortedByDescending { it.time.updated },
            isRefreshing = refreshing,
            error = error,
            creating = creating,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionListUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            repository.refreshSessions(projectId).onFailure { _error.value = it.message }
            _refreshing.value = false
        }
    }

    /** Returns the created session id via [onCreated] so the caller can navigate to chat. */
    fun createSession(agent: String? = null, model: ModelRef? = null, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _creating.value = true
            repository.createSession(projectId = projectId, agent = agent, model = model).fold(
                onSuccess = { session -> onCreated(session.id) },
                onFailure = { _error.value = it.message },
            )
            _creating.value = false
        }
    }
}
