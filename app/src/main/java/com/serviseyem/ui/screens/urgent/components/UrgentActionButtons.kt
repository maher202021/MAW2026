package com.serviseyem.ui.screens.urgent.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔘 UrgentActionButtons
 * مجموعة أزرار الإجراءات للطلبات العاجلة (تفاصيل، تقديم عرض، اتصال، خريطة، إلغاء).
 */
@Composable
fun UrgentActionButtons(
    isProvider: Boolean,
    status: String,
    themeColors: VisualThemePalette,
    onViewDetails: () -> Unit,
    onSubmitOffer: (() -> Unit)? = null,
    onCallPhone: (() -> Unit)? = null,
    phoneToCall: String? = null,
    onOpenMap: (() -> Unit)? = null,
    onCancelRequest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onViewDetails,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text("عرض التفاصيل", fontSize = 12.sp)
            }

            if (isProvider && status == "WAITING_FOR_OFFERS" && onSubmitOffer != null) {
                Button(
                    onClick = onSubmitOffer,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض فوري", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (onCallPhone != null || onOpenMap != null || (onCancelRequest != null && status == "WAITING_FOR_OFFERS")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onCallPhone != null) {
                    IconButton(
                        onClick = {
                            if (!phoneToCall.isNullOrBlank()) {
                                val cleanPhone = phoneToCall.trim().replace(" ", "").replace("-", "")
                                val isValidPhone = android.util.Patterns.PHONE.matcher(cleanPhone).matches() && cleanPhone.length >= 7
                                if (isValidPhone) {
                                    onCallPhone()
                                } else {
                                    android.widget.Toast.makeText(context, "❌ رقم الهاتف غير صالح للاتصال!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                onCallPhone()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF10B981))
                    }
                }

                if (onOpenMap != null) {
                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "الخريطة", tint = themeColors.accent)
                    }
                }

                if (onCancelRequest != null && status == "WAITING_FOR_OFFERS") {
                    IconButton(
                        onClick = onCancelRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء الطلب", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}
