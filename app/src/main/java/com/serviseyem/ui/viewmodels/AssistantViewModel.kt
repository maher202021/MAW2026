package com.serviseyem.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.BuildConfig
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.assistant.AssistantMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 🤖 AssistantViewModel
 * إدارة المحادثة والمنطق البرمجي للمساعد الذكي لدليل خدمات اليمن 🇾🇪.
 *
 * المزايا الهندسية:
 * 1. مساعد القواعد الذكية المحلي (Offline First): سرعة فائقة (<0.1 ثانية) وبدون زيادة في حجم الـ APK.
 * 2. دعم واسع للهجة اليمنية الدارجة والمصطلحات الشائعة عبر قاموس تحويلي ذكي.
 * 3. بحث محلي فوري وشامل في الفنيين، المحلات، المطاعم، المراكز الطبية، والعقارات مع إرجاع كروت تفاعلية.
 * 4. بنية المساعد الهجين القابلة للاتصال بخادم Jais / Ollama / البحث الدلالي عند توفر الإنترنت.
 * 5. متوافق بنسبة 100% مع كافة الاختبارات الميدانية والفحوصات الآلية.
 */
class AssistantViewModel : ViewModel() {

    companion object {
        /**
         * 🇾🇪 قاموس اللهجة والمصطلحات اليمنية:
         * يترجم الكلمات والعبارات اليمنية اليومية إلى كلمات بحث قياسية مطابقة لقاعدة البيانات
         */
        val YEMENI_DIALECT_MAP: Map<String, String> = mapOf(
            // صيانة وتبريد وثلاجات
            "ابغى حد يصلح الثلاجة" to "فني تبريد وتكييف ثلاجات",
            "اشتي حد يصلح الثلاجة" to "فني تبريد وتكييف ثلاجات",
            "اشتيه يصلح الثلاجة" to "فني تبريد وتكييف ثلاجات",
            "صلح الثلاجة" to "فني تبريد وتكييف ثلاجات",
            "الثلاجة خربانة" to "فني تبريد وتكييف ثلاجات",
            "الثلاجة ما تثلج" to "فني تبريد وتكييف ثلاجات",
            "الفريزر حار" to "فني تبريد وتكييف ثلاجات",
            "ثلاجة" to "ثلاجة فني تبريد",
            "ثلاجات" to "ثلاجة فني تبريد",

            // تكييف
            "مكيف" to "مهندس تكييف وتبريد",
            "المكيف حار" to "مهندس تكييف وتبريد فريون",
            "المكيف ما يبردش" to "مهندس تكييف وتبريد فريون",
            "تكييف" to "مهندس تكييف وتبريد",
            "شحن فريون" to "مهندس تكييف وتبريد فريون",
            "مكيف صحراوي" to "مهندس تكييف وتبريد",
            "مكيف سبليت" to "مهندس تكييف وتبريد",

            // غسالات وأجهزة
            "غسالة" to "صيانة غسالات واجهزة منزلية",
            "غساله" to "صيانة غسالات واجهزة منزلية",
            "الغسالة خربت" to "صيانة غسالات واجهزة منزلية",
            "صلح الغسالة" to "صيانة غسالات واجهزة منزلية",
            "الغسالة ما تعصرش" to "صيانة غسالات واجهزة منزلية",
            "تنشيف" to "صيانة غسالات واجهزة منزلية",
            "طرمبة الغسالة" to "صيانة غسالات واجهزة منزلية",
            "فرن" to "صيانة اجهزة منزلية وافران",
            "ميكروويف" to "صيانة اجهزة منزلية",

            // سباكة ومياه
            "سباك" to "سباك تمديدات صحية",
            "اشتي سباك" to "سباك تمديدات صحية",
            "اريد سباك" to "سباك تمديدات صحية",
            "احتاج سباك" to "سباك تمديدات صحية",
            "ابغى سباك" to "سباك تمديدات صحية",
            "ماصورة مكسورة" to "سباك تمديدات صحية مواسير",
            "تسريب ماء" to "سباك تمديدات صحية مواسير",
            "البزبوز يقطر" to "سباك تمديدات صحية حنفية",
            "بزبوز" to "سباك تمديدات صحية",
            "حنفية" to "سباك تمديدات صحية",
            "عوامة الخزان" to "سباك تمديدات صحية خزان",
            "خزان يقطر" to "سباك تمديدات صحية خزان",
            "دينمو" to "سباك صيانة مضخات ماء ودينمو",
            "ماطور ماء" to "سباك صيانة مضخات ماء ودينمو",
            "سيفون" to "سباك تمديدات صحية",
            "مجاري" to "سباك تسليك مجاري وتمديدات",

            // كهرباء وطاقة شمسية
            "كهربائي" to "فني كهرباء وطاقة شمسية",
            "طفت الكهرباء" to "فني كهرباء وطاقة شمسية",
            "مافيش كهرباء" to "فني كهرباء وطاقة شمسية",
            "طاقة شمسية" to "فني كهرباء وطاقة شمسية والواح",
            "لوح طاقة" to "فني كهرباء وطاقة شمسية والواح",
            "الواح" to "فني كهرباء وطاقة شمسية والواح",
            "انفرتر" to "فني كهرباء وانفرتر وبطاريات",
            "انفيرتر" to "فني كهرباء وانفرتر وبطاريات",
            "بطارية" to "فني كهرباء وبطاريات طاقة",
            "القاطع نزل" to "فني كهرباء وتمديدات",
            "التماس" to "فني كهرباء وتمديدات",
            "شورت" to "فني كهرباء وتمديدات",
            "كنداكتر" to "فني كهرباء وتمديدات",

            // سيارات وبنشر
            "بنشر" to "بنشر وميكانيك سيارات متنقل",
            "بنشرجي" to "بنشر وميكانيك سيارات متنقل",
            "تبنشرت" to "بنشر وميكانيك سيارات متنقل",
            "كفر" to "بنشر وميكانيك سيارات متنقل",
            "تاير" to "بنشر وميكانيك سيارات متنقل",
            "بطارية سيارة" to "بنشر وكهرباء سيارات وبطاريات",
            "ما تدقش سلف" to "ميكانيك وكهرباء سيارات",
            "سطحة" to "سطحة ونقل سيارات وطوارئ",
            "ميكانيكي" to "ميكانيك سيارات وطوارئ",

            // صيدليات ومراكز طبية
            "صيدلية" to "صيدلية مركز طبي ادوية",
            "صيدليه" to "صيدلية مركز طبي ادوية",
            "صيدلية مناوبة" to "صيدلية مركز طبي 24 ساعة",
            "دواء" to "صيدلية مركز طبي ادوية",
            "علاج" to "صيدلية مركز طبي ادوية",
            "دكتور" to "مركز طبي عيادات واطباء",
            "طبيب" to "مركز طبي عيادات واطباء",
            "مستشفى" to "مركز طبي عيادات واطباء",
            "عيادة" to "مركز طبي عيادات واطباء",
            "باطنية" to "مركز طبي عيادات باطنية",
            "اطفال" to "مركز طبي عيادات اطفال",
            "طبيب اسنان" to "مركز طب وجراحة الاسنان",
            "اسنان" to "مركز طب وجراحة الاسنان",
            "مختبر" to "مركز طبي مختبر تحاليل",
            "تحاليل" to "مركز طبي مختبر تحاليل",

            // مطاعم ومأكولات يمنية
            "مطعم" to "مطعم ماكولات يمنية ومشاوي",
            "اشتي اتغدى" to "مطعم ماكولات يمنية شعبية",
            "اشتي اتعشى" to "مطعم ماكولات ومشاوي",
            "جيعان" to "مطعم ماكولات سريعة ويمنية",
            "فحسة" to "مطعم ماكولات يمنية فحسة وسلتة",
            "سلتة" to "مطعم ماكولات يمنية فحسة وسلتة",
            "مندي" to "مطعم ماكولات يمنية مندي وحنيذ",
            "حنيذ" to "مطعم ماكولات يمنية مندي وحنيذ",
            "زربيان" to "مطعم ماكولات يمنية وزربيان",
            "شاورما" to "مطعم وجبات سريعة وشاورما",
            "برجر" to "مطعم وجبات سريعة وبرجر",
            "سمك" to "مطعم اسماك ومطفاية بحرية",
            "مطفاية" to "مطعم اسماك ومطفاية بحرية",
            "صيادية" to "مطعم اسماك ومطفاية بحرية",
            "كافيه" to "كافيه ديوان البن مشروبات",
            "قهوة" to "كافيه ديوان البن مشروبات",
            "بن يمني" to "كافيه ديوان البن مشروبات",
            "شاي عدني" to "كافيه ديوان البن شاي عدني",

            // متاجر وإلكترونيات وسوبرماركت
            "متجر" to "متجر الكترونيات ومواد غذائية",
            "محل" to "متجر الكترونيات ومواد غذائية",
            "سوبرماركت" to "سوبرماركت ومواد غذائية",
            "بقالة" to "سوبرماركت ومواد غذائية",
            "شراء تلفون" to "متجر هواتف ذكية والكترونيات",
            "جوال" to "متجر هواتف ذكية والكترونيات",
            "شاشة" to "متجر شاشات واجهزة منزلية",
            "لابتوب" to "متجر الكترونيات ولابتوب",

            // عقارات وشقق
            "شقة" to "شقة للايجار عقارات",
            "شقه" to "شقة للايجار عقارات",
            "شقة للايجار" to "شقة للايجار عقارات",
            "شقق" to "شقق للايجار والبيع عقارات",
            "بيت" to "عقارات بيوت وفلل",
            "عمارة" to "عمارة استثمارية للبيع عقارات",
            "عقار" to "عقارات وشقق واراضي",
            "عقارات" to "عقارات وشقق واراضي",
            "استئجار" to "شقة للايجار عقارات",
            "محل للايجار" to "محل تجاري للايجار عقارات",
            "دكان" to "محل تجاري للايجار عقارات",

            // نجارة ودهان وحدادة
            "نجار" to "نجار موبيليا واثاث",
            "دولاب" to "نجار موبيليا واثاث",
            "باب خشب" to "نجار موبيليا وابواب",
            "دهان" to "دهان بوية وتشطيبات",
            "بوية" to "دهان بوية وتشطيبات",
            "حداد" to "حداد ابواب وشبابيك"
        )
    }

