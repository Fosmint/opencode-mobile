package dev.opencode.mobile.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.opencode.models.ServerProfile
import dev.opencode.mobile.ui.icons.OpenCodeIcons

/**
 * Home shows the active server's status and a shortcut into Projects, or —
 * if no server is configured yet — a prompt to add one in Settings. This
 * intentionally does not duplicate the full server list UI (that lives in
 * Settings); Home is an at-a-glance / entry-point screen.
 */
@Composable
fun HomeScreen(
    servers: List<ServerProfile>,
    activeServerId: String?,
    onOpenProjects: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val activeServer = servers.firstOrNull { it.id == activeServerId }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = OpenCodeIcons.AgentBot,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        if (activeServer == null) {
            Text("No server connected", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add an OpenCode server to get started.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = onOpenSettings) { Text("Add server") }
        } else {
            Text(activeServer.name, style = MaterialTheme.typography.titleMedium)
            Text(
                activeServer.baseUrl,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )
            Button(onClick = onOpenProjects) { Text("Open project") }
        }
    }
}
