package com.serviseyem.ui

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppStrings(
    // Top & Bottom Navigation
    val home: String = "الرئيسية",
    val maps: String = "الخرائط",
    val join: String = "الانضمام",
    val alerts: String = "الإشعارات",
    val chats: String = "المحادثات",
    val aboutApp: String = "عن التطبيق",
    val bookings: String = "الحجوزات",
    val adminPanel: String = "الإدارة",
    val languageName: String = "العربية",
    val settings: String = "الإعدادات",

    // Admin Portal
    val adminTitle: String = "بوابة مسؤولي المنصة الموثقة",
    val adminSubtitle: String = "الرجاء إدخال اسم المستخدم وكلمة المرور للدخول لوحة الإشراف والتحكم",
    val username: String = "اسم المستخدم",
    val password: String = "كلمة المرور",
    val rememberMe: String = "تذكرني وحفظ تسجيل الدخول",
    val adminLoginBtn: String = "تسجيل دخول المشرف",
    val loginSuccess: String = "تم تسجيل الدخول بنجاح",
    val invalidCredentials: String = "اسم المستخدم أو كلمة المرور غير صحيحة",

    // Common Buttons & Labels
    val searchPlaceholder: String = "بحث عن خدمة، مركز تجاري، أو عقار...",
    val categories: String = "الأقسام الرئيسية",
    val providers: String = "مقدمو الخدمات",
    val stores: String = "المراكز التجارية",
    val realEstate: String = "العقارات",
    val restaurants: String = "المطاعم",
    val medical: String = "المراكز الطبية",
    val jobs: String = "الوظائف",
    val city: String = "المدينة",
    val filter: String = "تصفية",
    val all: String = "الكل",
    val available: String = "متاح 🟢",
    val busy: String = "مشغول 🟡",
    val distance: String = "المسافة",
    val km: String = "كم",
    val send: String = "إرسال",
    val cancel: String = "إلغاء",
    val save: String = "حفظ",
    val delete: String = "حذف",
    val edit: String = "تعديل",
    val add: String = "إضافة",
    val refresh: String = "تحديث",
    val confirm: String = "تأكيد",
    val back: String = "رجوع",
    val retry: String = "إعادة المحاولة",
    val statusSent: String = "تم الإرسال ✓",
    val statusDelivered: String = "تم الوصول ✓✓",
    val statusRead: String = "تمت القراءة ✓✓",
    val typeMessage: String = "اكتب رسالتك هنا...",
    val selectCity: String = "اختر المحافظة / المدينة",
    val noData: String = "لا توجد بيانات حالياً",
    val loading: String = "جاري التحميل...",
    val errorOccurred: String = "حدث خطأ ما، يرجى المحاولة لاحقاً",
    val changeLanguage: String = "تغيير اللغة",
    val arabic: String = "العربية",
    val english: String = "English"
)

val ArStrings = AppStrings(
    home = "الرئيسية",
    maps = "الخرائط",
    join = "الانضمام",
    alerts = "الإشعارات",
    chats = "المحادثات",
    aboutApp = "عن التطبيق",
    bookings = "الحجوزات",
    adminPanel = "الإدارة",
    languageName = "العربية",
    settings = "الإعدادات",

    adminTitle = "بوابة مسؤولي المنصة الموثقة",
    adminSubtitle = "الرجاء إدخال اسم المستخدم وكلمة المرور للدخول لوحة الإشراف والتحكم",
    username = "اسم المستخدم",
    password = "كلمة المرور",
    rememberMe = "تذكرني وحفظ تسجيل الدخول",
    adminLoginBtn = "تسجيل دخول المشرف",
    loginSuccess = "تم تسجيل الدخول بنجاح",
    invalidCredentials = "اسم المستخدم أو كلمة المرور غير صحيحة",

    searchPlaceholder = "بحث عن خدمة، مركز تجاري، أو عقار...",
    categories = "الأقسام الرئيسية",
    providers = "مقدمو الخدمات",
    stores = "المراكز التجارية",
    realEstate = "العقارات",
    restaurants = "المطاعم",
    medical = "المراكز الطبية",
    jobs = "الوظائف",
    city = "المدينة",
    filter = "تصفية",
    all = "الكل",
    available = "متاح 🟢",
    busy = "مشغول 🟡",
    distance = "المسافة",
    km = "كم",
    send = "إرسال",
    cancel = "إلغاء",
    save = "حفظ",
    delete = "حذف",
    edit = "تعديل",
    add = "إضافة",
    refresh = "تحديث",
    confirm = "تأكيد",
    back = "رجوع",
    retry = "إعادة المحاولة",
    statusSent = "تم الإرسال ✓",
    statusDelivered = "تم الوصول ✓✓",
    statusRead = "تمت القراءة ✓✓",
    typeMessage = "اكتب رسالتك هنا...",
    selectCity = "اختر المحافظة / المدينة",
    noData = "لا توجد بيانات حالياً",
    loading = "جاري التحميل...",
    errorOccurred = "حدث خطأ ما، يرجى المحاولة لاحقاً",
    changeLanguage = "تغيير اللغة",
    arabic = "العربية",
    english = "English"
)

