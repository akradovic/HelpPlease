// screens/DocumentRehumanizationScreen.kt
package com.example.helpplease.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.helpplease.IncompleteFunctionIcon
import com.example.helpplease.viewmodel.DocumentRehumanizationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRehumanizationScreen(
    navController: NavController,
    viewModel: DocumentRehumanizationViewModel = viewModel()
) {
    val processingState by viewModel.processingState.collectAsState()
    val fileUploadState = viewModel.fileUploadState
    val context = LocalContext.current

    // Initialize the services that require context
    LaunchedEffect(Unit) {
        viewModel.initializeServices(context)
    }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Get MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            // Get file name
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = if (nameIndex != null && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                "Uploaded Document"
            }
            cursor?.close()

            // Check if file type is supported
            if (mimeType == "application/pdf" ||
                mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                mimeType == "text/plain") {

                // Process the file
                viewModel.processFileUpload(uri, mimeType, fileName)
            } else {
                // Display error for unsupported file type
                // In a production app, you'd want to show a Snackbar or dialog here
                println("Unsupported file type: $mimeType")
            }
        }
    }

    // Main UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Rehumanization Tool") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction to the tool
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Rehumanization Tool",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This tool replaces dehumanizing legal terms with a person's name, " +
                                    "helping to maintain dignity and identity in legal documents.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // File upload section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Upload Document",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select a PDF, DOCX, or TXT file to automatically extract text.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // File upload button and status indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Upload button
                            Button(
                                onClick = {
                                    documentPickerLauncher.launch(arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "text/plain"
                                    ))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = "Upload")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select File")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Display file upload status
                            when (fileUploadState) {
                                is DocumentRehumanizationViewModel.FileUploadState.Idle -> {
                                    // No file selected yet
                                    if (viewModel.selectedFileName != null) {
                                        FileStatusChip(
                                            fileName = viewModel.selectedFileName!!,
                                            status = "Uploaded",
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }
                                }
                                is DocumentRehumanizationViewModel.FileUploadState.Processing -> {
                                    // Show loading indicator
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                is DocumentRehumanizationViewModel.FileUploadState.Success -> {
                                    // Show success indicator
                                    val fileName = (fileUploadState as DocumentRehumanizationViewModel.FileUploadState.Success).fileName
                                    FileStatusChip(
                                        fileName = fileName,
                                        status = "Uploaded",
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                                is DocumentRehumanizationViewModel.FileUploadState.Error -> {
                                    // Show error message
                                    val errorMessage = (fileUploadState as DocumentRehumanizationViewModel.FileUploadState.Error).message
                                    FileStatusChip(
                                        fileName = "Error",
                                        status = errorMessage,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Document text input
            item {
                OutlinedTextField(
                    value = viewModel.documentText,
                    onValueChange = { viewModel.updateDocumentText(it) },
                    label = { Text("Document text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    maxLines = 10
                )
            }

            // Person name input
            item {
                OutlinedTextField(
                    value = viewModel.personName,
                    onValueChange = { viewModel.updatePersonName(it) },
                    label = { Text("Person's name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Term selection header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms to Replace",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        TextButton(onClick = { viewModel.selectAllTerms() }) {
                            Text("Select All")
                        }

                        TextButton(onClick = { viewModel.deselectAllTerms() }) {
                            Text("Deselect All")
                        }
                    }
                }
            }

            // Term selection list
            items(viewModel.availableTerms) { term ->
                val isSelected = viewModel.selectedTerms.contains(term)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { viewModel.toggleTermSelection(term) }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = term,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Process button
            item {
                Button(
                    onClick = { viewModel.processDocument() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Process Document")
                }
            }

            // Result section
            when (val state = processingState) {
                is DocumentRehumanizationViewModel.ProcessingState.Processing -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is DocumentRehumanizationViewModel.ProcessingState.Success -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Processing Results",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Replacements made: ${state.result.replacementsMade.values.sum()}",
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Processed Document:")

                                Spacer(modifier = Modifier.height(8.dp))

                                // Display formatted text if available, otherwise raw text
                                if (state.result.formattedProcessedText != null) {
                                    FormattedTextDisplay(
                                        htmlText = state.result.formattedProcessedText,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Color.LightGray,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                    )
                                } else {
                                    Text(
                                        text = state.result.processedText,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = Color.LightGray,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Download button
                                val exportState = viewModel.exportState

                                when (exportState) {
                                    is DocumentRehumanizationViewModel.ExportState.Idle -> {
                                        Button(
                                            onClick = { viewModel.exportDocument(state.result) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Download Document")
                                        }
                                    }

                                    is DocumentRehumanizationViewModel.ExportState.Processing -> {
                                        Button(
                                            onClick = { },
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Preparing Document...")
                                        }
                                    }

                                    is DocumentRehumanizationViewModel.ExportState.Success -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.Green
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Document saved successfully",
                                                color = Color.Green
                                            )
                                        }
                                    }

                                    is DocumentRehumanizationViewModel.ExportState.Error -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Error,
                                                    contentDescription = null,
                                                    tint = Color.Red
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = exportState.message,
                                                    color = Color.Red
                                                )
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.resetExportState()
                                                    viewModel.exportDocument(state.result)
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Try Again")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is DocumentRehumanizationViewModel.ProcessingState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                text = state.message,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Red
                            )
                        }
                    }
                }

                else -> { /* No specific UI for idle state */ }
            }
        }
    }
}

@Composable
fun FileStatusChip(
    fileName: String,
    status: String,
    color: Color
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Custom component to display HTML-formatted text
 */
@Composable
fun FormattedTextDisplay(
    htmlText: String,
    modifier: Modifier = Modifier
) {
    // Basic WebView implementation to render HTML
    AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                settings.javaScriptEnabled = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isVerticalScrollBarEnabled = true

                // Apply styling to make it look like a document
                val cssStyle = """
                    <style>
                        body {
                            font-family: sans-serif;
                            font-size: 14px;
                            line-height: 1.5;
                            color: #212121;
                            padding: 8px;
                            margin: 0;
                        }
                        p {
                            margin-top: 0.5em;
                            margin-bottom: 0.5em;
                        }
                        .text-center {
                            text-align: center;
                        }
                        .text-right {
                            text-align: right;
                        }
                        .page-break {
                            height: 20px;
                            border-top: 1px dashed #ccc;
                            margin: 20px 0;
                            text-align: center;
                            position: relative;
                        }
                        .page-break::after {
                            content: "Page Break";
                            position: absolute;
                            top: -10px;
                            left: 50%;
                            transform: translateX(-50%);
                            background: white;
                            padding: 0 10px;
                            font-size: 12px;
                            color: #666;
                        }
                    </style>
                """.trimIndent()

                // Load the HTML content with CSS styling
                loadDataWithBaseURL(null, "<html><head>$cssStyle</head><body>$htmlText</body></html>", "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            // Update the HTML content if it changes
            webView.loadDataWithBaseURL(null, "<html><head><style>body{font-family:sans-serif;font-size:14px;line-height:1.5;color:#212121;padding:8px;margin:0;}p{margin-top:0.5em;margin-bottom:0.5em;}.text-center{text-align:center;}.text-right{text-align:right;}.page-break{height:20px;border-top:1px dashed #ccc;margin:20px 0;text-align:center;position:relative;}.page-break::after{content:\"Page Break\";position:absolute;top:-10px;left:50%;transform:translateX(-50%);background:white;padding:0 10px;font-size:12px;color:#666;}</style></head><body>$htmlText</body></html>", "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}