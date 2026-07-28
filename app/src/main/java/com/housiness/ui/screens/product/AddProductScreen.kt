package com.housiness.ui.screens.product

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.housiness.HousinessApp
import com.housiness.ui.components.StepIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HousinessApp
    val vm: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(
            app.serviceLocator.productRepository,
            app.serviceLocator.pantryRepository
        )
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val pantryItems by vm.pantryItems.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedProductId) {
        if (state.savedProductId != null) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.step > 0) {
                        TextButton(onClick = { vm.setStep(state.step - 1) }) {
                            Text("Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(
                    currentStep = state.step,
                    totalSteps = 3
                )

                    Button(
                        onClick = {
                            when (state.step) {
                                0 -> if (state.isStep1Valid) vm.setStep(1)
                                1 -> if (state.isStep2Valid) vm.setStep(2)
                                2 -> vm.save()
                            }
                        },
                        enabled = when (state.step) {
                            0 -> state.isStep1Valid
                            1 -> state.isStep2Valid
                            2 -> true
                        } && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            if (state.step == 2) "Save Product" else "Next"
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(targetState = state.step, label = "stepper") { step ->
                when (step) {
                    0 -> StepNamePrice(
                        name = state.name,
                        sellingPrice = state.sellingPrice,
                        onNameChange = vm::setName,
                        onPriceChange = vm::setSellingPrice
                    )
                    1 -> StepCost(
                        costSource = state.costSource,
                        manualCost = state.manualCost,
                        materials = state.materials,
                        pantryItems = pantryItems,
                        onCostSourceChange = vm::setCostSource,
                        onManualCostChange = vm::setManualCost,
                        onAddMaterial = vm::addMaterial,
                        onUpdateMaterial = { id, fn -> vm.updateMaterial(id, fn) },
                        onRemoveMaterial = vm::removeMaterial,
                        onPickFromPantry = { item -> vm.selectAndAddPantryItem(item) }
                    )
                    2 -> StepReview(
                        name = state.name,
                        sellingPrice = state.totalSellingPrice,
                        costSource = state.costSource,
                        makingCost = state.computedMakingCost,
                        materials = state.materials
                    )
                    else -> {}
                }
            }
        }
    }
}
