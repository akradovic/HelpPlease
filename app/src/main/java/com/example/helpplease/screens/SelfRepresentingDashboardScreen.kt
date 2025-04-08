// SelfRepresentingDashboardScreen.kt
package com.example.helpplease.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helpplease.IncompleteFunctionIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfRepresentingDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Self-Representing Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome to your Self-Representation Hub",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "These tools are designed to help you navigate the legal system more effectively.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Feature cards
            FeatureCard(
                title = "Document Rehumanization Tool",
                description = "Replace dehumanizing legal terms with your name to maintain your identity and dignity in legal documents.",
                icon = Icons.Default.Description,
                onClick = { navController.navigate("document_rehumanization") },
                isImplemented = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = "Document Truth Analyzer",
                description = "Highlight and analyze statements in legal documents for accuracy and review.",
                icon = Icons.Default.FindInPage,
                onClick = { /* Will be implemented later */ },
                isImplemented = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = "Legal Resource Library",
                description = "Access guides, templates, and educational materials about court procedures and your rights.",
                icon = Icons.Default.MenuBook,
                onClick = { /* Will be implemented later */ },
                isImplemented = false
            )

            // Emergency resources section
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Emergency Resources",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Need immediate assistance?",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Legal Aid Hotline: 1-800-XXX-XXXX")
                    Text("Court Support Services: 1-800-XXX-XXXX")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { /* Will be implemented later */ }
                        ) {
                            Text("View All Resources")
                            Spacer(Modifier.width(4.dp))
                            IncompleteFunctionIcon()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isImplemented: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (!isImplemented) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IncompleteFunctionIcon()
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}