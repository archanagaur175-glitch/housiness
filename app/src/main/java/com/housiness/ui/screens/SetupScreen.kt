package com.housiness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.housiness.data.db.entity.AppSettings
import com.housiness.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(settingsRepository: SettingsRepository, onComplete: () -> Unit) {
    var businessName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83C\uDFE0",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Welcome to Housiness!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Let's set up your business profile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = businessName,
                onValueChange = {
                    businessName = it
                    error = false
                },
                label = { Text("Your business name") },
                placeholder = { Text("e.g. My Home Bakery") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = error,
                supportingText = if (error) {{ Text("Please enter a business name") }} else null
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (businessName.isBlank()) {
                        error = true
                    } else {
                        scope.launch {
                            settingsRepository.save(AppSettings(businessName = businessName.trim()))
                            onComplete()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started")
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "You can change this later in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
