package dev.opencode.mobile.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import dev.opencode.mobile.opencode.models.ServerProfile
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun ServerListScreen(
    viewModel: ServerListViewModel,
    onAddServer: () -> Unit,
    onEditServer: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servers") },
                actions = {
                    IconButton(onClick = onAddServer) {
                        Icon(OpenCodeIcons.Plus, contentDescription = "Add server")
                    }
                },
            )
        },
    ) { padding ->
        if (state.servers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No servers yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.padding(4.dp))
                    Button(onClick = onAddServer) { Text("Add server") }
                }
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(state.servers, key = { it.id }) { server ->
                ServerRow(
                    server = server,
                    isActive = server.id == state.activeServerId,
                    onClick = { viewModel.selectServer(server.id) },
                    onEdit = { onEditServer(server.id) },
                    onDelete = { viewModel.deleteServer(server.id) },
                    onTest = { viewModel.testConnection(server) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ServerRow(
    server: ServerProfile,
    isActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(server.name, style = MaterialTheme.typography.bodyLarge)
                if (isActive) {
                    Spacer(Modifier.padding(start = 6.dp))
                    Icon(OpenCodeIcons.Success, contentDescription = "Active", modifier = Modifier.padding(start = 4.dp))
                }
            }
            Text(server.baseUrl, style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onTest) { Icon(OpenCodeIcons.Retry, contentDescription = "Test connection") }
            IconButton(onClick = onEdit) { Icon(OpenCodeIcons.More, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(OpenCodeIcons.Trash, contentDescription = "Delete") }
        }
    }
}
