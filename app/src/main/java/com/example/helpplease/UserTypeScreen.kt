// UserTypeScreen.kt
package com.example.helpplease

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// UserTypeScreen.kt - Update UserTypeScreen
@Composable
fun UserTypeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Are you:",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        UserTypeOption(
            text = "A Lawyer?",
            backgroundColor = Color(0xFFFFF9C4), // Light yellow
            onClick = { navController.navigate("lawyer_dashboard") },
            isImplemented = false // Mark as not implemented
        )

        UserTypeOption(
            text = "Self Representing?",
            backgroundColor = Color(0xFFBBDEFB), // Light blue
            onClick = { navController.navigate("self_representing_dashboard") },
            isImplemented = true // Mark as not implemented
        )

        UserTypeOption(
            text = "Need a Lawyer?",
            backgroundColor = Color(0xFFFFCCBC), // Light orange/pink
            onClick = { navController.navigate("find_lawyer") },
            isImplemented = false // Mark as not implemented
        )

        UserTypeOption(
            text = "Have information or experience to contribute?",
            backgroundColor = Color(0xFFDCEDC8), // Light green
            onClick = { navController.navigate("contribute") },
            isImplemented = false // Mark as not implemented
        )
    }
}

// UserTypeScreen.kt - Update the UserTypeOption
@Composable
fun UserTypeOption(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    isImplemented: Boolean = false // Add parameter to indicate implementation status
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )

            // Show icon for incomplete features
            if (!isImplemented) {
                IncompleteFunctionIcon()
                Modifier
                    .align(Alignment.TopEnd)
            }
        }
    }
}