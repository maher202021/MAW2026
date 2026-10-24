package com.serviseyem.ui.screens.register.components

import android.app.Activity
import com.serviseyem.ui.*
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import java.util.Locale

/**
 * 📝 RegistrationField - حقل إدخال موحد متطور لشاشات التسجيل
 * يدعم: زر مسح الحقل، الإكمال التلقائي، والإدخال الصوتي مع معالجة الأخطاء
 */
@Composable
fun RegistrationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    enableVoiceInput: Boolean = true,
    suggestions: List<String> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Auto-complete filtered suggestions
    val filteredSuggestions by remember(value, suggestions) {
        derivedStateOf {
            if (value.length >= 1 && suggestions.isNotEmpty()) {
                suggestions.filter { it.contains(value.trim(), ignoreCase = true) && it != value }
            } else {
                emptyList()
            }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onValueChange(if (value.isBlank()) spokenText else "$value $spokenText")
            }
        } else {
            Toast.makeText(context, "لم يتم التقاط أي صوت، يرجى المحاولة ثانية", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 11.5.sp) },
            placeholder = { Text(placeholder, fontSize = 11.sp, color = Color.Gray) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = themeColors.accent) }
            },
            trailingIcon = {
                Row {
                    // 1. Clear Button
                    if (value.isNotEmpty() && !isPassword) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح النص",
                                tint = Color.Gray
                            )
                        }
                    }

                    // 2. Password visibility toggle
                    if (isPassword) {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "عرض كلمة المرور",
                                tint = if (passwordVisible) themeColors.accent else Color.LightGray
                            )
                        }
                    }

                    // 3. Voice Input
                    if (enableVoiceInput && !isPassword) {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("ar", "YE"))
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث لإدخال $label...")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "خاصية التعرف الصوتي غير متوفرة على جهازك", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "إدخال صوتي",
                                tint = themeColors.accent
                            )
                        }
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = !errorMessage.isNullOrBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = themeColors.accent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = themeColors.accent,
                unfocusedLabelColor = Color.Gray,
                errorBorderColor = Color(0xFFEF5350),
                errorLabelColor = Color(0xFFEF5350)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Suggestions chips for Auto-Complete
        AnimatedVisibility(visible = filteredSuggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSuggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onValueChange(suggestion) },
                        label = { Text(suggestion, fontSize = 10.sp, color = themeColors.accent) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1E293B))
                    )
                }
            }
        }

        // Error message view
        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
            errorMessage?.let { err ->
                Text(
                    text = "⚠️ $err",
                    color = Color(0xFFEF5350),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                )
            }
        }
    }
}
