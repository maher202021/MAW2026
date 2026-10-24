package com.serviseyem.ui.screens.about

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * About App Info Dialog overlay.
 * Refactored using MVVM to delegate states to [AboutViewModel].
 */
@Composable
fun AboutAppDialogView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    aboutViewModel: AboutViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by aboutViewModel.uiState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.activity.compose.BackHandler(enabled = true, onBack = onDismiss)
        Surface(
            color = themeColors.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top header bar for the full-screen about page
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "معلومات عن التطبيق",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (val state = uiState) {
                        is AboutUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = themeColors.primary)
                            }
                        }
                        is AboutUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.message,
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        is AboutUiState.Success -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (state.isAdmin) {
                                    AboutLayoutEditor(
                                        settingsState = state.settings,
                                        themeColors = themeColors,
                                        viewModel = aboutViewModel
                                    )
                                }
                                AboutContentRenderer(
                                    settingsState = state.settings,
                                    themeColors = themeColors
                                )
                            }
                        }
                        is AboutUiState.Editing -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AboutLayoutEditor(
                                    settingsState = state.settings,
                                    themeColors = themeColors,
                                    viewModel = aboutViewModel
                                )
                                AboutContentRenderer(
                                    settingsState = state.settings,
                                    themeColors = themeColors
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * About App Info Screen Content view for full-screen navigation routing.
 */
@Composable
fun AboutAppScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    aboutViewModel: AboutViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by aboutViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Top header bar for the screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.primary)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "معلومات عن التطبيق",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is AboutUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeColors.primary)
                    }
                }
                is AboutUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
                is AboutUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.isAdmin) {
                            AboutLayoutEditor(
                                settingsState = state.settings,
                                themeColors = themeColors,
                                viewModel = aboutViewModel
                            )
                        }
                        AboutContentRenderer(
                            settingsState = state.settings,
                            themeColors = themeColors
                        )
                    }
                }
                is AboutUiState.Editing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AboutLayoutEditor(
                            settingsState = state.settings,
                            themeColors = themeColors,
                            viewModel = aboutViewModel
                        )
                        AboutContentRenderer(
                            settingsState = state.settings,
                            themeColors = themeColors
                        )
                    }
                }
            }
        }
    }
}
