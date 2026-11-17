package com.serviseyem.ui.screens.map.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.utils.VisualThemePalette

/**
 * 👥 GroupTrackingDialog - Modal for joining or sharing a live group tracking session
 */
@Composable
fun GroupTrackingDialog(
    isGroupActive: Boolean,
    currentGroupCode: String,
    userLat: Double = 0.0,
    userLng: Double = 0.0,
    onStartGroupSession: (String) -> Unit,
    onStopGroupSession: () -> Unit,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var groupCodeInput by remember { mutableStateOf(currentGroupCode.ifBlank { "YEMEN-${(100..999).random()}" }) }
    val context = LocalContext.current

    fun shareGroupInvite(code: String) {
        val locPart = if (userLat != 0.0 || userLng != 0.0) {
            "\n📍 موقعي الحالي:\nhttps://www.google.com/maps/search/?api=1&query=$userLat,$userLng"
        } else ""
        val shareText = "👥 انضم إلى مجموعتي في خرائط خدمات اليمن!\n🔑 رمز المجموعة: $code$locPart"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "مشاركة رمز المجموعة")) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, themeColors.accent.copy(alpha = 0.6f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "وضع المجموعة",
                            tint = themeColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "تتبع المجموعة والأصدقاء 👥",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "شارك رمز المجموعة وموقعك مع أصدقائك على الخريطة",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                if (isGroupActive) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🟢 وضع التتبع النشط حالياً",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "رمز المجموعة: $currentGroupCode",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { shareGroupInvite(currentGroupCode) },
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة رمز المجموعة وموقعي", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onStopGroupSession,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إيقاف تتبع المجموعة 🔴", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = groupCodeInput,
                        onValueChange = { groupCodeInput = it.uppercase() },
                        label = { Text("رمز المجموعة الخاص بك") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.accent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (groupCodeInput.isNotBlank()) shareGroupInvite(groupCodeInput.trim()) },
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة الرمز", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = {
                                if (groupCodeInput.isNotBlank()) {
                                    onStartGroupSession(groupCodeInput.trim())
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("تفعيل المجموعة 🟢", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
