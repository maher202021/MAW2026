package com.serviseyem.ui.screens.urgent

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.RequestOfferEntity

/**
 * 🏷️ UrgentOffersList
 * Displays list of provider offers with live price, arrival time, rating, and call/accept controls.
 */
@Composable
fun UrgentOffersList(
    offers: List<RequestOfferEntity>,
    isOwner: Boolean,
    onAcceptOffer: (RequestOfferEntity) -> Unit,
    onContactProvider: (RequestOfferEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "📬 العروض المقدمة (${offers.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        if (offers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFD32F2F),
                    strokeWidth = 2.5.dp
                )
                Text(
                    text = "⏳ جاري البحث عن عروض مقدمي الخدمة والمحلات والمراكز المتاحة بالمنطقة...",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(offers, key = { it.id }) { offer ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👨‍🔧", fontSize = 18.sp)
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = offer.technicianName.ifBlank { "فني متخصص" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            // حالة الفني متاح / مشغول
                                            Surface(
                                                color = if (offer.isAvailable) Color(0xFFDCFCE7) else Color(0xFFFFE4E6),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (offer.isAvailable) "🟢 متاح الآن" else "🔴 مشغول",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (offer.isAvailable) Color(0xFF166534) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "⭐ ${offer.technicianRating} / 5.0",
                                            fontSize = 11.sp,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%,.0f", offer.price)} ر.ي",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = offer.estimatedArrivalTime,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            if (offer.notes.isNotBlank()) {
                                Text(
                                    text = "💬 ${offer.notes}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            if (isOwner && offer.status == "PENDING") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAcceptOffer(offer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("قبول العرض ✅", fontSize = 12.sp, color = Color.White)
                                    }
                                    OutlinedButton(
                                        onClick = { onContactProvider(offer) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("دردشة 💬", fontSize = 12.sp)
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
