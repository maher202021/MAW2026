package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.delay

@Composable
fun AdminCustomBannerView(settingsState: AdminSettingsEntity, themeColors: VisualThemePalette) {
    var isVisible by remember(settingsState.bannerContent, settingsState.bannerEnabled) { mutableStateOf(true) }
    
    if (settingsState.bannerDurationSeconds > 0) {
        LaunchedEffect(settingsState.bannerContent, settingsState.bannerEnabled) {
            delay(settingsState.bannerDurationSeconds * 1000L)
            isVisible = false
        }
    }

    if (!isVisible || !settingsState.bannerEnabled) return

    val imageBitmap = remember(settingsState.bannerBase64) {
        if (!settingsState.bannerBase64.isNullOrEmpty()) {
            try {
                val bytes = android.util.Base64.decode(settingsState.bannerBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch(e: Exception) { null }
        } else null
    }

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            startAnimation = true
        }
    }

    val style = settingsState.bannerDisplayStyle ?: "SLIDE"

    // Animations corresponding to display styles
    val slideOffset by animateDpAsState(
        targetValue = if (startAnimation && style == "SLIDE") 0.dp else if (style == "SLIDE") (-300).dp else 0.dp,
        animationSpec = tween(800)
    )

    val fadeAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scrollOffset by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val appliedModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .let { modifier ->
            when (style) {
                "SLIDE" -> modifier.offset(x = slideOffset)
                "FADE" -> modifier.alpha(fadeAlpha)
                "BLINK" -> modifier.alpha(blinkAlpha)
                "SCROLL" -> modifier.offset(x = scrollOffset.dp)
                else -> modifier
            }
        }

    Card(
        modifier = appliedModifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, themeColors.accent.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header row with title and type icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (settingsState.bannerType) {
                            "IMAGE" -> "🖼️"
                            "VIDEO" -> "📹"
                            else -> "📢"
                        }
                        Text(icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إعلان رسمي من إدارة المنصة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                    }
                    
                    // Dismiss button
                    IconButton(
                        onClick = { isVisible = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                when (settingsState.bannerType) {
                    "IMAGE" -> {
                        val isRemoteUrl = settingsState.bannerContent.startsWith("http://") || settingsState.bannerContent.startsWith("https://")
                        if (isRemoteUrl) {
                            SmartAsyncImage(
                                model = settingsState.bannerContent,
                                contentDescription = "إعلان بنر",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "إعلان بنر",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Load from remote URL or show decorative placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📢 إعلان مرئي", fontSize = 16.sp, color = themeColors.accent)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(settingsState.bannerContent, fontSize = 11.sp, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }
                    }
                    "VIDEO" -> {
                        // Simulated high-fidelity Video Player card as requested
                        var isPlaying by remember { mutableStateOf(true) }
                        var simulatedProgress by remember { mutableStateOf(0.4f) }
                        
                        LaunchedEffect(isPlaying) {
                            if (isPlaying) {
                                while (true) {
                                    delay(1000L)
                                    simulatedProgress = (simulatedProgress + 0.05f)
                                    if (simulatedProgress >= 1.0f) simulatedProgress = 0.0f
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "فيديو البنر",
                                    modifier = Modifier.fillMaxSize().alpha(0.6f),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).alpha(0.5f))
                            }
                            
                            // REC Indicator
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LIVE", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }

                            // Play/Pause Overlay Button
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Text(if (isPlaying) "⏸️" else "▶️", fontSize = 18.sp)
                            }

                            // Video Controls Overlay at Bottom
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (settingsState.bannerContent.length > 30) settingsState.bannerContent.take(30) + "..." else settingsState.bannerContent,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                    Text("00:0${(simulatedProgress * 15).toInt()} / 00:15", fontSize = 9.sp, color = Color.LightGray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Custom progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(Color.Gray)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(simulatedProgress)
                                            .fillMaxHeight()
                                            .background(themeColors.accent)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Text Banner
                        val welcomeMsg = "أهلاً بكم في دليل خدمات اليمن! المنصة الأولى لربط مقدمي الخدمات والمهنيين والمراكز التجارية مع المستخدمين، وانتظروا الإضافات القادمة! ✨"
                        val displayText = if (settingsState.bannerContent.contains("خصومات") || settingsState.bannerContent.contains("الصيانة الكهربائية") || settingsState.bannerContent.isBlank()) {
                            welcomeMsg
                        } else {
                            settingsState.bannerContent
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.accent.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📢", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = displayText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
