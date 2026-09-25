package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.NavDestination
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.AdnMineroTheme
import com.example.ui.theme.FaenaBg
import com.example.ui.theme.SurfaceDark
import com.example.viewmodel.MiningViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MiningViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            // Show snackbar notifications when userNotification is triggered
            LaunchedEffect(uiState.userNotification) {
                uiState.userNotification?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearNotification()
                }
            }

            AdnMineroTheme(isModoFaena = uiState.isModoFaena) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (uiState.isModoFaena) FaenaBg else SurfaceDark),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            TopBar(
                                isModoFaena = uiState.isModoFaena,
                                onSosClick = { viewModel.openSosDialog() },
                                onCoffeeClick = { viewModel.openCoffeeDialog() }
                            )
                            LiveTickerRibbon(
                                isModoFaena = uiState.isModoFaena,
                                onToggleModoFaena = { viewModel.toggleModoFaena() },
                                quotes = uiState.marketQuotes
                            )
                        }
                    },
                    bottomBar = {
                        BottomNavBar(
                            currentDestination = uiState.currentDestination,
                            onDestinationSelect = { dest ->
                                viewModel.closeMineDetail()
                                viewModel.setDestination(dest)
                            },
                            isModoFaena = uiState.isModoFaena
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(if (uiState.isModoFaena) FaenaBg else SurfaceDark)
                    ) {
                        AnimatedContent(
                            targetState = Pair(uiState.selectedMineDetail != null, uiState.currentDestination),
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { (hasMineDetail, currentDest) ->
                            if (hasMineDetail && uiState.selectedMineDetail != null) {
                                MineDetailScreen(
                                    mine = uiState.selectedMineDetail!!,
                                    state = uiState,
                                    viewModel = viewModel
                                )
                            } else {
                                when (currentDest) {
                                    NavDestination.INICIO -> InicioFaenaScreen(
                                        state = uiState,
                                        viewModel = viewModel
                                    )
                                    NavDestination.VOZ -> NoticiasVozScreen(
                                        state = uiState,
                                        viewModel = viewModel
                                    )
                                    NavDestination.ALERTAS -> AlertasScreen(
                                        state = uiState,
                                        viewModel = viewModel
                                    )
                                    NavDestination.PANELES -> PanelesScreen(
                                        state = uiState,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }

                    // Dialogs
                    if (uiState.showSosDialog) {
                        SosEmergencyDialog(
                            onDismiss = { viewModel.closeSosDialog() }
                        )
                    }

                    if (uiState.showCoffeeDialog) {
                        CoffeeSupportDialog(
                            onDismiss = { viewModel.closeCoffeeDialog() }
                        )
                    }
                }
            }
        }
    }
}
