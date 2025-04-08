// MainActivity.kt
package com.example.helpplease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.helpplease.screens.DocumentRehumanizationScreen
import com.example.helpplease.screens.SelfRepresentingDashboardScreen  // Add this line

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelpPleaseApp()
        }
    }
}

@Composable
fun HelpPleaseApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = "welcome") {
            composable("welcome") {
                    WelcomeScreen(navController = navController)
                }
                composable("user_type") {
                    UserTypeScreen(navController = navController)
                }
                // Add placeholder screens
                composable("lawyer_dashboard") {
                    LawyerDashboardScreen(navController = navController)
                }
                composable("self_representing_dashboard") {
                    SelfRepresentingDashboardScreen(navController = navController)
                }
                composable("find_lawyer") {
                    FindLawyerScreen(navController = navController)
                }
                composable("contribute") {
                    ContributeScreen(navController = navController)
                }
                composable("document_rehumanization") {
                    DocumentRehumanizationScreen(navController = navController)
                }
            }
        }
    }
}