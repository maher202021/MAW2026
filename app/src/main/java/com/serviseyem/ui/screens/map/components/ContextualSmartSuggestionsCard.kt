package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import java.util.Calendar

/**
 * 💡 ContextualSmartSuggestionsCard
 * Smart contextual suggestion card overlay for map (100% On-Device, No Complex AI or Paid APIs required).
 * Suggests open services, restaurants/bofeyat, or nearby technicians depending on current hour and location.
 */
@Composable
fun ContextualSmartSuggestionsCard(
    isVisible: Boolean,
    currentCity: String,
    onApplySuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val suggestionInfo = remember(currentHour, currentCity) {
        when {
            currentHour in 6..10 -> Pair(
                "🍳 بوفيهات ومأكولات إفطار شعبية مثرية في $currentCity",
                "بوفيه"
            )
            currentHour in 11..15 -> Pair(
                "🍲 أشهى المطاعم والسلته اليمني القريبة منك الآن في $currentCity",
                "مطعم"
            )
            currentHour in 16..20 -> Pair(
                "🔧 فنيين وميكانيكيين متاحين الآن لخدمتك في $currentCity",
                "ميكانيكي"
            )
            else -> Pair(
                "💊 صيدليات ومراكز طوارئ خافرة تعمل الآن في $currentCity",
                "صيدلية"
            )
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            border = BorderStroke(1.2.dp, themeColors.accent.copy(alpha = 0.8f)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "مقترح لك",
                        tint = themeColors.accent,
                        modifier = Modifier.size(22.dp)
                    )

                    Column {
                        Text(
                            text = "مقترح لك حسب الوقت والمكان 💡",
                            color = themeColors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = suggestionInfo.first,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onApplySuggestion(suggestionInfo.second) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("عرض ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "تجاهل",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
