package dev.opencode.mobile.features.files

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.opencode.models.FileSystemEntry
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.currentPath.isBlank()) "Files" else "/${state.currentPath}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.navigateUp()) onBack() }) {
                        Icon(OpenCodeIcons.Back, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(OpenCodeIcons.Retry, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoadingList -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.listError != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                ) {
                    Icon(OpenCodeIcons.ErrorIcon, contentDescription = null)
                    Text(state.listError ?: "", color = MaterialTheme.colorScheme.error)
                }
                state.entries.isEmpty() -> Text(
                    "Empty directory",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.entries, key = { it.path }) { entry ->
                        FileRow(entry = entry, onClick = { viewModel.onEntryClicked(entry) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    state.openFile?.let { openFile ->
        FilePreviewSheet(openFile = openFile, onDismiss = viewModel::closeFile)
    }
}

@Composable
private fun FileRow(entry: FileSystemEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (entry.type == "directory") OpenCodeIcons.Folder else OpenCodeIcons.File,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (entry.ignored) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        Box(modifier = Modifier.width(12.dp))
        Text(
            entry.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (entry.ignored) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FilePreviewSheet(openFile: OpenFile, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text(
                openFile.entry.path,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            when {
                openFile.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                openFile.error != null -> Text(openFile.error, color = MaterialTheme.colorScheme.error)
                openFile.content == null -> Unit
                openFile.content.type == "binary" -> Text(
                    "Binary file — preview not shown" +
                        (openFile.content.mimeType?.let { " ($it)" } ?: ""),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SelectionContainer {
                        Text(
                            openFile.content.content,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}
