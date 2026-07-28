package com.housiness.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.housiness.ui.screens.invoice.InvoiceHistoryScreen
import com.housiness.ui.screens.invoice.InvoicePreviewScreen
import com.housiness.ui.screens.backup.BackupRestoreScreen
import com.housiness.ui.screens.template.TemplateEditScreen
import com.housiness.ui.screens.template.TemplateListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var showInvoiceHistory by remember { mutableStateOf(false) }
    var previewInvoiceId by remember { mutableStateOf<Long?>(null) }
    var showTemplates by remember { mutableStateOf(false) }
    var editingTemplateId by remember { mutableStateOf<Long?>(null) }
    var showBackup by remember { mutableStateOf(false) }

    if (previewInvoiceId != null) {
        InvoicePreviewScreen(
            invoiceId = previewInvoiceId!!,
            onBack = { previewInvoiceId = null }
        )
        return
    }

    if (showInvoiceHistory) {
        InvoiceHistoryScreen(
            onBack = { showInvoiceHistory = false },
            onInvoiceClick = { id -> previewInvoiceId = id }
        )
        return
    }

    if (showTemplates) {
        when (editingTemplateId) {
            null -> {
                TemplateListScreen(
                    onBack = { showTemplates = false },
                    onEditTemplate = { id -> editingTemplateId = id },
                    onCreateTemplate = { editingTemplateId = -1L }
                )
            }
            else -> {
                TemplateEditScreen(
                    templateId = if (editingTemplateId == -1L) null else editingTemplateId,
                    onBack = { editingTemplateId = null },
                    onSaved = { editingTemplateId = null }
                )
            }
        }
        return
    }

    if (showBackup) {
        BackupRestoreScreen(onBack = { showBackup = false })
        return
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                onClick = { showInvoiceHistory = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Invoice History", style = MaterialTheme.typography.titleMedium)
                        Text("View and share past invoices", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                onClick = { showTemplates = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Message, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("Message Templates", style = MaterialTheme.typography.titleMedium)
                        Text("Create and share reusable messages", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                onClick = { showBackup = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.SaveAlt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Column {
                        Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
                        Text("Save or load your data", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column {
                        Text("Pantry Manager", style = MaterialTheme.typography.titleMedium)
                        Text("Coming soon", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column {
                        Text("About", style = MaterialTheme.typography.titleMedium)
                        Text("Housiness v1.0.0", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
