package dev.opencode.mobile.features.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat", style = MaterialTheme.typography.titleMedium)
                        val model = state.availableModels.firstOrNull()
                        if (model != null) {
                            Text(model.modelID, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(OpenCodeIcons.Back, contentDescription = "Back") }
                },
                actions = {
                    if (state.isGenerating) {
                        IconButton(onClick = viewModel::stopGeneration) {
                            Icon(OpenCodeIcons.Stop, contentDescription = "Stop generating")
                        }
                    }
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.imePadding().padding(8.dp)) {
                state.streamError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 4.dp))
                }
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.draft,
                        onValueChange = viewModel::updateDraft,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message OpenCode…") },
                    )
                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = state.draft.isNotBlank() && !state.isSending,
                    ) {
                        Icon(OpenCodeIcons.Send, contentDescription = "Send")
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                items(state.messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }
            }
            state.loadError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                )
            }
        }
    }
}