val EnStrings = AppStrings(
    home = "Home",
    maps = "Maps",
    join = "Join",
    alerts = "Alerts",
    chats = "Chats",
    aboutApp = "About",
    bookings = "Bookings",
    adminPanel = "Admin",
    languageName = "English",
    settings = "Settings",

    adminTitle = "Authenticated Admin Portal",
    adminSubtitle = "Please enter username and password to access dashboard controls",
    username = "Username",
    password = "Password",
    rememberMe = "Remember me & keep logged in",
    adminLoginBtn = "Admin Login",
    loginSuccess = "Logged in successfully",
    invalidCredentials = "Invalid username or password",

    searchPlaceholder = "Search for service, mall, or real estate...",
    categories = "Main Categories",
    providers = "Service Providers",
    stores = "Malls & Stores",
    realEstate = "Real Estate",
    restaurants = "Restaurants",
    medical = "Medical Centers",
    jobs = "Jobs",
    city = "City",
    filter = "Filter",
    all = "All",
    available = "Available 🟢",
    busy = "Busy 🟡",
    distance = "Distance",
    km = "km",
    send = "Send",
    cancel = "Cancel",
    save = "Save",
    delete = "Delete",
    edit = "Edit",
    add = "Add",
    refresh = "Refresh",
    confirm = "Confirm",
    back = "Back",
    retry = "Retry",
    statusSent = "Sent ✓",
    statusDelivered = "Delivered ✓✓",
    statusRead = "Read ✓✓",
    typeMessage = "Type a message...",
    selectCity = "Select City",
    noData = "No data available",
    loading = "Loading...",
    errorOccurred = "An error occurred, please try again later",
    changeLanguage = "Change Language",
    arabic = "Arabic",
    english = "English"
)

val LocalAppStrings = staticCompositionLocalOf { ArStrings }

val appStrings: AppStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalAppStrings.current

/**
 * LocaleManager: Centralized Localization System
 * Synchronizes selected language locally (SharedPreferences) and remotely (Firestore).
 * Dynamically fetches JSON/Map translations from Firestore to allow Admin override without APK update.
 */
object LocaleManager {
    private const val PREFS_NAME = "yemen_service_prefs"
    private const val KEY_LANG = "app_language"

    private val _currentLang = MutableStateFlow("ar")
    val currentLang: StateFlow<String> = _currentLang.asStateFlow()

    private val _dynamicTranslations = MutableStateFlow<Map<String, String>>(emptyMap())
    val dynamicTranslations: StateFlow<Map<String, String>> = _dynamicTranslations.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = sp.getString(KEY_LANG, "ar") ?: "ar"
        _currentLang.value = savedLang

