package com.housiness.ui.screens.template

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.db.entity.MessageTemplate
import com.housiness.data.repository.MessageTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TemplateEditState(
    val id: Long? = null,
    val title: String = "",
    val body: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class TemplateViewModel(
    private val repository: MessageTemplateRepository
) : ViewModel() {

    private val _editState = MutableStateFlow(TemplateEditState())
    val editState: StateFlow<TemplateEditState> = _editState.asStateFlow()

    fun loadTemplate(id: Long) {
        viewModelScope.launch {
            val template = repository.getById(id) ?: return@launch
            _editState.value = TemplateEditState(
                id = template.id,
                title = template.title,
                body = template.body
            )
        }
    }

    fun setTitle(value: String) { _editState.value = _editState.value.copy(title = value) }
    fun setBody(value: String) { _editState.value = _editState.value.copy(body = value) }

    fun save() {
        val s = _editState.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            _editState.value = s.copy(isSaving = true)
            val template = MessageTemplate(
                id = s.id ?: 0,
                title = s.title,
                body = s.body,
                lastEditedAt = System.currentTimeMillis()
            )
            if (s.id != null) repository.update(template)
            else repository.insert(template)
            _editState.value = _editState.value.copy(isSaving = false, saved = true)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.getById(id)?.let { repository.delete(it) }
        }
    }

    fun getShareIntent(template: MessageTemplate): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, template.body)
            putExtra(Intent.EXTRA_SUBJECT, template.title)
        }
    }

    class Factory(private val repository: MessageTemplateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TemplateViewModel(repository) as T
    }
}
