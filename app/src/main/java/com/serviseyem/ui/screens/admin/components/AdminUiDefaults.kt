package com.serviseyem.ui.screens.admin.components

import android.content.Context
import android.content.Intent
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.serviseyem.utils.VisualThemePalette
import java.io.File

@Composable
fun adminSwitchColors(themeColors: VisualThemePalette): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = Color(0xFF06281E),
    checkedTrackColor = themeColors.accent,
    checkedBorderColor = Color.White.copy(alpha = 0.6f),
    uncheckedThumbColor = Color(0xFFE2E8F0),
    uncheckedTrackColor = Color(0xFF334155),
    uncheckedBorderColor = Color(0xFF64748B)
)

fun shareGeneratedFile(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String
): Boolean {
    return try {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        true
    } catch (e: Exception) {
        android.util.Log.e("AdminFileShare", "Failed sharing file: ${e.message}", e)
        false
    }
}

fun writeTextFileToExportsAndShare(
    context: Context,
    fileName: String,
    content: String,
    mimeType: String,
    chooserTitle: String
): File? {
    return try {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText("\uFEFF$content", Charsets.UTF_8) // UTF-8 BOM for Arabic Excel compatibility
        shareGeneratedFile(context, file, mimeType, chooserTitle)
        file
    } catch (e: Exception) {
        android.util.Log.e("AdminFileExport", "Failed exporting $fileName: ${e.message}", e)
        null
    }
}
