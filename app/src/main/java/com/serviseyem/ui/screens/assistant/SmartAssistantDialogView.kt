@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.assistant
import com.serviseyem.ui.*

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.screens.assistant.components.*
import com.serviseyem.NetworkUtils
import com.serviseyem.VoiceManager
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.viewmodels.AssistantViewModel
import kotlinx.coroutines.launch
import java.util.UUID

data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val matchedEntities: List<Any> = emptyList()
)

/**
 * 🤖 Modern Smart Assistant Dialog View
 * Cleanly modularized view using AssistantHeader, AssistantChipsRow, AssistantMessageItem, and AssistantInputBar.
 */
@Composable
fun SmartAssistantDialogView(
    viewModel: MainViewModel,
    settings: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onChatOpen: (String) -> Unit,
    assistantViewModel: AssistantViewModel = viewModel(),
    onRequestQuickService: (() -> Unit)? = null,
    onNavigateToMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val isOnline = remember(context) {
        try { NetworkUtils.isNetworkAvailable(context) } catch (_: Exception) { true }
    }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val chatHistory by assistantViewModel.chatHistory.collectAsState()
    val typedText by assistantViewModel.typedText.collectAsState()
    val isGenerating by assistantViewModel.isGenerating.collectAsState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenMatches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                assistantViewModel.updateTypedText(spokenText)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🎙️ تم الاستماع: '$spokenText'")
                }
            }
        }
    }

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    BackHandler(enabled = true, onBack = onDismiss)

    Surface(
        color = themeColors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize() // FIXED: Removed root imePadding to prevent dialog background squishing
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                AssistantHeader(
                    isOnline = isOnline,
                    themeColors = themeColors,
                    onClearChat = {
                        assistantViewModel.clearChat(settings.welcomeMessage)
                        coroutineScope.launch { snackbarHostState.showSnackbar("🧹 تم مسح سجل المحادثة") }
                    },
                    onDismiss = onDismiss
                )

                // Navigation Chips
                AssistantChipsRow(
                    themeColors = themeColors,
                    onRequestQuickService = {
                        onDismiss()
                        if (onRequestQuickService != null) {
                            onRequestQuickService.invoke()
                        } else {
                            viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST)
                        }
                    },
                    onNavigateTechnicians = {
                        onDismiss()
                        viewModel.openBrowserTab("الرئيسية")
                        viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    },
                    onNavigateStores = {
                        onDismiss()
                        viewModel.openBrowserTab("المحلات والمتاجر")
                        viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    },
                    onNavigateRestaurants = {
                        onDismiss()
                        viewModel.openBrowserTab("المطاعم والكافيهات")
                        viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    },
                    onNavigateMedical = {
                        onDismiss()
                        viewModel.openBrowserTab("المراكز الطبية")
                        viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    },
                    onNavigateToMap = { 
                        onDismiss()
                        onNavigateToMap()
                    }
                )

                    // Voice Recognition Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم الخدمة أو الفني أو المشكلة...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    try {
                                        VoiceManager.onHear?.invoke { spoken ->
                                            if (spoken.isNotEmpty()) {
                                                assistantViewModel.updateTypedText(spoken)
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎙️", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "البحث الصوتي الذكي",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.accent
                                    )
                                    Text(
                                        text = "انقر للتحدث أو البحث بصوتك فوراً",
                                        fontSize = 9.5.sp,
                                        color = themeColors.textSecondary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(themeColors.accent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("تحدث الآن", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Chat Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatHistory, key = { it.id }) { msg ->
                            AssistantMessageItem(
                                msg = msg,
                                viewModel = viewModel,
                                themeColors = themeColors,
                                onRequestQuickService = {
                                    onDismiss()
                                    if (onRequestQuickService != null) {
                                        onRequestQuickService.invoke()
                                    } else {
                                        viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST)
                                    }
                                },
                                onNavigateToMap = onNavigateToMap,
                                onChatOpen = onChatOpen
                            )
                        }

                        if (isGenerating) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = themeColors.accent,
                                                strokeWidth = 2.dp
                                            )
                                            Text("جاري توليد الإجابة الذكية...", fontSize = 11.sp, color = themeColors.textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Prompt Suggestions
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val promptSuggestions = listOf(
                            "🔧 أقرب سباك في منطقتي",
                            "⚡ كهربائي منازل فوري",
                            "❄️ صيانة وتعبئة تكييف",
                            "🩺 عيادات ومراكز طبية",
                            "🚗 ميكانيكي وبنشر متنقل",
                            "🧹 نقل أثاث ونظافة"
                        )
                        items(promptSuggestions) { pText ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(themeColors.surface)
                                    .border(0.8.dp, themeColors.border, RoundedCornerShape(12.dp))
                                    .clickable { assistantViewModel.updateTypedText(pText.substringAfter(" ")) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(pText, fontSize = 10.sp, color = themeColors.textPrimary)
                            }
                        }
                    }

                    // Input Bar (Anchored directly above keyboard)
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()) {
                        AssistantInputBar(
                            typedText = typedText,
                            isGenerating = isGenerating,
                            themeColors = themeColors,
                            onTextChanged = { assistantViewModel.updateTypedText(it) },
                            onSend = {
                                try {
                                    if (typedText.isNotBlank() && !isGenerating) {
                                        assistantViewModel.sendUserQuery(
                                            prompt = typedText,
                                            isOnline = isOnline,
                                            mainViewModel = viewModel,
                                            settings = settings,
                                            onSpeechSpeak = {
                                                try { VoiceManager.onSpeak?.invoke(it) } catch (_: Exception) {}
                                            },
                                            onError = { err ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                                            }
                                        )
                                    }
                                } catch (e: Exception) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("تعذر معالجة الطلب: ${e.localizedMessage}") }
                                }
                            },
                            onMicClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم الخدمة أو الفني أو المشكلة...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    try {
                                        VoiceManager.onHear?.invoke { spoken ->
                                            if (spoken.isNotEmpty()) {
                                                assistantViewModel.updateTypedText(spoken)
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
