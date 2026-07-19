@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AppStateStore
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.DriverDashboardScreen
import com.example.ui.screens.LoginFlowContainer
import com.example.ui.screens.ManagerDashboardScreen
import com.example.ui.screens.WarehouseDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentRole by AppStateStore.currentRole.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentRole,
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            },
                            label = "MainRoleTransition"
                        ) { role ->
                            when (role) {
                                null -> {
                                    LoginFlowContainer { loggedInRole ->
                                        // AppStateStore.currentRole handles routing reactively
                                    }
                                }
                                "customer" -> {
                                    CustomerDashboardScreen {
                                        AppStateStore.currentRole.value = null
                                        AppStateStore.loginStep.value = "role"
                                    }
                                }
                                "driver" -> {
                                    DriverDashboardScreen {
                                        AppStateStore.currentRole.value = null
                                        AppStateStore.loginStep.value = "role"
                                    }
                                }
                                "warehouse" -> {
                                    WarehouseDashboardScreen {
                                        AppStateStore.currentRole.value = null
                                        AppStateStore.loginStep.value = "role"
                                    }
                                }
                                "manager" -> {
                                    ManagerDashboardScreen {
                                        AppStateStore.currentRole.value = null
                                        AppStateStore.loginStep.value = "role"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
