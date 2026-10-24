package com.serviseyem

import android.content.Context
import android.content.Intent
import com.serviseyem.ui.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.AppNavigator
import com.serviseyem.ui.components.*
import com.serviseyem.ui.dialogs.*
import com.serviseyem.ui.screens.home.*
import com.serviseyem.ui.screens.map.*
import com.serviseyem.ui.screens.bookings.*
import com.serviseyem.ui.screens.admin.*
import com.serviseyem.ui.screens.assistant.*
import com.serviseyem.ui.screens.register.*
import com.serviseyem.ui.screens.status.*
import com.serviseyem.ui.screens.about.*
import com.serviseyem.utils.*
import com.serviseyem.ui.viewmodels.*

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var locationCallback: com.google.android.gms.location.LocationCallback? = null
    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null
    private var fallbackSpeechRecognizer: android.speech.SpeechRecognizer? = null
    private val pendingNavigationIntent = kotlinx.coroutines.flow.MutableStateFlow<Intent?>(null)
    private var lastBackPressTime = 0L
    
    private var voiceResultCallback: ((String) -> Unit)? = null
    private val voiceRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                voiceResultCallback?.invoke(matches[0])
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigationIntent.value = intent
    }

    fun startVoiceInput(onResult: (String) -> Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
                android.app.AlertDialog.Builder(this)
                    .setTitle("🎙️ إذن الميكروفون")
                    .setMessage("نحتاج إلى إذن الميكروفون لتمكين ميزة البحث الصوتي والأوامر الصوتية الذكية مع المساعد الذكي لمساعدتك بسرعة وسهولة.")
                    .setPositiveButton("منح الإذن 👍") { _, _ ->
                        androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
                    }
                    .setNegativeButton("إلغاء ❌") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, "تم إلغاء منح إذن الصوت. يمكنك البحث بالكتابة بدلاً من ذلك.", Toast.LENGTH_SHORT).show()
                    }
                    .create()
                    .show()
            } else {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
                Toast.makeText(this, "⚠️ فضلاً وافق على إذن استخدام المايك في النافذة المنبثقة لخدمتك بالكامل", Toast.LENGTH_LONG).show()
            }
            return
        }

        runOnUiThread {
            voiceResultCallback = onResult
            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-YE")
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "تحدث الآن باللغة العربية للبحث أو التحدث...")
            }
            try {
                voiceRecognitionLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    try {
                        fallbackSpeechRecognizer?.destroy()
                    } catch (_: Exception) {}
                    val recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
                    fallbackSpeechRecognizer = recognizer
                    recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                        override fun onReadyForSpeech(p0: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(p0: Float) {}
                        override fun onBufferReceived(p0: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onError(error: Int) {
                            runOnUiThread {
                                try {
                                    recognizer.destroy()
                                    if (fallbackSpeechRecognizer === recognizer) fallbackSpeechRecognizer = null
                                } catch (_: Exception) {}
                                Toast.makeText(this@MainActivity, "لم يتم التعرف على الصوت، يرجى المحاولة مجدداً بصوت مسموع", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onResults(results: Bundle?) {
                            val matchesList = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                            runOnUiThread {
                                try {
                                    recognizer.destroy()
                                    if (fallbackSpeechRecognizer === recognizer) fallbackSpeechRecognizer = null
                                } catch (_: Exception) {}
                                if (!matchesList.isNullOrEmpty()) {
                                    onResult(matchesList[0])
                                }
                            }
                        }
                        override fun onPartialResults(p0: Bundle?) {}
                        override fun onEvent(p0: Int, p1: Bundle?) {}
                    })
                    recognizer.startListening(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Toast.makeText(this@MainActivity, "عذراً، نظام هاتفك لا يدعم التعرف الصوتي المباشر.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startFusedLocationUpdates(activity: ComponentActivity, viewModel: MainViewModel) {
        try {
            val context = activity.applicationContext
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateUserLocation(location.latitude, location.longitude)
                    }
                }
                
                locationCallback?.let { existingCallback ->
                    try {
                        fusedLocationClient.removeLocationUpdates(existingCallback)
                    } catch (_: Exception) {}
                }

                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000L // 🚨 M-14: 60-second interval to prevent battery drain
                ).setMinUpdateIntervalMillis(30000L).build()
                
                locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        for (loc in result.locations) {
                            viewModel.updateUserLocation(loc.latitude, loc.longitude)
                        }
                    }
                }
                
                locationCallback?.let { callback ->
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        callback,
                        android.os.Looper.getMainLooper()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            // إلغاء تسجيل تحديثات الموقع
            locationCallback?.let { callback ->
                try {
                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(applicationContext)
                    fusedLocationClient.removeLocationUpdates(callback)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            locationCallback = null

            // إلغاء تسجيل مستمع الشبكة لمنع تسريب الذاكرة
            networkCallback?.let { cb ->
                try {
                    val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                    cm?.unregisterNetworkCallback(cb)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            networkCallback = null

            // تحرير محرك التعرف الصوتي الاحتياطي إن وجد
            try {
                fallbackSpeechRecognizer?.destroy()
                fallbackSpeechRecognizer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // تحرير TextToSpeech الموحد
            try {
                VoiceManager.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNavigationIntent.value = intent

        try {
            VoiceManager.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val context = LocalContext.current
            val settingsState by viewModel.settings.collectAsState()

            LaunchedEffect(Unit) {
                try {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                    if (cm != null) {
                        networkCallback?.let { oldCb ->
                            try { cm.unregisterNetworkCallback(oldCb) } catch (_: Exception) {}
                        }

                        val request = android.net.NetworkRequest.Builder()
                            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build()
                        val newCb = object : android.net.ConnectivityManager.NetworkCallback() {
                            override fun onAvailable(network: android.net.Network) {
                                viewModel.updateOnlineStatus(true)
                            }
                            override fun onLost(network: android.net.Network) {
                                viewModel.updateOnlineStatus(false)
                            }
                        }
                        networkCallback = newCb
                        cm.registerNetworkCallback(request, newCb)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val locationPermissions = arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val granted = results.values.any { it }
                if (granted) {
                    viewModel.triggerNotification("📌 تم تفعيل تحديد الموقع التلقائي بدقة عالية!")
                    startFusedLocationUpdates(this@MainActivity, viewModel)
                } else {
                    viewModel.triggerNotification("⚠️ تم رفض إذن الموقع، يمكنك تحديد مدينتك يدوياً")
                }
            }

            LaunchedEffect(Unit) {
                try {
                    val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        startFusedLocationUpdates(this@MainActivity, viewModel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val colors = remember(settingsState) {
                resolveThemePalette(settingsState)
            }

            val isInitialized by viewModel.isInitialized.collectAsState()
            val currentLang by viewModel.currentLanguage.collectAsState()
            val navIntent by pendingNavigationIntent.collectAsState()

            LaunchedEffect(isInitialized, navIntent) {
                val activeIntent = navIntent
                if (isInitialized && activeIntent != null) {
                    try {
                        val openChatId = activeIntent.getStringExtra("openChatChannelId")
                        val openUrgentCode = activeIntent.getStringExtra("openUrgentRequestCode")
                        val navigateTo = activeIntent.getStringExtra("navigate_to")
                        val targetScreen = activeIntent.getStringExtra("target_screen")

                        when {
                            !openChatId.isNullOrBlank() -> {
                                viewModel.targetChatChannelId = openChatId
                                viewModel.navigateToScreen(AppScreens.CHAT_DIRECT)
                            }
                            !openUrgentCode.isNullOrBlank() -> {
                                viewModel.selectedRequestId = openUrgentCode
                                viewModel.navigateToScreen(AppScreens.URGENT_REQUEST_DETAILS)
                            }
                            navigateTo == "PASSWORD_RECOVERY_PANEL" || navigateTo == "ADMIN_PANEL" || targetScreen == "ADMIN_PANEL" -> {
                                viewModel.navigateToScreen(AppScreens.ADMIN_PANEL)
                            }
                            !targetScreen.isNullOrBlank() && targetScreen != "MAIN" && targetScreen != "HOME" -> {
                                viewModel.navigateToScreen(targetScreen)
                            }
                            !navigateTo.isNullOrBlank() && navigateTo != "MAIN" && navigateTo != "HOME" -> {
                                viewModel.navigateToScreen(navigateTo)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingNavigationIntent.value = null
                    }
                }
            }

            BackHandler {
                val handled = viewModel.goBack()
                if (!handled) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressTime < 2000) {
                        finish()
                    } else {
                        lastBackPressTime = now
                        Toast.makeText(context, "Press again to exit / اضغط مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            MaterialTheme(
                colorScheme = colors.scheme
            ) {
                val appStrings = if (currentLang == "en") com.serviseyem.ui.EnStrings else com.serviseyem.ui.ArStrings
                CompositionLocalProvider(
                    LocalLayoutDirection provides (if (currentLang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl),
                    com.serviseyem.ui.LocalAppStrings provides appStrings
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (isInitialized) colors.background else Color.Black
                    ) {
                        if (!isInitialized) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp)
                                ) {
                                    // الدائرة والشعار WAM
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1D58B8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "WAM",
                                            color = Color(0xFF00DC82),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 36.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(36.dp))

                                    // العنوان الرئيسي
                                    Text(
                                        text = "كل خدمات اليمن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // العنوان الفرعي
                                    Text(
                                        text = "دليلك الشامل لكل الخدمات والمهن",
                                        fontSize = 16.sp,
                                        color = Color(0xFFD1D5DB),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(38.dp))

                                    // مؤشر التحميل الأخضر/الفيروزي
                                    CircularProgressIndicator(
                                        color = Color(0xFF00DC82),
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )

                                    Spacer(modifier = Modifier.height(38.dp))

                                    // النص الترحيبي التعريفي
                                    val splashMessage = if (settingsState.splashWelcomeMessage.isNotBlank()) {
                                        settingsState.splashWelcomeMessage
                                    } else {
                                        "التطبيق الأول في اليمن والوطن العربي الذي يربط مقدمي الخدمات وأصحاب المهن بالمستخدمين فورياً"
                                    }
                                    Text(
                                        text = splashMessage,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        } else {
                            AppNavigator(viewModel = viewModel, themeColors = colors, permissionLauncher = permissionLauncher, locationPermissions = locationPermissions)
                        }
                    }
                }
            }
        }
    }

    /**
     * 🔐 تهيئة Vault المالك والأدمن بشكل آمن عند التشغيل
     */
    private fun initializeAdminVaultIfNeeded() {
        if (com.serviseyem.utils.SecureAdminStorage.isVaultInitialized(this)) return
        
        lifecycleScope.launch {
            try {
                // ملاحظة: التهيئة الكاملة تتم عند أول تسجيل دخول ناجح للمالك أو الأدمن
                // أو يمكن مزامنتها عبر Cloud Function في بيئة الإنتاج
                android.util.Log.d("MainActivity", "Admin Vault not initialized yet. It will be synced on first login.")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Vault init error: ${e.message}")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // لا تفعل شيئاً - منع إعادة الإنشاء
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "🎙️ تم تفعيل الميكروفون بنجاح!", Toast.LENGTH_SHORT).show()
                    voiceResultCallback?.let { callback ->
                        startVoiceInput(callback)
                    }
                } else {
                    Toast.makeText(this, "❌ تم رفض إذن الميكروفون. يمكنك البحث بالكتابة.", Toast.LENGTH_SHORT).show()
                }
            }
            102 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "🔔 تم تفعيل الإشعارات بنجاح!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "⚠️ تم رفض الإشعارات. لن تتلقى تنبيهات بالرسائل الجديدة.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
