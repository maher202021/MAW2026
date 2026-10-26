package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.BespokePremiumSystem

/**
 * 🚀 ComingSoonSectionView - شاشة عرض راقية للخدمات قيد الإطلاق (نقل وتوصيل، خدمات منزلية)
 */
@Composable
fun ComingSoonSectionView(
    title: String,
    subtitle: String,
    iconEmoji: String,
    gradient: Brush,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BespokePremiumSystem.DeepDark)
            .padding(bottom = 30.dp)
    ) {
        // زر الرجوع الفوري إلى الشاشة الرئيسية
        BespokePremiumSystem.SectionBackHeader(
            title = title,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // البطاقة المركزية الفخمة لحالة قريباً
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            shape = RoundedCornerShape(24.dp),
            color = BespokePremiumSystem.SurfaceDark,
            border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // أيقونة الخدمة في دائرة مضيئة بتدرج أنيق
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // عنوان التوفير قريباً
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BespokePremiumSystem.EmeraldAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, BespokePremiumSystem.EmeraldAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BespokePremiumSystem.EmeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "قيد الإطلاق والتجهيز",
                            color = BespokePremiumSystem.EmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "سيتم توفير هذه الخدمات قريباً",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // مميزات الخدمة القادمة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeaturePill(icon = "🛡️", text = "خدمة موثوقة")
                    FeaturePill(icon = "⚡", text = "سرعة واستجابة")
                    FeaturePill(icon = "📍", text = "تغطية يمنية شاملة")
                }

                Spacer(modifier = Modifier.height(28.dp))

                // زر الرجوع للرئيسية
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BespokePremiumSystem.EmeraldAccent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = BespokePremiumSystem.DeepDark
                        )
                        Text(
                            text = "العودة إلى الشاشة الرئيسية",
                            color = BespokePremiumSystem.DeepDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // زر التسجيل كمزود للخدمة
                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "كن أول من يقدم هذه الخدمة وسجل معنا ✍️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(0.5.dp, BespokePremiumSystem.GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
