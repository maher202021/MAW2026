package com.serviseyem.ui.screens.dashboard

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔓 Modern Modal Bottom Sheet for Account Restoration & Recovery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreAccountBottomSheet(
    onDismissRequest: () -> Unit,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var restorePhone by remember { mutableStateOf("") }
    var restorePass by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF0F172A),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "🔓 استرجاع حساب سابق موحد",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "حسابك مشفر سحابياً برقم هاتفك. يمكنك استرجاع حسابك والدردشات القديمة بواسطة رقم هاتفك المكون من 9 أرقام وكلمة المرور السرية.",
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 16.sp
            )

            OutlinedTextField(
                value = restorePhone,
                onValueChange = { restorePhone = it },
                label = { Text("رقم هاتفك اليمني (مثال: 77xxxxxxx)", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.accent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = themeColors.accent
                ),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = restorePass,
                onValueChange = { restorePass = it },
                label = { Text("كلمة المرور السرية", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.accent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = themeColors.accent
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val cleanPhone = restorePhone.trim().replace(" ", "")
                        val cleanPass = restorePass.trim()
                        if (cleanPhone.length == 9 && cleanPass.isNotEmpty()) {
                            isRestoring = true
                            viewModel.restoreGuestUser(
                                context = context,
                                phone = cleanPhone,
                                password = cleanPass,
                                onResult = { success, msg ->
                                    isRestoring = false
                                    if (success) {
                                        onDismissRequest()
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "❌ يرجى تعبئة كافة الحقول بشكل صحيح ورقم من 9 أرقام!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isRestoring) "جاري الاسترجاع... ⏳" else "تأكيد واسترجاع ✓",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء", color = Color.White, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            TextButton(
                onClick = {
                    val cleanPhone = restorePhone.trim().replace(" ", "")
                    if (cleanPhone.length == 9) {
                        viewModel.requestAdminPasswordReset(cleanPhone)
                        Toast.makeText(context, "📩 تم إرسال طلب إعادة تعيين كلمة المرور لإدارة التطبيق لرقمك ($cleanPhone)", Toast.LENGTH_LONG).show()
                        onDismissRequest()
                    } else {
                        Toast.makeText(context, "⚠️ يرجى إدخال رقم هاتفك في الحقل أولاً لطلب إعادة التعيين من الإدارة", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "❓ نسيت كلمة المرور؟ اضغط لطلب إعادة تعيينها من الإدارة",
                    color = themeColors.accent,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
