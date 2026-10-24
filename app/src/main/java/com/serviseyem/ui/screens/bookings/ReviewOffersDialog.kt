package com.serviseyem.ui.screens.bookings

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ReviewOffersDialog(
    request: InstantRequestEntity,
    offers: List<RequestOfferEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onAcceptOffer: (RequestOfferEntity) -> Unit
) {
    val context = LocalContext.current
    val instantReqViewModel: InstantRequestScreenViewModel = hiltViewModel()
    val uiState by instantReqViewModel.uiState.collectAsState()

    val sortedOffers = instantReqViewModel.sortOffers(offers)
    
    var showComplaintDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, themeColors.accent)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("عروض الأسعار (${offers.size})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("قارن واختر الأنسب لك", color = Color.LightGray, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }
                
                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                
                // Sorting options
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.offerSortType == "PRICE",
                        onClick = { instantReqViewModel.setOfferSortType("PRICE") },
                        label = { Text("الأقل سعراً", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = uiState.offerSortType == "RATING",
                        onClick = { instantReqViewModel.setOfferSortType("RATING") },
                        label = { Text("الأعلى تقييماً", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = uiState.offerSortType == "TIME",
                        onClick = { instantReqViewModel.setOfferSortType("TIME") },
                        label = { Text("الأسرع وصولاً", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                    )
                }

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedOffers) { offer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF3B82F6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(offer.technicianName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text(offer.technicianName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = if (offer.technicianRating > 0) "⭐ ${offer.technicianRating}" else "فني جديد",
                                                    color = Color(0xFFF59E0B),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${offer.price} ريال", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("الوصول: ${offer.estimatedArrivalTime}", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }
                                
                                if (offer.notes.isNotBlank()) {
                                    Text(
                                        text = offer.notes,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A), RoundedCornerShape(8.dp)).padding(8.dp)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            instantReqViewModel.setComplaintTarget(offer.technicianId)
                                            showComplaintDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تقديم شكوى/تبليغ", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    
                                    Button(
                                        onClick = { onAcceptOffer(offer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("قبول هذا العرض", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    if (sortedOffers.isEmpty()) {
                        item {
                            Text("لا توجد عروض حتى الآن.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                        }
                    }
                }
            }
        }
    }
    
    if (showComplaintDialog) {
        var complaintText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showComplaintDialog = false },
            title = { Text("تقديم شكوى ضد مقدم العرض", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("يرجى كتابة تفاصيل الشكوى ليتم مراجعتها من قبل الإدارة:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = complaintText,
                        onValueChange = { complaintText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        placeholder = { Text("تفاصيل الشكوى...", color = Color.Gray) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (complaintText.isNotBlank()) {
                            isSubmitting = true
                            viewModel.submitReport(
                                report = com.serviseyem.data.ReportEntity(
                                    id = "rep_${System.currentTimeMillis()}",
                                    reporterName = viewModel.currentUserName.value,
                                    reporterPhone = viewModel.currentUserPhone.value,
                                    providerId = uiState.complaintTarget ?: "",
                                    reason = complaintText,
                                    status = "PENDING",
                                    timestamp = System.currentTimeMillis()
                                )
                            ) {
                                isSubmitting = false
                                Toast.makeText(context, "تم رفع الشكوى للإدارة بنجاح", Toast.LENGTH_SHORT).show()
                                showComplaintDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text("إرسال الشكوى", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintDialog = false }) { Text("إلغاء", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
