package com.serviseyem.ui.screens.register.components

import android.content.Context
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 📜 RegistrationTermsCheckbox - خانة الموافقة على شروط الاستخدام مع نافذة تفصيلية وتخزين الموافقة
 */
@Composable
fun RegistrationTermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Auto-load previous consent
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("yemen_services_terms", Context.MODE_PRIVATE)
        val agreedBefore = prefs.getBoolean("has_agreed_terms", false)
        if (agreedBefore && !checked) {
            onCheckedChange(true)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                val newChecked = !checked
                onCheckedChange(newChecked)
                context.getSharedPreferences("yemen_services_terms", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("has_agreed_terms", newChecked)
                    .apply()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { isChecked ->
                onCheckedChange(isChecked)
                context.getSharedPreferences("yemen_services_terms", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("has_agreed_terms", isChecked)
                    .apply()
            },
            colors = CheckboxDefaults.colors(
                checkedColor = themeColors.accent,
                checkmarkColor = Color.Black,
                uncheckedColor = Color.Gray
            )
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "أوافق وأتعهد بالالتزام بـ",
                fontSize = 11.sp,
                color = Color.LightGray
            )
            Text(
                text = "شروط الاستخدام وسياسة الخصوصية",
                fontSize = 11.sp,
                color = themeColors.accent,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { showTermsDialog = true }
            )
        }
    }

    // Scrollable Detailed Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "📜 ميثاق وشروط دليل خدمات اليمن",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. المصداقية والأمانة:",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "يلتزم مزود الخدمة أو العميل بتقديم بيانات دقيقة وصحيحة وتجنب أي أسماء أو أرقام وهمية تحت طائلة الحظر الفوري.",
                        color = Color.LightGray,
                        fontSize = 10.5.sp
                    )

                    Text(
                        text = "2. المعاملات والأسعار:",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "يتم الاتفاق المالي بين الطرفين بكل وضوح وشفافية ودون مغالاة، وفق التسعيرة المحلية العادلة داخل الجمهورية اليمنية.",
                        color = Color.LightGray,
                        fontSize = 10.5.sp
                    )

                    Text(
                        text = "3. سرية البيانات:",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "نحن نحافظ على سرية بياناتك الشخصية ولا نشاركها مع أي أطراف غير مصرح بها، وتستخدم فقط لربطك بالخدمات المطلوبة.",
                        color = Color.LightGray,
                        fontSize = 10.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCheckedChange(true)
                        context.getSharedPreferences("yemen_services_terms", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("has_agreed_terms", true)
                            .apply()
                        showTermsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("موافق ومتابع ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("إغلاق", color = Color.Gray, fontSize = 11.sp)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
