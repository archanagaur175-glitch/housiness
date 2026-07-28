package com.housiness.ui.screens.invoice

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.housiness.HousinessApp
import com.housiness.util.PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewScreen(
    invoiceId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HousinessApp
    val vm: InvoiceViewModel = viewModel(
        factory = InvoiceViewModel.Factory(
            app.serviceLocator.invoiceRepository,
            app.serviceLocator.orderRepository
        )
    )
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(invoiceId) {
        vm.loadInvoice(invoiceId)
    }

    LaunchedEffect(state.invoice, state.lineItems) {
        if (state.invoice != null && state.pdfFile == null && !state.isGenerating) {
            vm.generatePdf(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.invoice?.orderRef ?: "Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.pdfFile != null) {
                        IconButton(onClick = {
                            val shareIntent = PdfGenerator.getShareIntent(context, state.pdfFile!!)
                            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isGenerating -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Generating invoice...")
                    }
                }
                state.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.error!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                state.pdfFile != null -> {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Invoice ready!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "${state.invoice?.orderRef} generated",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = {
                                val intent = PdfGenerator.getPreviewIntent(context, state.pdfFile!!)
                                context.startActivity(intent)
                            }) {
                                Text("Preview")
                            }
                            OutlinedButton(onClick = {
                                val shareIntent = PdfGenerator.getShareIntent(context, state.pdfFile!!)
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            }) {
                                Text("Share")
                            }
                        }
                    }
                }
                state.invoice == null -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
