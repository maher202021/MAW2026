package com.serviseyem.ui.screens.about

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 🌟 شاشة معلومات عن التطبيق والدعم الفني المطورة
 * تعرض هوية التطبيق، الخدمات المتاحة، وسائل التواصل والدعم، وروابط التحميل
 */
@Composable
fun AboutContentRenderer(
    settingsState: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appTitle = settingsState.appName.ifBlank { "دليل الخدمات الشامل في اليمن" }
    val appVersion = settingsState.appVersion.ifBlank { "v2.5.0 Gold Edition" }
    val supportPhone = settingsState.supportPhone.ifBlank { "777644" }
    val supportWhatsapp = settingsState.supportWhatsapp.ifBlank { "777644" }
    val supportEmail = settingsState.supportEmail.ifBlank { "" }

    val openUrl = { url: String ->
        if (url.isNotBlank()) {
            try {
                val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.w("AboutContentRenderer", "Failed to open URL: $url", e)
            }
        }
    }

    val openWhatsapp = { phone: String ->
        val cleanNumber = phone.replace("+", "").replace(" ", "").replace("-", "")
        val url = "https://wa.me/$cleanNumber"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.w("AboutContentRenderer", "Failed to open WhatsApp: $phone", e)
        }
    }

    val openDialer = { phone: String ->
        val uri = Uri.parse("tel:$phone")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.w("AboutContentRenderer", "Failed to open Dialer: $phone", e)
        }
    }

    val openEmail = { email: String ->
        val uri = Uri.parse("mailto:$email")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.w("AboutContentRenderer", "Failed to open Email: $email", e)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Hero Brand Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // App Logo Icon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFD97706), Color(0xFFB45309))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🇾🇪",
                        fontSize = 38.sp
                    )
                }

                Text(
                    text = appTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.accent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "الإصدار: $appVersion • مرخص وموثق ✅",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = settingsState.aboutCustomInfo.ifBlank {
                        settingsState.bannerContent.ifEmpty { "المنصة الذكية الأولى في اليمن لربط العملاء بالمهنيين، المحلات التجارية، المراكز الطبية، والمطاعم مباشرة." }
                    },
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Features Grid Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مميزات وخدمات التطبيق 🚀", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                }

                val features = listOf(
                    "🔧 فنيون ومهنيون معتمدون في جميع المحافظات والمدن اليمنية",
                    "🛍️ دليل المتاجر، المطاعم، والكافيهات مع أرقام التواصل وساعات العمل",
                    "🏥 المراكز الطبية والعيادات التخصصية والصيدليات المناوبة",
                    "🏢 عروض العقارات والشقق والأراضي المباشرة بدون وسيط",
                    "💼 إعلانات الوظائف والفرص الوظيفية المباشرة",
                    "⚡ مساعد ذكي فوري AI لتلبية طلباتك والبحث عن أي خدمة",
                    "🔒 نظام تشفير وحماية متقدم لكافة بياناتك واتصالاتك"
                )

                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3. Quick Contact Actions (WhatsApp & Phone & Email)
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📞 اتصل وتواصل مع إدارة التطبيق والدعم الفني:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { openWhatsapp(supportWhatsapp) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("💬 واتساب الدعم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = { openDialer(supportPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📞 اتصال مباشر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                if (supportEmail.isNotBlank()) {
                    OutlinedButton(
                        onClick = { openEmail(supportEmail) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📧 مراسلة عبر البريد: $supportEmail", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        // 4. Social Media Channels Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🌐 تابعنا عبر المنصات وموقعنا الرسمي:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Telegram
                    if (!settingsState.hideTelegram && settingsState.telegramUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0088CC).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF0088CC).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.telegramUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("✈️", fontSize = 16.sp)
                                    Text("قناة تيليجرام الرسمية (Telegram)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("انضمام ↗", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Website
                    if (!settingsState.hideWebsite && settingsState.websiteUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF059669).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.websiteUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🌐", fontSize = 16.sp)
                                    Text("الموقع الإلكتروني الرسمي للمنصة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("زيارة ↗", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Facebook
                    if (!settingsState.hideFacebook && settingsState.facebookUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1877F2).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.facebookUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("📘", fontSize = 16.sp)
                                    Text("صفحة فيسبوك الرسمية (Facebook)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("متابعة ↗", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Twitter / X
                    if (!settingsState.hideTwitter && settingsState.twitterUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1DA1F2).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF1DA1F2).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.twitterUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("𝕏", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("حساب إكس / تويتر الرسمي (Twitter/X)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("متابعة ↗", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Instagram
                    if (!settingsState.hideInstagram && settingsState.instagramUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE1306C).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFE1306C).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.instagramUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("📸", fontSize = 16.sp)
                                    Text("حساب انستغرام الرسمي (Instagram)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("متابعة ↗", color = Color(0xFFF472B6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // YouTube
                    if (!settingsState.hideYoutube && settingsState.youtubeUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFF0000).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().clickable { openUrl(settingsState.youtubeUrl) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("▶️", fontSize = 16.sp)
                                    Text("قناة يوتيوب الرسمية (YouTube)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("اشتراك ↗", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Technical & Security Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("معلومات النظام والأمان 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مستوى التشفير والحماية:", fontSize = 11.5.sp, color = Color.LightGray)
                    Text("AES-256 Cloud End-to-End", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الخوادم وقواعد البيانات:", fontSize = 11.5.sp, color = Color.LightGray)
                    Text("Google Cloud & Firestore Enterprise", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("حالة المزامنة والاتصال:", fontSize = 11.5.sp, color = Color.LightGray)
                    Text("متصل ومحدث آنياً 🟢", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }

        // 6. App Download & Update Button
        Button(
            onClick = {
                val url = settingsState.appDownloadUrl.ifBlank { "https://example.com/download" }
                openUrl(url)
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📥 تحميل وتحديث التطبيق المباشر (APK)",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Text(
            text = "جميع الحقوق محفوظة © ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} - ${appTitle}",
            color = Color.Gray,
            fontSize = 10.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
    }
}
}
