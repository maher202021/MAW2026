package com.serviseyem.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.serviseyem.ui.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 🏷️ نوع حوار التأكيد
 */
enum class ConfirmationDialogType {
    CONFIRM,   // تأكيد اعتيادي
    WARNING,   // تحذير أو إجراء حساس
    SUCCESS,   // إشعار نجاح
    ERROR      // تنبيه لخطأ ما
}

/**
 * 🔔 ConfirmationDialog
 * حوار تأكيد عام وشامل قابل لإعادة الاستخدام في جميع شاشات التطبيق
 * متوافق مع معايير Material Design 3 ويدعم حالات التحميل والإجراءات التدميرية.
 */
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "تأكيد",
    cancelLabel: String = "إلغاء",
    dialogType: ConfirmationDialogType = ConfirmationDialogType.CONFIRM,
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val icon: ImageVector
    val primaryColor: Color
    val secondaryBgColor: Color

    when (dialogType) {
        ConfirmationDialogType.CONFIRM -> {
            icon = Icons.Default.Info
            primaryColor = if (isDestructive) Color(0xFFEF4444) else Color(0xFF00E5FF)
            secondaryBgColor = primaryColor.copy(alpha = 0.12f)
        }
        ConfirmationDialogType.WARNING -> {
            icon = Icons.Default.Warning
            primaryColor = Color(0xFFF59E0B)
            secondaryBgColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
        }
        ConfirmationDialogType.SUCCESS -> {
            icon = Icons.Default.CheckCircle
            primaryColor = Color(0xFF10B981)
            secondaryBgColor = Color(0xFF10B981).copy(alpha = 0.12f)
        }
        ConfirmationDialogType.ERROR -> {
            icon = Icons.Default.Close
            primaryColor = Color(0xFFEF4444)
            secondaryBgColor = Color(0xFFEF4444).copy(alpha = 0.12f)
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.9f)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("confirmation_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(secondaryBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Title
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("confirmation_dialog_title")
                    )

                    // Message Content
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.testTag("confirmation_dialog_message")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("confirmation_dialog_cancel_button")
                        ) {
                            Text(
                                text = cancelLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Confirm / Action Button
                        Button(
                            onClick = onConfirm,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDestructive) Color(0xFFDC2626) else primaryColor,
                                contentColor = if (isDestructive || dialogType == ConfirmationDialogType.ERROR) Color.White else Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("confirmation_dialog_confirm_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (isDestructive) Color.White else Color(0xFF0F172A),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = confirmLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
