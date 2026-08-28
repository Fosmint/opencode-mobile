package dev.opencode.mobile.features.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.opencode.mobile.opencode.api.OpenCodeClient
import dev.opencode.mobile.opencode.models.FileContentResponse
import dev.opencode.mobile.opencode.models.FileSystemEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Read-only file browser backed directly by the live server filesystem —
 * `GET /file` for directory listings, `GET /file/content` for a single
 * file. There's no local cache here (unlike sessions/messages): showing a
 * stale directory listing or stale file body would be actively misleading
 * for something whose entire value is "what's on disk right now", so this
 * always goes straight to the client.
 *
 * [directory] is the project's `worktree` (see [dev.opencode.mobile.opencode.models.ProjectInfo]),
 * not a `projectId` — the file endpoints route by directory/workspace, not
 * by project id.
 */
data class FilesUiState(
    val currentPath: String = "",
    val entries: List<FileSystemEntry> = emptyList(),
    val isLoadingList: Boolean = true,
    val listError: String? = null,
    // Non-null while a file is open for preview.
    val openFile: OpenFile? = null,
)

data class OpenFile(
    val entry: FileSystemEntry,
    val isLoading: Boolean = true,
    val content: FileContentResponse? = null,
    val error: String? = null,
)

class FilesViewModel(
    private val client: OpenCodeClient,
    private val directory: String,
) : ViewModel() {

    private val _state = MutableStateFlow(FilesUiState())
    val state: StateFlow<FilesUiState> = _state.asStateFlow()

    // Directory breadcrumb stack; "" is project root. Used for back navigation
    // without re-deriving parent paths from string splitting on every entry.
    private val pathStack = ArrayDeque<String>().apply { addLast("") }

    init {
        loadCurrentPath()
    }

    private fun loadCurrentPath() {
        val path = pathStack.last()
        viewModelScope.launch {
            _state.value = _state.value.copy(currentPath = path, isLoadingList = true, listError = null)
            client.listFiles(path = path.ifBlank { null }, directory = directory).fold(
                onSuccess = { entries ->
                    _state.value = _state.value.copy(
                        entries = entries.sortedWith(
                            compareByDescending<FileSystemEntry> { it.type == "directory" }.thenBy { it.name.lowercase() },
                        ),
                        isLoadingList = false,
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoadingList = false,
                        listError = error.message ?: "Failed to list files",
                    )
                },
            )
        }
    }

    fun refresh() = loadCurrentPath()

    fun onEntryClicked(entry: FileSystemEntry) {
        if (entry.type == "directory") {
            pathStack.addLast(entry.path)
            loadCurrentPath()
        } else {
            openFile(entry)
        }
    }

    /** True if consumed (i.e. we were in a subdirectory), false if already at root. */
    fun navigateUp(): Boolean {
        if (pathStack.size <= 1) return false
        pathStack.removeLast()
        loadCurrentPath()
        return true
    }

    private fun openFile(entry: FileSystemEntry) {
        _state.value = _state.value.copy(openFile = OpenFile(entry = entry, isLoading = true))
        viewModelScope.launch {
            client.readFile(path = entry.path, directory = directory).fold(
                onSuccess = { content ->
                    val current = _state.value.openFile ?: return@fold
                    _state.value = _state.value.copy(
                        openFile = current.copy(isLoading = false, content = content),
                    )
                },
                onFailure = { error ->
                    val current = _state.value.openFile ?: return@fold
                    _state.value = _state.value.copy(
                        openFile = current.copy(isLoading = false, error = error.message ?: "Failed to read file"),
                    )
                },
            )
        }
    }

    fun closeFile() {
        _state.value = _state.value.copy(openFile = null)
    }
}
