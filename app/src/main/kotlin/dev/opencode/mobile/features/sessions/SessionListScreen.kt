package dev.opencode.mobile.features.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun SessionListScreen(
    viewModel: SessionListViewModel,
    onBack: () -> Unit,
    onOpenSession: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(OpenCodeIcons.Back, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) { Icon(OpenCodeIcons.Retry, contentDescription = "Refresh") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createSession(onCreated = onOpenSession) }) {
                Icon(OpenCodeIcons.Plus, contentDescription = "New session")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.sessions.isEmpty()) {
                Text(
                    if (state.isRefreshing) "Loading sessions…" else "No sessions yet — tap + to start one",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.sessions, key = { it.id }) { session ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSession(session.id) }
                                .padding(16.dp),
                        ) {
                            Text(session.title.ifBlank { session.id }, style = MaterialTheme.typography.bodyLarge)
                            session.agent?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        HorizontalDivider()
                    }
                }
            }
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            }
        }
    }
}
