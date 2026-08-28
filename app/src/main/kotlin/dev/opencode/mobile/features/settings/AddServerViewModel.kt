package dev.opencode.mobile.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.models.ConnectionState
import dev.opencode.mobile.opencode.models.ServerProfile
import dev.opencode.mobile.opencode.repository.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddServerFormState(
    val name: String = "",
    val baseUrl: String = "",
    val useBasicAuth: Boolean = false,
    val username: String = "",
    val password: String = "",
    val isTesting: Boolean = false,
    val testResult: ConnectionState? = null,
    val testErrorMessage: String? = null,
    val isSaving: Boolean = false,
    val validationError: String? = null,
    val saved: Boolean = false,
)

class AddServerViewModel(
    private val repository: ServerRepository,
    private val existingServerId: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(AddServerFormState())
    val state: StateFlow<AddServerFormState> = _state.asStateFlow()

    fun updateName(value: String) { _state.value = _state.value.copy(name = value) }
    fun updateBaseUrl(value: String) { _state.value = _state.value.copy(baseUrl = value, testResult = null) }
    fun updateUseBasicAuth(value: Boolean) { _state.value = _state.value.copy(useBasicAuth = value) }
    fun updateUsername(value: String) { _state.value = _state.value.copy(username = value) }
    fun updatePassword(value: String) { _state.value = _state.value.copy(password = value) }

    private fun buildDraftProfile(): ServerProfile? {
        val s = _state.value
        val normalizedUrl = s.baseUrl.trim()
        if (normalizedUrl.isBlank()) return null
        val hasScheme = normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")
        val finalUrl = if (hasScheme) normalizedUrl else "http://$normalizedUrl"
        return ServerProfile(
            id = existingServerId ?: "draft",
            name = s.name.ifBlank { finalUrl },
            baseUrl = finalUrl,
            useBasicAuth = s.useBasicAuth,
            username = s.username.ifBlank { null },
        )
    }

    fun testConnection() {
        val profile = buildDraftProfile() ?: run {
            _state.value = _state.value.copy(validationError = "Enter a server address first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isTesting = true, testResult = null, testErrorMessage = null, validationError = null)
            val result = repository.testConnection(profile)
            result.fold(
                onSuccess = { connState ->
                    _state.value = _state.value.copy(isTesting = false, testResult = connState)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isTesting = false,
                        testResult = ConnectionState.ERROR,
                        testErrorMessage = error.message,
                    )
                },
            )
        }
    }

    fun save() {
        val s = _state.value
        val normalizedUrl = s.baseUrl.trim()
        if (normalizedUrl.isBlank()) {
            _state.value = s.copy(validationError = "Server address is required")
            return
        }
        val hasScheme = normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")
        val finalUrl = if (hasScheme) normalizedUrl else "http://$normalizedUrl"

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, validationError = null)
            repository.addOrUpdateServer(
                id = existingServerId,
                name = s.name.ifBlank { finalUrl },
                baseUrl = finalUrl,
                useBasicAuth = s.useBasicAuth,
                username = s.username.ifBlank { null },
                password = s.password.ifBlank { null },
            )
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
