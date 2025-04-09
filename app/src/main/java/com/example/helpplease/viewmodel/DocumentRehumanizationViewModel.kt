// viewmodel/DocumentRehumanizationViewModel.kt
package com.example.helpplease.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helpplease.model.DehumanizingTerms
import com.example.helpplease.model.DocumentProcessingRequest
import com.example.helpplease.model.DocumentProcessingResult
import com.example.helpplease.service.DocumentProcessor
import com.example.helpplease.service.FileExportService
import com.example.helpplease.service.FileParserService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class DocumentRehumanizationViewModel : ViewModel() {

    // Service instances
    private val processor = DocumentProcessor()
    private lateinit var fileParserService: FileParserService
    private lateinit var fileExportService: FileExportService
    private lateinit var appContext: Context

    // Initialize services - called from UI
    fun initializeServices(context: Context) {
        appContext = context.applicationContext
        fileParserService = FileParserService(context)
        fileExportService = FileExportService(context)
    }

    // UI state
    var documentText by mutableStateOf("")
        private set

    var formattedText by mutableStateOf<String?>(null)
        private set

    var originalFormat by mutableStateOf<String?>(null)
        private set

    var personName by mutableStateOf("")
        private set

    // File upload state
    var selectedFileName by mutableStateOf<String?>(null)
        private set

    var fileUploadState by mutableStateOf<FileUploadState>(FileUploadState.Idle)
        private set

    // Export state
    var exportState by mutableStateOf<ExportState>(ExportState.Idle)
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

    // Process uploaded file
    fun processFileUpload(uri: Uri, mimeType: String, fileName: String) {
        fileUploadState = FileUploadState.Processing
        selectedFileName = fileName
        originalFormat = mimeType

        viewModelScope.launch {
            try {
                val result = fileParserService.parseFile(uri, mimeType)

                result.fold(
                    onSuccess = { parsedDocument ->
                        documentText = parsedDocument.rawText
                        formattedText = parsedDocument.formattedText
                        originalFormat = parsedDocument.originalFormat
                        fileUploadState = FileUploadState.Success(fileName)
                    },
                    onFailure = { exception ->
                        fileUploadState = FileUploadState.Error("File parsing failed: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                fileUploadState = FileUploadState.Error("Error processing file: ${e.message}")
            }
        }
    }

    // Reset file upload state
    fun resetFileUploadState() {
        fileUploadState = FileUploadState.Idle
        selectedFileName = null
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
                    formattedText = formattedText,
                    personName = personName,
                    selectedTermsToReplace = selectedTerms,
                    originalFormat = originalFormat
                )

                val result = processor.processDocument(request)
                _processingState.value = ProcessingState.Success(result)
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error("Error processing document: ${e.message}")
            }
        }
    }

    /**
     * Export the processed document
     */
    fun exportDocument(result: DocumentProcessingResult) {
        if (exportState is ExportState.Processing) {
            return // Prevent multiple exports
        }

        exportState = ExportState.Processing

        viewModelScope.launch {
            try {
                // Use formatted text if available, otherwise use plain text
                val textToExport = result.formattedProcessedText ?: result.processedText

                // Get original format or default to text/plain
                val format = result.originalFormat ?: "text/plain"

                // Generate filename based on original filename if available
                val baseFileName = selectedFileName?.let {
                    it.substringBeforeLast(".") + "_rehumanized"
                }

                val exportResult = fileExportService.exportDocument(
                    processedText = textToExport,
                    originalFormat = format,
                    fileName = baseFileName
                )

                exportResult.fold(
                    onSuccess = { uri ->
                        // Open the document for viewing
                        openDocument(uri, format)
                        exportState = ExportState.Success(uri)

                        // Auto-reset the export button after 5 seconds
                        viewModelScope.launch {
                            delay(5000) // 5 seconds delay
                            resetExportState()
                        }
                    },
                    onFailure = { exception ->
                        exportState = ExportState.Error("Export failed: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                exportState = ExportState.Error("Error exporting document: ${e.message}")
            }
        }
    }

    /**
     * Open the document in an appropriate viewer app
     */
    private fun openDocument(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // This requires the activity context, which will be handled in the UI layer
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    /**
     * Reset export state
     */
    fun resetExportState() {
        exportState = ExportState.Idle
    }

    // States for the processing flow
    sealed class ProcessingState {
        object Idle : ProcessingState()
        object Processing : ProcessingState()
        data class Success(val result: DocumentProcessingResult) : ProcessingState()
        data class Error(val message: String) : ProcessingState()
    }

    // States for file upload
    sealed class FileUploadState {
        object Idle : FileUploadState()
        object Processing : FileUploadState()
        data class Success(val fileName: String) : FileUploadState()
        data class Error(val message: String) : FileUploadState()
    }

    // States for document export
    sealed class ExportState {
        object Idle : ExportState()
        object Processing : ExportState()
        data class Success(val uri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
    }
}