        listenToFirestoreTranslations()
    }

    fun setLanguage(context: Context, langCode: String) {
        val cleanCode = if (langCode == "en") "en" else "ar"
        _currentLang.value = cleanCode

        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LANG, cleanCode).apply()

        // Sync with Firestore for remote backup / settings
        try {
            val db = FirebaseFirestore.getInstance()
            val userId = sp.getString("user_id", "guest") ?: "guest"
            if (userId.isNotEmpty() && userId != "guest") {
                val decId = try { com.serviseyem.utils.SecurityCryptoUtils.decrypt(userId) } catch (e: Exception) { userId }
                db.collection("registered_users").document(decId).update("preferredLanguage", cleanCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleLanguage(context: Context): String {
        val newLang = if (_currentLang.value == "ar") "en" else "ar"
        setLanguage(context, newLang)
        return newLang
    }

    fun getString(key: String, fallback: String): String {
        val customMap = _dynamicTranslations.value
        val lang = _currentLang.value
        val fullKey = "${lang}_$key"
        return customMap[fullKey] ?: customMap[key] ?: fallback
    }

    private fun listenToFirestoreTranslations() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("settings").document("app_translations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        error.printStackTrace()
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        if (data != null) {
                            val map = mutableMapOf<String, String>()
                            data.forEach { (k, v) ->
                                if (v is String) map[k] = v
                            }
                            _dynamicTranslations.value = map
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateFirestoreTranslations(translationsMap: Map<String, String>, onComplete: (Boolean) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("settings").document("app_translations")
                .set(translationsMap, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } catch (e: Exception) {
            onComplete(false)
        }
    }
}

/**
 * LocalizationProvider composable wrapper ensuring:
 * 1. Correct LocalLayoutDirection (RTL for "ar", LTR for "en")
 * 2. Instant Recomposition across all screens on language change
 */
@Composable
fun LocalizationProvider(
    langCode: String,
    content: @Composable () -> Unit
) {
    val dynamicMap: Map<String, String> by LocaleManager.dynamicTranslations.collectAsState()
    val layoutDirection = if (langCode == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
    val baseStrings = if (langCode == "en") EnStrings else ArStrings

    val effectiveStrings = remember(baseStrings, dynamicMap, langCode) {
        if (dynamicMap.isEmpty()) {
            baseStrings
        } else {
            fun getDyn(key: String, fallback: String): String {
                return dynamicMap["${langCode}_$key"] ?: dynamicMap[key] ?: fallback
            }
            // NOTE: Manual mapping chosen over reflection for Compose performance.
            // Reflection in remember{} blocks causes unnecessary recompositions.
            // If adding new strings, add them to AppStrings + both ArStrings/EnStrings + this copy block.
            baseStrings.copy(
                home = getDyn("home", baseStrings.home),
                maps = getDyn("maps", baseStrings.maps),
                join = getDyn("join", baseStrings.join),
                alerts = getDyn("alerts", baseStrings.alerts),
                chats = getDyn("chats", baseStrings.chats),
                aboutApp = getDyn("aboutApp", baseStrings.aboutApp),
                bookings = getDyn("bookings", baseStrings.bookings),
                adminPanel = getDyn("adminPanel", baseStrings.adminPanel),
                languageName = getDyn("languageName", baseStrings.languageName),
                settings = getDyn("settings", baseStrings.settings),
                adminTitle = getDyn("adminTitle", baseStrings.adminTitle),
                adminSubtitle = getDyn("adminSubtitle", baseStrings.adminSubtitle),
                username = getDyn("username", baseStrings.username),
                password = getDyn("password", baseStrings.password),
                rememberMe = getDyn("rememberMe", baseStrings.rememberMe),
                adminLoginBtn = getDyn("adminLoginBtn", baseStrings.adminLoginBtn),
                loginSuccess = getDyn("loginSuccess", baseStrings.loginSuccess),
                invalidCredentials = getDyn("invalidCredentials", baseStrings.invalidCredentials),
                searchPlaceholder = getDyn("searchPlaceholder", baseStrings.searchPlaceholder),
                categories = getDyn("categories", baseStrings.categories),
                providers = getDyn("providers", baseStrings.providers),
                stores = getDyn("stores", baseStrings.stores),
                realEstate = getDyn("realEstate", baseStrings.realEstate),
                restaurants = getDyn("restaurants", baseStrings.restaurants),
                medical = getDyn("medical", baseStrings.medical),
                jobs = getDyn("jobs", baseStrings.jobs),
                city = getDyn("city", baseStrings.city),
                filter = getDyn("filter", baseStrings.filter),
                all = getDyn("all", baseStrings.all),
                available = getDyn("available", baseStrings.available),
                busy = getDyn("busy", baseStrings.busy),
                distance = getDyn("distance", baseStrings.distance),
                km = getDyn("km", baseStrings.km),
                send = getDyn("send", baseStrings.send),
                cancel = getDyn("cancel", baseStrings.cancel),
                save = getDyn("save", baseStrings.save),
                delete = getDyn("delete", baseStrings.delete),
                edit = getDyn("edit", baseStrings.edit),
                add = getDyn("add", baseStrings.add),
                refresh = getDyn("refresh", baseStrings.refresh),
                confirm = getDyn("confirm", baseStrings.confirm),
                back = getDyn("back", baseStrings.back),
                retry = getDyn("retry", baseStrings.retry),
                statusSent = getDyn("statusSent", baseStrings.statusSent),
                statusDelivered = getDyn("statusDelivered", baseStrings.statusDelivered),
                statusRead = getDyn("statusRead", baseStrings.statusRead),
                typeMessage = getDyn("typeMessage", baseStrings.typeMessage),
                selectCity = getDyn("selectCity", baseStrings.selectCity),
                noData = getDyn("noData", baseStrings.noData),
                loading = getDyn("loading", baseStrings.loading),
                errorOccurred = getDyn("errorOccurred", baseStrings.errorOccurred),
                changeLanguage = getDyn("changeLanguage", baseStrings.changeLanguage),
                arabic = getDyn("arabic", baseStrings.arabic),
                english = getDyn("english", baseStrings.english)
            )
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides effectiveStrings
    ) {
        content()
    }
}

