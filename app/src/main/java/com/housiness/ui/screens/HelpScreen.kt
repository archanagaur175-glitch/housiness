package com.housiness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Use Housiness") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Section("Adding Products") {
                BulletPoint("Go to the Products tab and tap the + button.")
                BulletPoint("Enter the product name and selling price.")
                BulletPoint("Choose how to track making cost: enter a total cost, or calculate from individual materials.")
                BulletPoint("Review and tap Save Product.")
                BulletPoint("You can also add multiple products at once using the Bulk Add button on the Products screen.")
            }
            Section("Creating Orders") {
                BulletPoint("Go to the Orders tab and tap the + button.")
                BulletPoint("Select a product, enter quantity and price details.")
                BulletPoint("Upcoming orders appear in the list — swipe left to mark as complete.")
            }
            Section("Generating Invoices") {
                BulletPoint("Tap an order to open the detail sheet.")
                BulletPoint("Tap Generate Invoice to create a PDF invoice.")
                BulletPoint("Preview or share the invoice directly from the invoice screen.")
                BulletPoint("Note: generating an invoice does NOT mark the order as complete.")
            }
            Section("Managing Materials & Pantry") {
                BulletPoint("When adding a product, choose 'Calculate from materials' to specify individual ingredients/materials.")
                BulletPoint("For each material, enter a flat cost or per-unit cost with size and quantity.")
                BulletPoint("Check 'Save to pantry' to reuse the material later.")
                BulletPoint("Access your saved materials in Settings > Pantry Manager.")
            }
            Section("Viewing Finances") {
                BulletPoint("The Home tab shows your revenue, profit, average per order, and margin.")
                BulletPoint("These numbers update automatically as you add orders and mark them complete.")
            }
            Section("Backup & Restore") {
                BulletPoint("Go to Settings > Backup & Restore.")
                BulletPoint("Tap 'Save a backup' to export your data as a JSON file.")
                BulletPoint("Tap 'Load a backup' to import data from a previous backup.")
                BulletPoint("Choose Merge to add data without removing existing records, or Replace to overwrite.")
            }
            Section("Business Name") {
                BulletPoint("You were prompted to enter your business name on first launch.")
                BulletPoint("Your business name appears on generated PDF invoices.")
                BulletPoint("Change it anytime in Settings > Business Name.")
            }
            Section("Message Templates") {
                BulletPoint("Create reusable message templates in Settings > Message Templates.")
                BulletPoint("Templates can include order and invoice details for easy customer communication.")
            }
            Section("Tips") {
                BulletPoint("Use the pantry feature to save commonly used materials and their costs.")
                BulletPoint("Completed orders are collapsed — tap the header to expand them.")
                BulletPoint("Backup regularly to avoid data loss.")
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Text("\u2022  $text", style = MaterialTheme.typography.bodyMedium)
}
