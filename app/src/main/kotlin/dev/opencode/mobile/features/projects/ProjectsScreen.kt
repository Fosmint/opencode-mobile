package dev.opencode.mobile.features.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * See [ProjectsViewModel] KDoc: there is no upstream project-list endpoint,
 * so this screen shows the current server location's project (always
 * resolvable via GET /api/location) as an entry point into its sessions.
 * A multi-project switcher would need either a directory picker (client
 * asks the server to resolve a different `location.directory`) or
 * consuming `project.updated` off the server-wide event stream to learn
 * about additional projects over time — both are documented as follow-up
 * work rather than implemented against a guessed endpoint.
 */
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    onOpenSessions: (projectId: String) -> Unit,
    onOpenFiles: (directory: String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Projects") }) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                state.currentProject != null -> {
                    val project = state.currentProject!!
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSessions(project.id) }
                                .padding(16.dp),
                        ) {
                            Text(project.name ?: project.worktree, style = MaterialTheme.typography.titleMedium)
                            Text(project.worktree, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "Browse files",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { onOpenFiles(project.worktree) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                else -> Text(
                    "No project resolved for this server",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
        }
    }
}
