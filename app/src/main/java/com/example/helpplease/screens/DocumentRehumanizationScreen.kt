// screens/DocumentRehumanizationScreen.kt
package com.example.helpplease.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

            // Document text input
            item {
                OutlinedTextField(
                    value = viewModel.documentText,
                    onValueChange = { viewModel.updateDocumentText(it) },
                    label = { Text("Paste document text here") },
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

                                Spacer(modifier = Modifier.height(16.dp))

                                // Download button placeholder (not yet implemented)
                                OutlinedButton(
                                    onClick = { /* Will be implemented later */ }
                                ) {
                                    Text("Download Document")
                                    Spacer(Modifier.width(8.dp))
                                    IncompleteFunctionIcon()
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