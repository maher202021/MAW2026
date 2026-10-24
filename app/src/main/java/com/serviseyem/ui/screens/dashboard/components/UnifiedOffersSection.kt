package com.serviseyem.ui.screens.dashboard.components

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎁 Unified Offers Section Component
 */
@Composable
fun UnifiedOffersSection(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var offerTitle by remember { mutableStateOf("") }
    var offerDiscount by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🎁 العروض والتخفيضات الخاصة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("إضافة عرض ترويجي جديد مع نسبة الخصم:", fontSize = 11.sp, color = Color.White)

                OutlinedTextField(
                    value = offerTitle,
                    onValueChange = { offerTitle = it },
                    label = { Text("عنوان العرض الترويجي (مثل: خصم 20% لفترة محدودة)", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = offerDiscount,
                    onValueChange = { offerDiscount = it },
                    label = { Text("نسبة الخصم % (مثال: 20)", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (offerTitle.isNotBlank()) {
                            Toast.makeText(context, "🎉 تم نشر العرض الترويجي بنجاح!", Toast.LENGTH_SHORT).show()
                            offerTitle = ""
                            offerDiscount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("نشر العرض للعملاء 📢", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
