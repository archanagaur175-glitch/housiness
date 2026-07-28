package com.housiness.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.housiness.HousinessApp
import com.housiness.util.BackupManager
import com.housiness.util.ImportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as HousinessApp
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportData by remember { mutableStateOf<BackupManager.ValidationResult.Valid?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            statusText = null
            try {
                val json = withContext(Dispatchers.IO) {
                    BackupManager.export(
                        productDao = app.serviceLocator.productDao,
                        productMaterialDao = app.serviceLocator.productMaterialDao,
                        pantryDao = app.serviceLocator.pantryDao,
                        orderDao = app.serviceLocator.orderDao,
                        invoiceDao = app.serviceLocator.invoiceDao,
                        invoiceLineItemDao = app.serviceLocator.invoiceLineItemDao,
                        messageTemplateDao = app.serviceLocator.messageTemplateDao
                    )
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                }
                statusText = "Backup saved successfully!"
            } catch (e: Exception) {
                statusText = "Failed: ${e.message}"
            } finally {
                isBusy = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            statusText = null
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        ?: throw Exception("Could not read file")
                }
                val result = BackupManager.validate(json)
                when (result) {
                    is BackupManager.ValidationResult.Valid -> {
                        pendingImportData = result
                        importWarning = result.warning
                        showImportDialog = true
                    }
                    is BackupManager.ValidationResult.Invalid -> {
                        statusText = result.message
                    }
                }
            } catch (e: Exception) {
                statusText = "Failed: ${e.message}"
            } finally {
                isBusy = false
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Load backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How should we add this backup?")
                    if (importWarning != null) {
                        Text(
                            text = importWarning!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = pendingImportData?.data ?: return@TextButton
                    scope.launch {
                        showImportDialog = false
                        isBusy = true
                        try {
                            withContext(Dispatchers.IO) {
                                BackupManager.import(
                                    data = data,
                                    mode = ImportMode.MERGE,
                                    productDao = app.serviceLocator.productDao,
                                    productMaterialDao = app.serviceLocator.productMaterialDao,
                                    pantryDao = app.serviceLocator.pantryDao,
                                    orderDao = app.serviceLocator.orderDao,
                                    invoiceDao = app.serviceLocator.invoiceDao,
                                    invoiceLineItemDao = app.serviceLocator.invoiceLineItemDao,
                                    messageTemplateDao = app.serviceLocator.messageTemplateDao
                                )
                            }
                            statusText = "Backup loaded (merge)!"
                        } catch (e: Exception) {
                            statusText = "Failed: ${e.message}"
                        } finally {
                            isBusy = false
                            pendingImportData = null
                        }
                    }
                }) {
                    Text("Merge")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val data = pendingImportData?.data ?: return@TextButton
                    scope.launch {
                        showImportDialog = false
                        isBusy = true
                        try {
                            withContext(Dispatchers.IO) {
                                BackupManager.import(
                                    data = data,
                                    mode = ImportMode.REPLACE,
                                    productDao = app.serviceLocator.productDao,
                                    productMaterialDao = app.serviceLocator.productMaterialDao,
                                    pantryDao = app.serviceLocator.pantryDao,
                                    orderDao = app.serviceLocator.orderDao,
                                    invoiceDao = app.serviceLocator.invoiceDao,
                                    invoiceLineItemDao = app.serviceLocator.invoiceLineItemDao,
                                    messageTemplateDao = app.serviceLocator.messageTemplateDao
                                )
                            }
                            statusText = "Backup loaded (replace)!"
                        } catch (e: Exception) {
                            statusText = "Failed: ${e.message}"
                        } finally {
                            isBusy = false
                            pendingImportData = null
                        }
                    }
                }) {
                    Text("Replace", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your data lives only on this device. Backups let you move to a new phone or keep a safety copy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Card(
                onClick = {
                    if (!isBusy) exportLauncher.launch(BackupManager.generateFilename())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.SaveAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text("Save a backup", style = MaterialTheme.typography.titleMedium)
                        Text("Export all your data to a file", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                onClick = {
                    if (!isBusy) importLauncher.launch(arrayOf("application/json", "*/*"))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text("Load a backup", style = MaterialTheme.typography.titleMedium)
                        Text("Restore data from a backup file", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (statusText != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
