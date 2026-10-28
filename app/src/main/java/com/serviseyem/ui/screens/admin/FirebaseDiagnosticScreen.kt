package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.AppScreens
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseDiagnosticScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var firestoreStatus by remember { mutableStateOf("جاري الفحص...") }
    var firestoreError by remember { mutableStateOf<String?>(null) }
    var firestoreSuccess by remember { mutableStateOf(false) }

    var authStatus by remember { mutableStateOf("جاري الفحص...") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authSuccess by remember { mutableStateOf(false) }

    var isChecking by remember { mutableStateOf(false) }

    fun runDiagnostics() {
        isChecking = true
        firestoreStatus = "جاري الاتصال بـ Firestore..."
        firestoreError = null
        firestoreSuccess = false

        authStatus = "جاري فحص مصادقة Firebase Auth..."
        authError = null
        authSuccess = false

        scope.launch(Dispatchers.IO) {
            // 1. Firestore Test
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("system_diagnostic").document("ping").get().await()
                withContext(Dispatchers.Main) {
                    firestoreStatus = "متصل بنجاح (Firestore Connected)"
                    firestoreSuccess = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    firestoreStatus = "فشل الاتصال بـ Firestore"
                    firestoreError = e.message ?: "خطأ غير معروف"
                    firestoreSuccess = false
                }
            }

            // 2. Auth Test
            try {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                withContext(Dispatchers.Main) {
                    authStatus = if (user != null) {
                        "مسجل دخول حالياً: ${user.email ?: user.uid}"
                    } else {
                        "خدمة Auth متاحة (لم يتم تسجيل دخول مستخدم حالي)"
                    }
                    authSuccess = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    authStatus = "فشل فحص Firebase Auth"
                    authError = e.message ?: "خطأ غير معروف"
                    authSuccess = false
                }
            }

            withContext(Dispatchers.Main) {
                isChecking = false
            }
        }
    }

    LaunchedEffect(Unit) {
        runDiagnostics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تشخيص و فحص خدمات Firebase", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { runDiagnostics() }, enabled = !isChecking) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة الفحص", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.primary)
            )
        },
        containerColor = themeColors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "فحص حالة الاتصال بقواعد البيانات وخدمات السحابة في الوقت الفعلي",
                    fontSize = 14.sp,
                    color = themeColors.textSecondary
                )
            }

            // Firestore Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (firestoreSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (firestoreSuccess) Color.Green else Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "قاعدة بيانات Firestore", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = firestoreStatus, fontSize = 14.sp, color = themeColors.textPrimary)
                        if (firestoreError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "خطأ: $firestoreError", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
            }

            // Auth Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (authSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (authSuccess) Color.Green else Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "مصادقة المستخدمين (Firebase Auth)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = authStatus, fontSize = 14.sp, color = themeColors.textPrimary)
                        if (authError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "خطأ: $authError", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { runDiagnostics() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري الفحص...", color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة تشخيص الفحص الكامل", color = Color.White)
                    }
                }
            }
        }
    }
}
