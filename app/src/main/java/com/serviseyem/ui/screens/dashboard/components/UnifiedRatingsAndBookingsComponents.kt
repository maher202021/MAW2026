package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * ⭐ Unified Ratings Section Component
 */
@Composable
fun UnifiedRatingsSection(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("⭐ التقييمات وآراء العملاء", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("متوسط التقييم العام: ", fontSize = 12.sp, color = Color.White)
                    Text("⭐ ${account.rating} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${account.numReviews} تقييم)", fontSize = 11.sp, color = Color.LightGray)
                }
                Text("يتيح هذا التبويب استعراض تعليقات وتقييمات العملاء وإمكانية الرد المباشر عليها.", fontSize = 10.sp, color = themeColors.textSecondary)
            }
        }
    }
}

/**
 * 📅 Unified Bookings Section Component
 */

/**
 * 📎 Unified Attachments Section Component
 */
@Composable
fun UnifiedAttachmentsSection(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📎 إدارة المرفقات والصور والملفات (PDF / Excel)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("رفع صور واجهة الغلاف والمنيو أو ملفات PDF:", fontSize = 11.sp, color = Color.White)
                Button(
                    onClick = {
                        android.widget.Toast.makeText(context, "📎 جاري فتح منتقي الصور والملفات بالهاتف...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("رفع صور جديدة / ملف PDF 📤", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
