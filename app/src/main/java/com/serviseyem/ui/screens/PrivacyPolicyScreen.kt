@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens
import com.serviseyem.ui.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit = {},
    themeColors: VisualThemePalette
) {
    val cardBg = themeColors.surface
    val textColor = themeColors.textPrimary
    val primaryColor = themeColors.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سياسة الخصوصية وحماية البيانات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.background,
                    titleContentColor = textColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "سياسة الخصوصية لتطبيق دليل خدمات اليمن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "التزامنا التام بحماية وسرية بيانات المستخدمين والمهنيين",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        "نحن نلتزم بحماية بياناتك الشخصية وموقعك الجغرافي. يتم استخدام الموقع فقط لتسهيل الوصول إلى المزودين القريبين منك دون مشاركتها مع أطراف ثالثة إلا لغرض إتمام الخدمة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 22.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrivacyItem(
                        icon = Icons.Default.Check,
                        title = "1. البيانات التي نجمعها",
                        desc = "الاسم ورقم الهاتف والموقع التقريبي لتسهيل ربطك بالفنيين والمتاجر القريبة منك."
                    )
                    PrivacyItem(
                        icon = Icons.Default.Lock,
                        title = "2. سرية وحماية المدفوعات",
                        desc = "كافة عمليات التحويل والسندات المالية يتم التحقق منها بصورة مشفرة ومؤمنة تماماً."
                    )
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            var hasAgreed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasAgreed,
                            onCheckedChange = { hasAgreed = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "أوافق على جميع بنود سياسة الخصوصية والشروط",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Button(
                        onClick = {
                            if (hasAgreed) {
                                val sp = context.getSharedPreferences("privacy_prefs", android.content.Context.MODE_PRIVATE)
                                sp.edit().putBoolean("privacy_agreed", true).apply()
                                onBack()
                            } else {
                                android.widget.Toast.makeText(context, "يرجى الموافقة على سياسة الخصوصية أولاً", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasAgreed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("موافق ومتابعة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
