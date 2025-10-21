package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.data.BackendConfig
import com.example.topoclimb.viewmodel.BackendManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendManagementScreen(
    onBackClick: () -> Unit,
    onNavigateToLogin: (String, String) -> Unit = { _, _ -> },
    viewModel: BackendManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingBackend by remember { mutableStateOf<BackendConfig?>(null) }
    
    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage TopoClimb Instances") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add TopoClimb Instance")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Configure TopoClimb instance URLs to fetch climbing data from multiple sources",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(uiState.backends) { backend ->
                BackendItem(
                    backend = backend,
                    onToggleEnabled = { viewModel.toggleBackendEnabled(backend.id) },
                    onEdit = {
                        editingBackend = backend
                        showEditDialog = true
                    },
                    onDelete = { viewModel.deleteBackend(backend.id) },
                    onLogin = {
                        onNavigateToLogin(backend.id, backend.name)
                    },
                    onLogout = {
                        viewModel.logout(backend.id)
                    },
                    onSetDefault = {
                        viewModel.setDefaultBackend(backend.id)
                    }
                )
            }
            
            if (uiState.backends.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No TopoClimb instances configured. Add one to get started.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddBackendDialog(
            onDismiss = { 
                viewModel.clearInstanceMeta()
                showAddDialog = false 
            },
            onAdd = { name, url ->
                viewModel.addBackend(name, url)
                viewModel.clearInstanceMeta()
                showAddDialog = false
            },
            viewModel = viewModel
        )
    }
    
    if (showEditDialog && editingBackend != null) {
        EditBackendDialog(
            backend = editingBackend!!,
            onDismiss = {
                showEditDialog = false
                editingBackend = null
            },
            onSave = { backend ->
                viewModel.updateBackend(backend)
                showEditDialog = false
                editingBackend = null
            }
        )
    }
    
    if (uiState.showRestartWarning) {
        RestartWarningDialog(
            onDismiss = { viewModel.dismissRestartWarning() }
        )
    }
}

@Composable
fun BackendItem(
    backend: BackendConfig,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLogin: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSetDefault: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = backend.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (backend.isAuthenticated()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Authenticated",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (backend.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = backend.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (backend.isAuthenticated() && backend.user != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Logged in as ${backend.user.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row {
                    Switch(
                        checked = backend.enabled,
                        onCheckedChange = { onToggleEnabled() }
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    if (backend.isAuthenticated()) {
                        TextButton(onClick = onLogout) {
                            Text("Logout")
                        }
                        if (!backend.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onSetDefault) {
                                Text("Set as Default")
                            }
                        }
                    } else {
                        TextButton(onClick = onLogin) {
                            Text("Login")
                        }
                    }
                }
                Row {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun AddBackendDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    viewModel: BackendManagementViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }
    
    val instanceMeta = uiState.instanceMeta
    
    // Trigger meta fetch when URL is valid
    LaunchedEffect(url, urlError) {
        if (url.isNotBlank() && urlError == null && validateUrl(url) == null) {
            kotlinx.coroutines.delay(500) // Debounce
            viewModel.fetchInstanceMeta(url)
        } else if (url.isBlank() || urlError != null) {
            viewModel.clearInstanceMeta()
        }
    }
    
    // Auto-fill name if meta is loaded and name is empty
    LaunchedEffect(instanceMeta) {
        if (instanceMeta != null && name.isBlank()) {
            name = instanceMeta.name
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add TopoClimb Instance") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = validateUrl(it)
                    },
                    label = { Text("Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://api.example.com/") },
                    isError = urlError != null,
                    supportingText = {
                        if (urlError != null) {
                            Text(urlError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Must end with /")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show instance metadata if available
                if (uiState.metaLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetching instance info...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (instanceMeta != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (instanceMeta.pictureUrl != null) {
                                AsyncImage(
                                    model = instanceMeta.pictureUrl,
                                    contentDescription = "Instance logo",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.small),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = instanceMeta.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = instanceMeta.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Version ${instanceMeta.version}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Instance Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, url) },
                enabled = name.isNotBlank() && url.isNotBlank() && urlError == null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditBackendDialog(
    backend: BackendConfig,
    onDismiss: () -> Unit,
    onSave: (BackendConfig) -> Unit
) {
    var name by remember { mutableStateOf(backend.name) }
    var url by remember { mutableStateOf(backend.baseUrl) }
    var urlError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit TopoClimb Instance") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Instance Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = validateUrl(it)
                    },
                    label = { Text("Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://api.example.com/") },
                    isError = urlError != null,
                    supportingText = {
                        if (urlError != null) {
                            Text(urlError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Must end with /")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(backend.copy(name = name, baseUrl = url))
                },
                enabled = name.isNotBlank() && url.isNotBlank() && urlError == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RestartWarningDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Restart Required") },
        text = {
            Text(
                text = "Please restart the app to see the changes in your TopoClimb instances.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun validateUrl(url: String): String? {
    return when {
        url.isBlank() -> null
        !url.startsWith("http://") && !url.startsWith("https://") -> 
            "URL must start with http:// or https://"
        !url.endsWith("/") -> 
            "URL must end with /"
        else -> null
    }
}
