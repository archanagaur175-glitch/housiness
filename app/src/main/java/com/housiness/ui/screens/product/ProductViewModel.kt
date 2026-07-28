package com.housiness.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.db.entity.*
import com.housiness.data.repository.PantryRepository
import com.housiness.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class MaterialUiState(
    val tempId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val unitType: UnitType = UnitType.UNIT,
    val entryMode: MaterialEntryMode = MaterialEntryMode.FLAT,
    val pricePaid: String = "",
    val unitSize: String = "",
    val quantityUsed: String = "",
    val flatCost: String = "",
    val calculatedCost: Long = 0,
    val saveToPantry: Boolean = false,
    val pantryItemId: Long? = null
)

enum class MaterialEntryMode { FLAT, UNIT_BASED }

data class ProductUiState(
    val step: Int = 0,
    val name: String = "",
    val sellingPrice: String = "",
    val costSource: CostSource = CostSource.MANUAL,
    val manualCost: String = "",
    val materials: List<MaterialUiState> = emptyList(),
    val isSaving: Boolean = false,
    val savedProductId: Long? = null,
    val error: String? = null
) {
    val makingCost: Long
        get() = if (costSource == CostSource.MANUAL) {
            manualCost.toLongOrNull() ?: 0
        } else {
            materials.sumOf { it.calculatedCost }
        }

    val totalSellingPrice: Long
        get() = sellingPrice.toLongOrNull() ?: 0

    val isValid: Boolean
        get() = name.isNotBlank() && sellingPrice.toLongOrNull() != null && makingCost >= 0

    val isStep1Valid: Boolean
        get() = name.isNotBlank() && sellingPrice.toLongOrNull() != null

    val isStep2Valid: Boolean
        get() = when (costSource) {
            CostSource.MANUAL -> manualCost.toLongOrNull() != null
            CostSource.CALCULATED -> materials.isNotEmpty() && materials.all { it.calculatedCost > 0 }
        }

    val computedMakingCost: Long
        get() = when (costSource) {
            CostSource.MANUAL -> manualCost.toLongOrNull() ?: 0
            CostSource.CALCULATED -> materials.sumOf { it.calculatedCost }
        }
}

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val pantryRepository: PantryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    private val _pantryItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val pantryItems: StateFlow<List<PantryItem>> = _pantryItems.asStateFlow()

    init {
        viewModelScope.launch {
            pantryRepository.getAllFlow().collect { _pantryItems.value = it }
        }
    }

    fun setName(value: String) { _state.value = _state.value.copy(name = value) }
    fun setSellingPrice(value: String) { _state.value = _state.value.copy(sellingPrice = value) }
    fun setCostSource(source: CostSource) { _state.value = _state.value.copy(costSource = source) }
    fun setManualCost(value: String) { _state.value = _state.value.copy(manualCost = value) }

    fun setStep(step: Int) { _state.value = _state.value.copy(step = step) }

    fun addMaterial() {
        val current = _state.value.materials
        _state.value = _state.value.copy(materials = current + MaterialUiState())
    }

    fun updateMaterial(tempId: String, transform: (MaterialUiState) -> MaterialUiState) {
        val updated = _state.value.materials.map { if (it.tempId == tempId) transform(it) else it }
        _state.value = _state.value.copy(materials = updated)
        recalculateMaterialCost(tempId)
    }

    fun removeMaterial(tempId: String) {
        val updated = _state.value.materials.filter { it.tempId != tempId }
        _state.value = _state.value.copy(materials = updated)
    }

    fun selectAndAddPantryItem(item: PantryItem) {
        val state = _state.value
        val idx = state.materials.indexOfLast { it.name.isBlank() }
        if (idx >= 0) {
            updateMaterial(state.materials[idx].tempId) {
                it.copy(
                    name = item.name,
                    unitType = item.unitType,
                    entryMode = MaterialEntryMode.FLAT,
                    flatCost = item.unitCost.toString(),
                    calculatedCost = item.unitCost,
                    pantryItemId = item.id
                )
            }
        } else {
            val newMat = MaterialUiState(
                name = item.name,
                unitType = item.unitType,
                entryMode = MaterialEntryMode.FLAT,
                flatCost = item.unitCost.toString(),
                calculatedCost = item.unitCost,
                pantryItemId = item.id
            )
            _state.value = state.copy(materials = state.materials + newMat)
        }
    }

    private fun recalculateMaterialCost(tempId: String) {
        val materials = _state.value.materials
        val idx = materials.indexOfFirst { it.tempId == tempId }
        if (idx == -1) return

        val mat = materials[idx]
        val calculated = when (mat.entryMode) {
            MaterialEntryMode.FLAT -> mat.flatCost.toLongOrNull() ?: 0
            MaterialEntryMode.UNIT_BASED -> {
                val price = mat.pricePaid.toLongOrNull() ?: 0
                val size = mat.unitSize.toLongOrNull() ?: 1
                val qty = mat.quantityUsed.toLongOrNull() ?: 0
                if (size > 0) (price.toDouble() / size * qty).toLong() else 0
            }
        }
        val updated = materials.toMutableList()
        updated[idx] = mat.copy(calculatedCost = calculated)
        _state.value = _state.value.copy(materials = updated)
    }

    fun save() {
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            _state.value = s.copy(isSaving = true, error = null)
            try {
                val now = System.currentTimeMillis()
                val product = Product(
                    name = s.name,
                    sellingPrice = s.sellingPrice.toLongOrNull() ?: 0,
                    makingCost = s.computedMakingCost,
                    costSource = s.costSource,
                    createdAt = now
                )
                val productId = productRepository.insert(product)

                if (s.costSource == CostSource.CALCULATED) {
                    val materials = s.materials.map { mat ->
                        ProductMaterial(
                            productId = productId,
                            materialId = mat.pantryItemId,
                            name = mat.name,
                            unitType = mat.unitType,
                            unitCost = mat.calculatedCost,
                            pricePaid = mat.pricePaid.toLongOrNull() ?: 0,
                            unitSize = mat.unitSize.toLongOrNull() ?: 1,
                            quantityUsed = mat.quantityUsed.toLongOrNull() ?: 1,
                            calculatedCost = mat.calculatedCost
                        )
                    }
                    productRepository.saveMaterials(materials)

                    s.materials.filter { it.saveToPantry && it.name.isNotBlank() }.forEach { mat ->
                        pantryRepository.insert(
                            PantryItem(
                                name = mat.name,
                                unitType = mat.unitType,
                                unitCost = mat.calculatedCost,
                                lastUsed = now
                            )
                        )
                    }
                }

                _state.value = _state.value.copy(isSaving = false, savedProductId = productId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val pantryRepository: PantryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductViewModel(productRepository, pantryRepository) as T
    }
}
