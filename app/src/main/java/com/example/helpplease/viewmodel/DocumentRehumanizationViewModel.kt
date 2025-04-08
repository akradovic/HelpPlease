// viewmodel/DocumentRehumanizationViewModel.kt
package com.example.helpplease.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helpplease.model.DehumanizingTerms
import com.example.helpplease.model.DocumentProcessingRequest
import com.example.helpplease.model.DocumentProcessingResult
import com.example.helpplease.service.DocumentProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentRehumanizationViewModel : ViewModel() {

    // Document processor service
    private val processor = DocumentProcessor()

    // UI state
    var documentText by mutableStateOf("")
        private set

    var personName by mutableStateOf("")
        private set

    // List of terms the user can select for replacement
    val availableTerms = DehumanizingTerms.commonTerms.keys.toList()

    // Terms selected by the user (default: all terms selected)
    var selectedTerms by mutableStateOf(availableTerms.toMutableList())
        private set

    // Processing state and result
    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    // Functions to update the state
    fun updateDocumentText(text: String) {
        documentText = text
    }

    fun updatePersonName(name: String) {
        personName = name
    }

    fun toggleTermSelection(term: String) {
        selectedTerms = selectedTerms.toMutableList().apply {
            if (contains(term)) remove(term) else add(term)
        }
    }

    fun selectAllTerms() {
        selectedTerms = availableTerms.toMutableList()
    }

    fun deselectAllTerms() {
        selectedTerms = mutableListOf()
    }

    // Process the document when the user submits
    fun processDocument() {
        if (documentText.isBlank()) {
            _processingState.value = ProcessingState.Error("Document text cannot be empty")
            return
        }

        if (personName.isBlank()) {
            _processingState.value = ProcessingState.Error("Person name cannot be empty")
            return
        }

        if (selectedTerms.isEmpty()) {
            _processingState.value = ProcessingState.Error("No terms selected for replacement")
            return
        }

        _processingState.value = ProcessingState.Processing

        viewModelScope.launch {
            try {
                val request = DocumentProcessingRequest(
                    documentText = documentText,
                    personName = personName,
                    selectedTermsToReplace = selectedTerms
                )

                val result = processor.processDocument(request)
                _processingState.value = ProcessingState.Success(result)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error("Error processing document: ${e.message}")
            }
        }
    }

    // States for the processing flow
    sealed class ProcessingState {
        object Idle : ProcessingState()
        object Processing : ProcessingState()
        data class Success(val result: DocumentProcessingResult) : ProcessingState()
        data class Error(val message: String) : ProcessingState()
    }
}