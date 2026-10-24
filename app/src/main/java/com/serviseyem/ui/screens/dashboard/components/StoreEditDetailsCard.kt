package com.serviseyem.ui.screens.dashboard.components

import android.content.Context
import com.serviseyem.ui.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📝 StoreEditDetailsCard - بطاقة تعديل بيانات المتجر والعنوان والهاتف
 */
@Composable
fun StoreEditDetailsCard(
    store: StoreEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var editName by remember(store) { mutableStateOf(store.name) }
    var editDesc by remember(store) { mutableStateOf(store.description) }
    var editAddress by remember(store) { mutableStateOf(store.localNeighborhood) }
    var editPhone by remember(store) { mutableStateOf(store.phone) }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📝 تعديل بيانات وموقع المتجر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("اسم المتجر / المحل التجارية") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )

            OutlinedTextField(
                value = editDesc,
                onValueChange = { editDesc = it },
                label = { Text("وصف النشاط والخدمات والمنتجات المقدمة") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )

            OutlinedTextField(
                value = editAddress,
                onValueChange = { editAddress = it },
                label = { Text("العنوان بالتفصيل (المحافظة - المديرية - الشارع)") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )

            OutlinedTextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                label = { Text("رقم الهاتف أو الواتساب") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )

            Button(
                onClick = {
                    if (editName.trim().isEmpty() || editPhone.trim().isEmpty()) {
                        viewModel.triggerNotification("⚠️ الاسم والهاتف حقول إجبارية!")
                    } else {
                        viewModel.saveStore(
                            store.copy(
                                name = editName.trim(),
                                description = editDesc.trim(),
                                localNeighborhood = editAddress.trim(),
                                phone = editPhone.trim()
                            )
                        )
                        Toast.makeText(context, "✅ تم حفظ التغييرات بنجاح لمحلّك!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ التحديثات 💾", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
