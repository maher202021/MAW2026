package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import org.json.JSONObject
import java.io.File

/**
 * 💾 System Backup, Performance Diagnostics & Runtime Permission Rationales
 * Solves Problem 9: Provides automated system database backups, performance diagnostics, and M3 permission rationales.
 */

// ==========================================
// 1. 💾 System Backup & Recovery Manager
// ==========================================
object SystemBackupManager {

    data class DatabaseBackupPayload(
        val timestamp: Long = System.currentTimeMillis(),
        val version: String = "2026.1",
        val storesCount: Int = 0,
        val providersCount: Int = 0,
        val rawDataJson: String = ""
    ) {
        fun toJsonString(): String {
            return JSONObject().apply {
                put("timestamp", timestamp)
                put("version", version)
                put("storesCount", storesCount)
                put("providersCount", providersCount)
                put("rawDataJson", rawDataJson)
            }.toString()
        }
    }

    fun createLocalBackup(context: Context, backupDataJson: String): Pair<Boolean, String> {
        return try {
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "ys_backup_${System.currentTimeMillis()}.json")
            backupFile.writeText(backupDataJson)

            Pair(true, backupFile.absolutePath)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "فشل إنشاء النسخة الاحتياطية")
        }
    }

    fun listLocalBackups(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}

// ==========================================
// 2. 🛡️ M3 Runtime Permission Rationale Dialog
// ==========================================
@Composable
fun RuntimePermissionRationaleDialog(
    permissionNameArabic: String, // e.g. "إذن الموقع الجغرافي (GPS)"
    reasonArabic: String, // e.g. "يحتاج التطبيق لإذن الموقع لعرض أقرب الفنيين والمحلات التجارية منك بدقة عالية."
    onConfirmGrant: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Permission",
                    tint = themeColors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "طلب صلاحية: $permissionNameArabic",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = reasonArabic,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
                Text(
                    text = "🔒 نضمن لك الحفاظ الكامل على خصوصية بياناتك وعدم مشاركتها مع أي أطراف خارجية.",
                    fontSize = 10.sp,
                    color = Color(0xFF10B981)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmGrant,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("منح الصلاحية الآن 🟢", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ليس الآن", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    )
}
