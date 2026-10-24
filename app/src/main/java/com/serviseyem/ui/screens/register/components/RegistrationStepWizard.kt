package com.serviseyem.ui.screens.register.components

import androidx.compose.animation.*
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🧙‍♂️ RegistrationStepWizard
 * Step progress indicator bar with active step animations and accessibility semantics.
 */
@Composable
fun RegistrationStepWizard(
    currentStep: Int,
    totalSteps: Int = 3,
    stepTitles: List<String> = listOf("البيانات الأساسية", "تفاصيل النشاط", "الأمان والمستندات"),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "معالج خطوات التسجيل: الخطوة $currentStep من $totalSteps" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                val isCompleted = i < currentStep
                val isCurrent = i == currentStep

                val circleBg = when {
                    isCompleted -> Color(0xFF10B981)
                    isCurrent -> Color(0xFF00E5FF)
                    else -> Color(0xFF334155)
                }

                val textColor = when {
                    isCompleted || isCurrent -> Color(0xFF0F172A)
                    else -> Color(0xFF94A3B8)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "مكتمل",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$i",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    if (stepTitles.size >= i) {
                        Text(
                            text = stepTitles[i - 1],
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                        )
                    }
                }

                if (i < totalSteps) {
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        thickness = 2.dp,
                        color = if (i < currentStep) Color(0xFF10B981) else Color(0xFF334155)
                    )
                }
            }
        }
    }
}
