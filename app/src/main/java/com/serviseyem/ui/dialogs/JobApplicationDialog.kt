package com.serviseyem.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.domain.entities.RegistrationEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.registerJobPoster
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

/**
 * 💼 JobApplicationDialog
 * Unified, reusable Composable dialog for submitting Job Applications.
 * Checks Firestore admin settings (`enableJobsRegistration`) before displaying or allowing submissions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApplicationDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    jobTitle: String = "طلب انضمام لوظيفة",
    onDismiss: () -> Unit
) {
    val adminSettings by viewModel.settings.collectAsState()
    val isJobsEnabled = adminSettings.enableJobsRegistration

    var applicantName by remember { mutableStateOf("") }
    var applicantPhone by remember { mutableStateOf("") }
    var applicantQualification by remember { mutableStateOf("") }
    var applicantExperience by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("صنعاء") }
    var notes by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "استمارة الانضمام للوظائف",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                if (!isJobsEnabled) {
                    // Disabled by Admin via Firestore
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "⚠️ التقديم مغلق حالياً",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                "تم إيقاف استقبال استمارات الوظائف مؤقتاً من قبل إدارة التطبيق. يمكنك المحاولة لاحقاً.",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق", color = Color.White)
                    }
                } else if (successMessage != null) {
                    // Success View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "✅ تم تقديم الطلب بنجاح!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                successMessage ?: "",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تم", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Form Content
                    Text(
                        text = "يرجى تعبئة بياناتك بدقة للتقديم على الفرصة الوظيفية ($jobTitle)",
                        fontSize = 11.5.sp,
                        color = Color.LightGray
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = "⚠️ $err",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("الاسم الكامل", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = applicantPhone,
                        onValueChange = { applicantPhone = it },
                        label = { Text("رقم الهاتف / الواتساب", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = applicantQualification,
                        onValueChange = { applicantQualification = it },
                        label = { Text("المؤهل العلمي / التخصص", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = applicantExperience,
                        onValueChange = { applicantExperience = it },
                        label = { Text("سنوات الخبرة / المهارات الرئيسية", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية / رابط السيرة الذاتية", fontSize = 12.sp) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (applicantName.isBlank() || applicantPhone.isBlank()) {
                                errorMessage = "يرجى كتابة الاسم ورقم الهاتف على الأقل"
                                return@Button
                            }
                            isSubmitting = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val jobObj = RegistrationEntity.Job(
                                        jobTitle = jobTitle,
                                        companyName = applicantName,
                                        category = applicantQualification,
                                        contactPhone = applicantPhone,
                                        contactEmail = "",
                                        city = selectedCity,
                                        requirements = "خبرة: $applicantExperience | ملاحظات: $notes",
                                        rawPassword = "NO_PASS"
                                    )
                                    val res = viewModel.registerJobPoster(jobObj)
                                    isSubmitting = false
                                    if (res.isSuccess) {
                                        successMessage = "شكراً لك، تم إرسال طلب الانضمام إلى مسؤول التوظيف بنجاح!"
                                    } else {
                                        errorMessage = res.exceptionOrNull()?.message ?: "حدث خطأ أثناء تقديم الطلب"
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    errorMessage = e.message ?: "فشلت العملية"
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "إرسال طلب الانضمام 📩",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
