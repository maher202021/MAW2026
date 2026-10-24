package com.serviseyem.ui.screens.admin

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📢 AdminReportsPanel
 * قسم إدارة البلاغات والشكاوى الواردة وتصديرها بصيغة PDF / CSV وتجميد الحسابات
 */
@Composable
fun AdminReportsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reports by viewModel.reports.collectAsState()
    var complaintsSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "📢 البلاغات الواردة وشكاوى المواطنين (${reports.size}):",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "استخدم الفلتر الذكي للبحث عن بلاغات فني أو مواطن معين وتصدير السجلات:",
            fontSize = 11.sp,
            color = themeColors.textSecondary
        )

        OutlinedTextField(
            value = complaintsSearchQuery,
            onValueChange = { complaintsSearchQuery = it },
            label = { Text("بحث باسم الفني أو المشتكي...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = themeColors.accent
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.exportComplaintsToCSV()
                    Toast.makeText(context, "تم تصدير سجل الشكاوى بصيغة CSV بنجاح 📁", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f)
            ) {
                Text("تصدير CSV 💾", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    viewModel.exportComplaintsToPDF()
                    Toast.makeText(context, "تم تصدير مستند الشكاوى بصيغة PDF بنجاح 📄", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.weight(1f)
            ) {
                Text("تصدير PDF 📄", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        val filteredComplaints = reports.filter {
            it.providerName.contains(complaintsSearchQuery, ignoreCase = true) ||
            it.reporterName.contains(complaintsSearchQuery, ignoreCase = true) ||
            it.content.contains(complaintsSearchQuery, ignoreCase = true)
        }

        if (filteredComplaints.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "لا توجد بلاغات تفرز معايير البحث المسجلة.",
                    fontSize = 11.sp,
                    color = themeColors.textSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredComplaints.forEach { rep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "الفني المشكو ضده: ${rep.providerName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "اسم المواطن الشاكي: ${rep.reporterName}",
                                fontSize = 11.sp,
                                color = themeColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("مضمون ومحتوى البلاغ: ${rep.content}", fontSize = 12.sp, color = Color.White)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.deleteReport(rep.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تجاوز وحذف البلاغ 🗑️", fontSize = 10.sp, color = Color.White)
                                }

                                if (rep.providerId.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            viewModel.toggleProviderSubscription(rep.providerId, "SUSPENDED")
                                            Toast.makeText(context, "تم تجميد وإيقاف حساب الفني بنجاح 🛑", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("تجميد حساب الفني 🛑", fontSize = 10.sp, color = Color.White)
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
