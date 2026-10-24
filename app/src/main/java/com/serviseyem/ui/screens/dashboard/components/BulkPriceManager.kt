package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 💰 BulkPriceManager (إدارة وتعديل الأسعار الجماعية)
 * إمكانية تطبيق زيادة أو تخفيض بنسبة مئوية أو بمبلغ ثابت على كافة المنتجات أو أقسام معينة دفعة واحدة.
 */
@Composable
fun BulkPriceManager(
    themeColors: VisualThemePalette,
    onApplyBulkUpdate: (category: String, percentChange: Double, isIncrease: Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("كافة المنتجات والخدمات") }
    var percentage by remember { mutableStateOf("10") }
    var isIncrease by remember { mutableStateOf(false) } // False = discount/reduction, True = increase
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF10B981))
                Text(
                    text = "تعديل الأسعار الجماعي (Bulk Price Manager)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "يمكنك تعديل أسعار المنتجات أو الخدمات دفعة واحدة بنسبة مئوية موحدة لمواكبة تغيرات السوق أو إطلاق مواسم تخفيضات شاملة.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            // Category selector
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { selectedCategory = it },
                label = { Text("القسم المستهدف للتعديل") },
                modifier = Modifier.fillMaxWidth()
            )

            // Adjustment Type (Increase vs Decrease)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !isIncrease,
                    onClick = { isIncrease = false },
                    label = { Text("تخفيض / خصم عام (%)", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF10B981)) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = isIncrease,
                    onClick = { isIncrease = true },
                    label = { Text("زيادة سعرية (%)", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color(0xFFEF4444)) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = percentage,
                onValueChange = { percentage = it },
                label = { Text("نسبة التعديل المئوية (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981)) },
                modifier = Modifier.fillMaxWidth()
            )

            statusMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val rate = percentage.toDoubleOrNull() ?: 0.0
                    onApplyBulkUpdate(selectedCategory, rate, isIncrease)
                    statusMessage = if (isIncrease) {
                        "تم تطبيق زيادة بنسبة $percentage% بنجاح على $selectedCategory"
                    } else {
                        "تم تطبيق خصم شامل بنسبة $percentage% بنجاح على $selectedCategory"
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncrease) Color(0xFFEF4444) else Color(0xFF10B981),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isIncrease) "تطبيق زيادة الأسعار الجماعية" else "تطبيق التخفيض الجماعي",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
