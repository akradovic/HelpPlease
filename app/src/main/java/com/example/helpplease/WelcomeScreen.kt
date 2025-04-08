// WelcomeScreen.kt
package com.example.helpplease

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo - using the "scales of justice" theme from your sketches
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFF0E6FF), shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HP",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFDAA520) // Gold color for HP letters
            )
            // You'll replace this with an actual logo image later
            // Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Help Please Logo")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "HELP PLEASE",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Legal Resource Navigator",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("user_type") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Get Started")
        }
    }
}