package dev.opencode.mobile.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.opencode.models.ConnectionState
import dev.opencode.mobile.ui.icons.OpenCodeIcons

@Composable
fun AddServerScreen(
    viewModel: AddServerViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add server") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(OpenCodeIcons.Back, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                label = { Text("Server address (http(s)://host:port)") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Basic auth", modifier = Modifier.padding(end = 8.dp))
                Switch(checked = state.useBasicAuth, onCheckedChange = viewModel::updateUseBasicAuth)
            }

            if (state.useBasicAuth) {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }

            state.validationError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            state.testErrorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.testResult == ConnectionState.CONNECTED) {
                Text("Connected", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                OutlinedButton(onClick = viewModel::testConnection, enabled = !state.isTesting) {
                    if (state.isTesting) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text("Test connection")
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save")
                }
            }
        }
    }
}
