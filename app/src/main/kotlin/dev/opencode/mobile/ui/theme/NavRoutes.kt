package dev.opencode.mobile.ui.theme

import java.net.URLDecoder
import java.net.URLEncoder

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object Projects : NavRoute("projects")
    data object Sessions : NavRoute("sessions/{projectId}") {
        fun create(projectId: String) = "sessions/$projectId"
    }
    data object Chat : NavRoute("chat/{sessionId}") {
        fun create(sessionId: String) = "chat/$sessionId"
    }

    /**
     * Argument is the project's `worktree` directory (absolute filesystem
     * path), not a project id — the file endpoints (`/file`, `/file/content`)
     * route by `directory`/`workspace`, not by project id, so that's what
     * [dev.opencode.mobile.features.files.FilesViewModel] actually needs.
     * URL-encoded because a worktree path contains `/`.
     */
    data object Files : NavRoute("files/{directory}") {
        fun create(directory: String) = "files/${URLEncoder.encode(directory, "UTF-8")}"
        fun decode(encoded: String): String = URLDecoder.decode(encoded, "UTF-8")
    }
    data object Settings : NavRoute("settings")
    data object AddServer : NavRoute("settings/add-server?serverId={serverId}") {
        fun create(serverId: String? = null) = "settings/add-server?serverId=${serverId ?: ""}"
    }
}