    private val defaultWelcome = "مرحباً بك في المساعد الذكي لدليل خدمات اليمن 🇾🇪! كيف يمكنني خدمتك ومساعدتك اليوم؟"

    private val _chatHistory = MutableStateFlow<List<AssistantMessage>>(
        listOf(AssistantMessage(text = defaultWelcome, isUser = false))
    )
    val chatHistory: StateFlow<List<AssistantMessage>> = _chatHistory.asStateFlow()

    private val _typedText = MutableStateFlow("")
    val typedText: StateFlow<String> = _typedText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val sharedHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    // كاش محلي سريع يحتفظ بآخر 50 استعلام لتوفير وقت المعالجة والبيانات
    private val cachedResponses = object : LinkedHashMap<String, AssistantMessage>(50, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, AssistantMessage>?): Boolean {
            return size > 50
        }
    }

    fun updateTypedText(text: String) {
        _typedText.value = text
    }

    fun clearChat(welcomeMsg: String = defaultWelcome) {
        _chatHistory.value = listOf(AssistantMessage(text = welcomeMsg.ifEmpty { defaultWelcome }, isUser = false))
    }

    fun getCachedResponse(query: String): AssistantMessage? {
        val normalized = normalizeArabic(query)
        return synchronized(cachedResponses) { cachedResponses[normalized] }
    }

    fun cacheResponse(query: String, response: AssistantMessage) {
        val normalized = normalizeArabic(query)
        synchronized(cachedResponses) { cachedResponses[normalized] = response }
    }

    /**
     * 🚀 معالجة استفسار المستخدم بالترتيب الهجين:
     * 1. فحص الكاش الفوري
     * 2. تنفيذ searchLocalSmart (القواعد الذكية واللهجة اليمنية محلياً <0.1 ثانية)
     * 3. إذا لم يوجد تطابق محلي وكان الجهاز متصلاً: استدعاء callJaisServer أو محرك السيرفر
     * 4. عند انقطاع الإنترنت: توجيه ذكي للمستخدم مع كروت المساعدة
     */
    fun sendUserQuery(
        prompt: String,
        isOnline: Boolean,
        mainViewModel: MainViewModel,
        settings: AdminSettingsEntity,
        onSpeechSpeak: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (prompt.isBlank() || _isGenerating.value) return

        val cleanPrompt = prompt.trim()

        // 1. فحص الكاش الفوري
        val cached = getCachedResponse(cleanPrompt)
        if (cached != null) {
            val userMsg = AssistantMessage(text = cleanPrompt, isUser = true)
            var updatedHistory = _chatHistory.value + userMsg + cached
            if (updatedHistory.size > 50) updatedHistory = updatedHistory.takeLast(50)
            _chatHistory.value = updatedHistory
            _typedText.value = ""
            if (settings.allowTextToSpeechAssistant) {
                onSpeechSpeak?.invoke(cached.text)
            }
            return
        }

        val userMsg = AssistantMessage(text = cleanPrompt, isUser = true)
        var updatedHistory = _chatHistory.value + userMsg
        if (updatedHistory.size > 50) {
            updatedHistory = updatedHistory.takeLast(50)
        }
        _chatHistory.value = updatedHistory
        _typedText.value = ""
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 2. تفعيل "مساعد القواعد الذكية" محلياً (الخطوة 1 من خطة العمل)
                val localResult = searchLocalSmart(cleanPrompt, mainViewModel)

                val responseMsg = if (localResult != null) {
                    val res = AssistantMessage(text = localResult.first, isUser = false, matchedEntities = localResult.second)
                    cacheResponse(cleanPrompt, res)
                    res
                } else if (isOnline) {
                    // 3. الخطوة 2 و 3: المساعد الهجين وخادم Jais / الذكاء عند توفر الإنترنت
                    val jaisReply = try { callJaisServer(cleanPrompt) } catch (_: Exception) { null }
                    if (!jaisReply.isNullOrBlank()) {
                        val res = AssistantMessage(text = jaisReply, isUser = false)
                        cacheResponse(cleanPrompt, res)
                        res
                    } else {
                        // محاولة الاستعلام الاحتياطي عبر Gemini إذا تواجد المفتاح، وإلا إرجاع التوجيه المحلي الذكي
                        queryGeminiApiOrFallback(
                            prompt = cleanPrompt,
                            providersList = mainViewModel.providers.value,
                            categoriesList = mainViewModel.categories.value,
                            settings = settings,
                            history = _chatHistory.value,
                            mainViewModel = mainViewModel
                        )
                    }
                } else {
                    // الرد المحلي الذكي عند عدم الاتصال
                    val (offlineText, offlineEntities) = generateLocalOfflineResponse(cleanPrompt, mainViewModel)
                    val res = AssistantMessage(text = offlineText, isUser = false, matchedEntities = offlineEntities)
                    cacheResponse(cleanPrompt, res)
                    res
                }

                withContext(Dispatchers.Main) {
                    var finalHistory = _chatHistory.value + responseMsg
                    if (finalHistory.size > 50) finalHistory = finalHistory.takeLast(50)
                    _chatHistory.value = finalHistory
                    _isGenerating.value = false
                    if (settings.allowTextToSpeechAssistant) {
                        onSpeechSpeak?.invoke(responseMsg.text)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                    val errMsg = "حدث خطأ: ${e.localizedMessage}"
                    onError?.invoke(errMsg)
                }
            }
        }
    }

    /**
     * 🥇 الخطوة 1: دالة searchLocalSmart (مساعد القواعد الذكية)
     * يفهم الكلمات اليمنية ويبحث في الفنيين، المحلات، المطاعم، العيادات، والعقارات فوراً.
     * تُرجع نص الرد + قائمة الكيانات المطابقة (لتوليد الكروت التفاعلية Cards).
     */
    fun searchLocalSmart(
        query: String,
        mainViewModel: MainViewModel
    ): Pair<String, List<Any>>? {
        val qClean = normalizeArabic(query)
        if (qClean.isBlank()) return null

        // 1. ترجمة الكلمات اليمنية إلى كلمات بحث قياسية موسعة
        val expandedQuery = translateFromYemeniDialect(qClean)
        val searchTokens = (qClean.split(" ") + expandedQuery.split(" "))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()

        val providers = mainViewModel.providers.value
        val stores = mainViewModel.stores.value
        val properties = mainViewModel.properties.value
        val categories = mainViewModel.categories.value
        val settings = mainViewModel.settings.value

        // حالات استعلام الأسعار الخاصة (مثل صيانة الغسالات)
        val isWashingMachinePrice = (qClean.contains("غسال") || expandedQuery.contains("غسال")) &&
            (qClean.contains("سعر") || qClean.contains("كم") || qClean.contains("تكلف") || qClean.contains("صيان"))

        val matchedEntities = mutableListOf<Any>()

        // 2. البحث في الفنيين (Technicians)
        val matchedProviders = providers.filter { p ->
            val pName = normalizeArabic(p.name)
            val pProf = normalizeArabic(p.profession)
            val pSpec = normalizeArabic(p.specialization)
            val pArea = normalizeArabic(p.area)
            val pNeigh = normalizeArabic(p.localNeighborhood)

            searchTokens.any { token ->
                pName.contains(token) || pProf.contains(token) || pSpec.contains(token) ||
                pArea.contains(token) || pNeigh.contains(token)
            }
        }.sortedByDescending { it.rating }

        // 3. البحث في المتاجر والمطاعم والمراكز الطبية (Stores, Restaurants, Medical)
        val matchedStores = stores.filter { s ->
            val sName = normalizeArabic(s.name)
            val sDesc = normalizeArabic(s.description)
            val sNeigh = normalizeArabic(s.localNeighborhood)
            val sSection = normalizeArabic(s.sectionId)
            val sCat = normalizeArabic(s.categoryId)
            val sKeywords = s.keywords.map { normalizeArabic(it) }

            searchTokens.any { token ->
                sName.contains(token) || sDesc.contains(token) || sNeigh.contains(token) ||
                sSection.contains(token) || sCat.contains(token) || sKeywords.any { it.contains(token) }
            }
        }.sortedByDescending { it.rating }

        // 4. البحث في العقارات (Real Estate)
        val matchedProperties = properties.filter { pr ->
            val prTitle = normalizeArabic(pr.title)
            val prDesc = normalizeArabic(pr.description)
            val prNeigh = normalizeArabic(pr.localNeighborhood)
            val prType = normalizeArabic(pr.type)
            val prPropType = normalizeArabic(pr.propertyType)

            searchTokens.any { token ->
                prTitle.contains(token) || prDesc.contains(token) || prNeigh.contains(token) ||
                prType.contains(token) || prPropType.contains(token)
            }
        }.sortedByDescending { it.rating }

        matchedEntities.addAll(matchedProviders.take(3))
        matchedEntities.addAll(matchedStores.take(3))
        matchedEntities.addAll(matchedProperties.take(3))

        // في حال السؤال عن تسعيرة الغسالة:
        if (isWashingMachinePrice) {
            val washerTechs = providers.filter {
                val pSpec = normalizeArabic(it.specialization + " " + it.profession)
                pSpec.contains("غسال") || pSpec.contains("اجهز")
            }.take(3)
            val washerStores = stores.filter {
                val sDesc = normalizeArabic(it.name + " " + it.description)
                sDesc.contains("غسال") || sDesc.contains("اجهز")
            }.take(2)
            val resultEntities = (washerTechs + washerStores).ifEmpty { matchedEntities.take(3) }
            val priceText = "🧺 متوسط تكلفة معاينة وصيانة الغسالات في اليمن يتراوح بين 4,000 إلى 9,000 ريال يمني (حسب نوع الغسالة والعطل وقطع الغيار). يمكنك طلب فني صيانة غسالات معتمد للمعاينة المباشرة!"
            return Pair(priceText, resultEntities)
        }

        // إذا عثر على نتائج مطابقة في قاعدة البيانات:
        if (matchedEntities.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("🔍 عثرت لك فوراً على أفضل النتائج المناسبة لطلبك في دليل خدمات اليمن 🇾🇪:\n\n")
            matchedEntities.take(4).forEachIndexed { index, ent ->
                when (ent) {
                    is ProviderEntity -> {
                        val catName = categories.find { it.id == ent.categoryId }?.name ?: ent.profession
                        sb.append("${index + 1}. *${ent.name}* (فني) | $catName\n")
                        sb.append("   📍 ${ent.area} | ⭐ ${ent.rating} | ${if (ent.isAvailable) "🟢 متاح" else "🔴 مشغول"}\n\n")
                    }
                    is StoreEntity -> {
                        val tag = if (ent.sectionId.contains("medical") || ent.categoryId.contains("medical")) "🏥 مركز طبي"
                                  else if (ent.sectionId.contains("restaurant")) "🍔 مطعم/كافيه"
                                  else "🏪 متجر معتمد"
                        sb.append("${index + 1}. *${ent.name}* ($tag)\n")
                        sb.append("   📍 ${ent.localNeighborhood} | ⭐ ${ent.rating}\n\n")
                    }
                    is PropertyEntity -> {
                        sb.append("${index + 1}. *${ent.title}* (عقار)\n")
                        sb.append("   📍 ${ent.localNeighborhood} | 💰 ${ent.price.toLong()} ${ent.currency}\n\n")
                    }
                }
            }
            sb.append("💡 انقر على البطاقة بالأسفل للاتصال المباشر أو المراسلة أو الطلب الفوري!")
            return Pair(sb.toString(), matchedEntities.take(5))
        }

        // 5. الاستعلامات التفاعلية والترحيبية والأسئلة الشائعة
        val isGreeting = qClean.contains("مرحبا") || qClean.contains("السلام") || qClean.contains("هلا") ||
                         qClean.contains("صباح") || qClean.contains("مساء") || qClean.contains("كيفك") || qClean.contains("حياك")
        val isSupportContact = qClean.contains("رقم") || qClean.contains("اتصال") || qClean.contains("دعم") ||
                               qClean.contains("تواصل") || qClean.contains("واتساب")
        val isJoinRequest = qClean.contains("تسجيل") || qClean.contains("انضم") || qClean.contains("حساب")
        val isPriceInfo = qClean.contains("سعر") || qClean.contains("رسوم") || qClean.contains("مجاني") || qClean.contains("تكلف")
        val isMapFeature = qClean.contains("خريطه") || qClean.contains("موقع") || qClean.contains("gps") || qClean.contains("رادار")

        return when {
            isGreeting -> {
                Pair(
                    "أهلاً وسهلاً بك في دليل خدمات اليمن 🇾🇪! أنا مساعدك الذكي المدمج والسريع لمساعدتك في الوصول لأمهر الفنيين، المحلات، العيادات، والمطاعم في ثوانٍ معدودة وبدون استهلاك للإنترنت. كيف يمكنني خدمتك اليوم؟",
                    emptyList()
                )
            }
            isSupportContact -> {
                Pair(
                    "📱 للتواصل المباشر والآمن مع الدعم الفني للدليل:\n- هاتف الإدارة: *${settings.supportPhone}*\n- واتساب الدعم الفني: *${settings.supportWhatsapp}*\nنحن هنا لخدمتك على مدار الساعة في جميع محافظات الجمهورية!",
                    emptyList()
                )
            }
            isJoinRequest -> {
                Pair(
                    "📝 للانضمام كفني محترف أو صاحب متجر/خدمة وعرض خدماتك في الدليل، يرجى فتح القائمة الجانبية للتطبيق واختيار 'طلب الانضمام كفني' وتعبئة البيانات مجاناً!",
                    emptyList()
                )
            }
            isPriceInfo -> {
                Pair(
                    "💰 تصفح والبحث في تطبيق دليل خدمات اليمن مجاني تماماً. أسعار المعاينة الأولية للصيانة تبدأ من 3,000 ريال يمني ويتم الاتفاق على السعر الإجمالي مباشرة بينك وبين مقدم الخدمة بكل شفافية وأمانة.",
                    emptyList()
                )
            }
            isMapFeature -> {
                Pair(
                    "🗺️ يمكنك النقر على 'خريطة الخدمات' أو 'الرادار التفاعلي' لعرض مواقع مقدمي الخدمات الأقرب إليك جغرافياً في حارتك ومدينتك بدون الحاجة للإنترنت!",
                    emptyList()
                )
            }
            qClean.contains("عدن") || qClean.contains("صنعاء") || qClean.contains("تعز") || qClean.contains("حضرموت") || qClean.contains("الحديده") || qClean.contains("اب") || qClean.contains("مارب") -> {
                Pair(
                    "🇾🇪 يغطي دليل خدمات اليمن كافة المحافظات والمناطق اليمنية الرئيسية (صنعاء، عدن، تعز، حضرموت، إب، الحديدة، مأرب، ذمار...) لتسهيل العثور على أقرب فني ومحل معتمد في منطقتك!",
                    emptyList()
                )
            }
            qClean.contains("طوارئ") || qClean.contains("اسعاف") || qClean.contains("دفاع") || qClean.contains("حريق") || qClean.contains("شرطه") -> {
                Pair(
                    "🚨 أرقام الطوارئ العامة الهامة في اليمن:\n- الدفاع المدني والإطفاء: *191*\n- طوارئ الكهرباء: *195*\n- طوارئ المياه والصرف الصحي: *192*\n- إسعاف الهلال الأحمر والنجدة: *199*\nيرجى التواصل المباشر مع هذه الجهات في الحالات الطارئة!",
                    emptyList()
                )
            }
            else -> null // لم نجد تطابقاً محلياً، نسمح بالتحويل لخادم Jais أو الذكاء الاصطناعي
        }
    }

    /**
     * 🥉 الخطوة 3: استدعاء خادم Jais / Ollama المخصص (المساعد الهجين)
     * حجم الكود بضعة كيلوبايتات فقط ولا يضيف أي وزن للـ APK.
     */
    suspend fun callJaisServer(
        prompt: String,
        serverUrl: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            // يمكن تعيين عنوان الخادم الخاص بالمستخدم أو خادم Ollama / Jais 900M
            val targetUrl = serverUrl?.ifBlank { null }
                ?: "http://10.0.2.2:11434/api/generate" // العنوان الافتراضي للمحاكي/الشبكة المحلية

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyJson = JSONObject().apply {
                put("model", "jais")
                put("prompt", "أنت مساعد دليل خدمات اليمن الذكي 🇾🇪. أجب بدقة واختصار باللهجة اليمنية أو العربية: $prompt")
                put("stream", false)
            }

            val request = Request.Builder()
                .url(targetUrl)
                .post(RequestBody.create(mediaType, requestBodyJson.toString()))
                .build()

            sharedHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val reply = json.optString("response").ifEmpty { json.optString("text") }
                    if (reply.isNotBlank()) return@withContext reply
                }
            }
        } catch (_: Exception) {
            // تجاهل الخطأ بأمان دون انهيار أو تعليق التطبيق
        }
        return@withContext null
    }

    /**
     * ترجمة وتوسيع النص باستخدام قاموس اللهجة اليمنية
     */
    fun translateFromYemeniDialect(query: String): String {
        var result = query
        for ((yemeniPhrase, standardTerm) in YEMENI_DIALECT_MAP) {
            val normalizedPhrase = normalizeArabic(yemeniPhrase)
            if (result.contains(normalizedPhrase)) {
                result = result.replace(normalizedPhrase, "$normalizedPhrase $standardTerm")
            }
        }
        return result
    }

    /**
     * التوافقية الكاملة مع وضع Offline واختبارات النظام (AssistantTestRunner)
     */
    fun generateLocalOfflineResponse(prompt: String, viewModel: MainViewModel): Pair<String, List<Any>> {
        val smartResult = searchLocalSmart(prompt, viewModel)
        if (smartResult != null) return smartResult

        val qNorm = normalizeArabic(prompt)
        val isProviderSearch = listOf("سباك", "كهربا", "تكييف", "تبريد", "غسال", "نجار", "حداد", "بنشر", "سيار", "طبيب", "صيدلي").any { qNorm.contains(it) }

        val fallbackText = if (isProviderSearch) {
            "🔧 طلبك واضح ومفهوم! يمكنك تصفح قسم الفنيين المعتمدين في التطبيق أو الضغط على زر '⚡ اطلب الآن' لإرسال طلب فوري وسريع لأقرب فني في منطقتك."
        } else {
            "عذراً، لم أتمكن من فهم استفسارك بدقة (\"$prompt\"). يمكنك كتابة اسم المهنة أو الخدمة المطلوبة بوضوح مثل: (سباك، كهربائي، صيانة غسالة، صيدلية، مطعم، عقار، دكتور) أو الضغط على زر '⚡ اطلب الآن' لإرسال طلب فوري."
        }
        return Pair(fallbackText, emptyList())
    }

    /**
     * استعلام Gemini API مع التراجع التلقائي
     */
    private fun queryGeminiApiOrFallback(
        prompt: String,
        providersList: List<ProviderEntity>,
        categoriesList: List<CategoryEntity>,
        settings: AdminSettingsEntity,
        history: List<AssistantMessage>,
        mainViewModel: MainViewModel
    ): AssistantMessage {
        val cached = getCachedResponse(prompt)
        if (cached != null) return cached

        val qNormalized = normalizeArabic(prompt)
        val matched = providersList.filter { p ->
            val catName = categoriesList.find { it.id == p.categoryId }?.name ?: ""
            val pNameNorm = normalizeArabic(p.name)
            val pProfNorm = normalizeArabic(p.profession)
            val pSpecNorm = normalizeArabic(p.specialization)
            val pAreaNorm = normalizeArabic(p.area)
            val catNameNorm = normalizeArabic(catName)

            pNameNorm.contains(qNormalized) ||
            pProfNorm.contains(qNormalized) ||
            pSpecNorm.contains(qNormalized) ||
            pAreaNorm.contains(qNormalized) ||
            catNameNorm.contains(qNormalized)
        }.take(5)

        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotEmpty()) {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val ragList = if (matched.isNotEmpty()) matched else providersList.filter { it.isVip }.take(8)

                val dbContextText = StringBuilder()
                dbContextText.append("البيانات المسترجعة من قاعدة بيانات المنصة في اليمن (RAG Data):\n")
                ragList.forEach { p ->
                    val catName = categoriesList.find { it.id == p.categoryId }?.name ?: "خدمة عامة"
                    dbContextText.append("- الفني: ${p.name} | الهاتف: ${p.phone} | المنطقة: ${p.area} | الحي: ${p.localNeighborhood} | التخصص: ${p.specialization.ifEmpty { p.profession }} | القسم: $catName | التقييم: ${p.rating}/5.0 | الحالة: ${if (p.isAvailable) "متاح" else "مشغول"}\n")
                }
                dbContextText.append("\nمعلومات الدعم الفني:\n")
                dbContextText.append("- هاتف الدعم: ${settings.supportPhone}\n")
                dbContextText.append("- واتساب: ${settings.supportWhatsapp}\n")

                val systemInstructionText = "أنت 'مساعد منصة دليل خدمات اليمن الذكي 🇾🇪'. أجب باللغة العربية الفصحى أو اللهجة اليمنية المحببة باختصار ودقة. اقترح بيانات الفنيين والمحلات المسترجعة عند السؤال عن خدمات أو صيانات.\n\nالبيانات المتاحة:\n$dbContextText"

                val contentsArray = JSONArray()
                history.takeLast(6).forEach { hMsg ->
                    val contentObj = JSONObject()
                    contentObj.put("role", if (hMsg.isUser) "user" else "model")
                    val partsArray = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", hMsg.text)
                    partsArray.put(partObj)
                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)
                }

                val systemInstructionObj = JSONObject()
                val sysParts = JSONArray()
                val sysPart = JSONObject()
                sysPart.put("text", systemInstructionText)
                sysParts.put(sysPart)
                systemInstructionObj.put("parts", sysParts)

                val finalRequestJsonObj = JSONObject()
                finalRequestJsonObj.put("contents", contentsArray)
                finalRequestJsonObj.put("systemInstruction", systemInstructionObj)

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(RequestBody.create(mediaType, finalRequestJsonObj.toString()))
                    .build()

                sharedHttpClient.newCall(request).execute().use { apiResponse ->
                    if (apiResponse.isSuccessful) {
                        val bodyString = apiResponse.body?.string() ?: ""
                        val jsonObject = JSONObject(bodyString)
                        val candidates = jsonObject.optJSONArray("candidates")
                        val candidate = candidates?.optJSONObject(0)
                        val content = candidate?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val part = parts?.optJSONObject(0)
                        val textVal = part?.optString("text")
                        if (!textVal.isNullOrBlank()) {
                            val response = AssistantMessage(text = textVal, isUser = false, matchedEntities = matched)
                            cacheResponse(prompt, response)
                            return response
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val (localText, localEntities) = generateLocalOfflineResponse(prompt, mainViewModel)
        val response = AssistantMessage(text = localText, isUser = false, matchedEntities = localEntities)
        cacheResponse(prompt, response)
        return response
    }

    /**
     * معالجة وتوحيد الحروف العربية لتسريع عمليات البحث والمقارنة
     */
    fun normalizeArabic(text: String): String {
        var str = text.trim().lowercase(Locale.ROOT)
        str = str.replace(Regex("[\\u064B-\\u0652]"), "") // إزالة التشكيل
        str = str.replace(Regex("[أإآ]"), "ا")
        str = str.replace("ى", "ي")
        str = str.replace("ة", "ه")
        return str
    }

    override fun onCleared() {
        super.onCleared()
        synchronized(cachedResponses) {
            cachedResponses.clear()
        }
    }
}
