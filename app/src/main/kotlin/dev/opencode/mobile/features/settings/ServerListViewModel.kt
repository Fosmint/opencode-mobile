package dev.opencode.mobile.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.models.ConnectionState
import dev.opencode.mobile.opencode.models.ServerProfile
import dev.opencode.mobile.opencode.repository.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServerListUiState(
    val servers: List<ServerProfile> = emptyList(),
    val activeServerId: String? = null,
    val testingServerId: String? = null,
    val testResults: Map<String, Result<ConnectionState>> = emptyMap(),
)

class ServerListViewModel(private val repository: ServerRepository) : ViewModel() {

    val uiState: StateFlow<ServerListUiState> = combine(
        repository.observeServers(),
        repository.activeServerId,
    ) { servers, activeId ->
        ServerListUiState(servers = servers, activeServerId = activeId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServerListUiState())

    private val _testResults = MutableStateFlow<Map<String, Result<ConnectionState>>>(emptyMap())

    fun selectServer(id: String) {
        viewModelScope.launch { repository.setActiveServer(id) }
    }

    fun deleteServer(id: String) {
        viewModelScope.launch { repository.deleteServer(id) }
    }

    fun testConnection(profile: ServerProfile) {
        viewModelScope.launch {
            val result = repository.testConnection(profile)
            _testResults.value = _testResults.value + (profile.id to result)
        }
    }
}
