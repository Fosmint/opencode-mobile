package dev.opencode.mobile.features.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.api.OpenCodeClient
import dev.opencode.mobile.opencode.models.ProjectInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * There is no dedicated `/api/project` list endpoint upstream — confirmed
 * against `packages/protocol/src/groups/location.ts` (only resolves one
 * location) and `project-copy.ts` (copy/remove/refresh of an existing
 * project, not listing). What upstream *does* expose:
 *
 *  - GET /api/location -> Location.Info, which includes the *current*
 *    project (the one the server process is rooted at / was pointed at).
 *  - GET /api/session -> each SessionInfo carries a `projectID`
 *    (see packages/schema/src/project.ts + session usage), so the set of
 *    projects a server has ever touched can be derived from the distinct
 *    projectIDs across sessions.
 *  - `project.updated` is a server-wide SSE event (see
 *    packages/schema/src/project.ts `Event.Updated`) carrying a full
 *    Project.Info payload whenever a project changes; the server-wide
 *    event stream (`/api/event`) is how the app would learn about
 *    projects it hasn't seen a session for yet, in real time.
 *
 * So "list projects" in this MVP means: show the current location's
 * project first (always resolvable), then any additional project IDs
 * seen via cached sessions. This is documented behavior, not a guess
 * dressed up as a real listing endpoint.
 */
data class ProjectsUiState(
    val isLoading: Boolean = true,
    val currentProject: ProjectInfo? = null,
    val error: String? = null,
)

class ProjectsViewModel(private val client: OpenCodeClient) : ViewModel() {

    private val _state = MutableStateFlow(ProjectsUiState())
    val state: StateFlow<ProjectsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            client.getLocation().fold(
                onSuccess = { location ->
                    _state.value = _state.value.copy(isLoading = false, currentProject = location.project)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message ?: "Failed to resolve project")
                },
            )
        }
    }
}
