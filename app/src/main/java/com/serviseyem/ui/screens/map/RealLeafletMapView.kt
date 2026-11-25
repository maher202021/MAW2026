package com.serviseyem.ui.screens.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.components.OfflineInteractiveMap
import com.serviseyem.ui.screens.map.utils.MapJsonBuilder
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.resolveThemePalette

/**
 * 🗺️ RealLeafletMapView (100% Free OpenStreetMap + Offline Tile Cache Engine)
 * - تحميل خريطة OpenStreetMap الرسمية المجانية بدون أي مفتاح API
 * - دعم كامل للمس المتعدد والتكبير والتصغير السلس (Pinch / Multi-Touch Zoom)
 * - ربط العلامات الملونة حسب نوع الكيان مع Bottom Sheet تفاعلي
 * - مؤشر نابض أزرق لموقع المستخدم الفعلي
 * - تخزين محلي للبلاطات لتصفح المناطق بدون إنترنت
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealLeafletMapView(
    userCoords: Pair<Double, Double>,
    nearbyProviders: List<ProviderEntity>,
    nearbyStores: List<StoreEntity>,
    nearbyProperties: List<PropertyEntity>,
    dynamicOffsets: Map<String, Pair<Double, Double>>,
    selectedCity: String = "الكل",
    zoomScale: Float = 1.0f,
    onZoomScaleChange: (Float) -> Unit = {},
    panOffset: Offset = Offset.Zero,
    onPanOffsetChange: (Offset) -> Unit = {},
    selectedEntity: Any? = null,
    mapMode: MapMode = MapMode.VELA_3D,
    currentMapLayer: String = "esri_world_imagery",
    maxZoom: Int = 22,
    showSatMenu: Boolean = true,
    showMeasureTool: Boolean = true,
    tiltAngle: Float = 30f,
    onProviderSelected: (ProviderEntity) -> Unit,
    onStoreSelected: (StoreEntity) -> Unit,
    onPropertySelected: (PropertyEntity) -> Unit,
    onDeselect: () -> Unit = {},
    onSwitchToRadar: (() -> Unit)? = null,
    onMapLoadFailed: (() -> Unit)? = null,
    onWebViewCreated: (WebView) -> Unit = {},
    onTilesFailedOffline: ((String) -> Unit)? = null,
    isEarthquakeLayerActive: Boolean = false,
    earthquakeUpdateTrigger: Int = 0,
    onEarthquakeDataLoaded: ((Long, Int) -> Unit)? = null,
    onEarthquakeDataError: ((String) -> Unit)? = null,
    isNasaGibsActive: Boolean = false,
    gibsProduct: String = "viirs_noaa20",
    onGibsInfo: ((String, String) -> Unit)? = null,
    isEoxCloudlessActive: Boolean = false,
    isEsriImageryActive: Boolean = false,
    eumetEnabled: Boolean = false,
    eumetLayer: String = "",
    onOverlayStatus: ((String, Boolean) -> Unit)? = null,
    isDataSaverActive: Boolean = false,
    pickMode: Boolean = false,
    onMapPicked: ((Double, Double) -> Unit)? = null,
    onMapLongPress: ((Double, Double) -> Unit)? = null,
    onMapTap: (() -> Unit)? = null,
    waypointMarkers: List<Triple<String, Double, Double>> = emptyList(),
    themeColors: VisualThemePalette = resolveThemePalette(AdminSettingsEntity()),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var isMapReady by remember { mutableStateOf(false) }
    var hasTilesLoaded by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val latestProviders by rememberUpdatedState(nearbyProviders)
    val latestStores by rememberUpdatedState(nearbyStores)
    val latestProperties by rememberUpdatedState(nearbyProperties)
    val latestOnProviderSelected by rememberUpdatedState(onProviderSelected)
    val latestOnStoreSelected by rememberUpdatedState(onStoreSelected)
    val latestOnPropertySelected by rememberUpdatedState(onPropertySelected)
    val latestOnDeselect by rememberUpdatedState(onDeselect)
    val latestOnTilesFailedOffline by rememberUpdatedState(onTilesFailedOffline)
    val latestOnEarthquakeDataLoaded by rememberUpdatedState(onEarthquakeDataLoaded)
    val latestOnEarthquakeDataError by rememberUpdatedState(onEarthquakeDataError)
    val latestOnMapPicked by rememberUpdatedState(onMapPicked)
    val latestOnMapLongPress by rememberUpdatedState(onMapLongPress)
    val latestOnMapTap by rememberUpdatedState(onMapTap)
    val latestOnGibsInfo by rememberUpdatedState(onGibsInfo)
    val latestOnOverlayStatus by rememberUpdatedState(onOverlayStatus)
    val latestOnMapLoadFailed by rememberUpdatedState(onMapLoadFailed)
    
    // التحقق المسبق من وجود ملف map.html لتجنب الشاشات السوداء تماماً
    val isAssetAvailable = remember {
        try {
            context.assets.open("map.html").close()
            true
        } catch (e: Exception) {
            false
        }
    }
    var hasLoadError by remember { mutableStateOf(!isAssetAvailable) }

    // [نظام خرائط وملاحة عالمي - ملكنا] حارس مهلة 7 ثوانٍ للتحول التلقائي للخريطة المحلية عند تعذر تحميل WebView
    LaunchedEffect(isAssetAvailable, hasLoadError, isMapReady) {
        if (isAssetAvailable && !hasLoadError && !isMapReady) {
            kotlinx.coroutines.delay(7_000L)
            if (!isMapReady) {
                hasLoadError = true
                latestOnMapLoadFailed?.invoke()
            }
        }
    }

    // تنظيف الموارد لمنع تسريب الذاكرة أو تجمد العرض
    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    removeAllViews()
                    destroy()
                }
            } catch (e: Exception) {}
            webViewRef = null
        }
    }

    // تحويل البيانات إلى JSON مهيأ للبلاطات والعلامات
    val offsetsSnapshot = dynamicOffsets.toMap()
    val markersJson = remember(nearbyProviders, nearbyStores, nearbyProperties, selectedCity, offsetsSnapshot, userCoords) {
        MapJsonBuilder.buildMarkersJson(
            nearbyProviders = nearbyProviders,
            nearbyStores = nearbyStores,
            nearbyProperties = nearbyProperties,
            dynamicOffsets = offsetsSnapshot,
            safeUserLat = userCoords.first,
            safeUserLng = userCoords.second
        )
    }

    // إرسال العلامات للخريطة عند اكتمال الجاهزية أو تغير البيانات
    LaunchedEffect(isMapReady, markersJson) {
        if (isMapReady && webViewRef != null) {
            val quoted = org.json.JSONObject.quote(markersJson)
            val script = "if (window.setMarkers) { window.setMarkers($quoted); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تحديث موقع المستخدم الفعلي على الخريطة
    LaunchedEffect(isMapReady, userCoords) {
        if (isMapReady && webViewRef != null && userCoords.first != 0.0 && userCoords.second != 0.0) {
            val script = "if (window.updateUserLocation) { window.updateUserLocation(${userCoords.first}, ${userCoords.second}, 30, false); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تمركز الخريطة عند تغيير المدينة
    LaunchedEffect(isMapReady, selectedCity) {
        if (isMapReady && webViewRef != null) {
            val cityCoords = OfflineMapManager.getCityCoordinates(selectedCity)
            val quotedCity = org.json.JSONObject.quote(cityCoords.nameAr)
            val script = "if (window.setCity) { window.setCity($quotedCity, ${cityCoords.latitude}, ${cityCoords.longitude}, ${cityCoords.defaultZoom}); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تبديل أوضاع خريطة Vela (3D أو الصور الجوية OAM) والطبقة المختارة
    LaunchedEffect(isMapReady, mapMode, currentMapLayer) {
        if (isMapReady && webViewRef != null) {
            val quotedMode = org.json.JSONObject.quote(mapMode.name)
            val quotedLayer = org.json.JSONObject.quote(currentMapLayer)
            val script = """
                if (window.setVelaMode) { window.setVelaMode($quotedMode); }
                if (window.switchLayer) { window.switchLayer($quotedLayer); }
            """.trimIndent()
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // مزامنة إعدادات الأدمن للخريطة (الحد الأقصى للتكبير، إظهار القائمة الفضائية والمسطرة)
    LaunchedEffect(isMapReady, maxZoom, showSatMenu, showMeasureTool) {
        if (isMapReady && webViewRef != null) {
            val cfgJson = org.json.JSONObject()
                .put("maxZoom", maxZoom)
                .put("showSatMenu", showSatMenu)
                .put("showMeasureTool", showMeasureTool)
                .toString()
            val quotedCfg = org.json.JSONObject.quote(cfgJson)
            webViewRef?.evaluateJavascript(
                "if (window.applyAdminMapConfig) { window.applyAdminMapConfig($quotedCfg); }",
                null
            )
        }
    }

    // إمالة الكاميرا التفاعلية للوضع ثلاثي الأبعاد 3D Tilt
    LaunchedEffect(isMapReady, tiltAngle, mapMode) {
        if (isMapReady && webViewRef != null) {
            val effectiveTilt = if (mapMode == MapMode.VELA_3D) tiltAngle else 0f
            val script = "if (window.setTilt) { window.setTilt($effectiveTilt); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // 🌋 تفعيل/إيقاف وتحديث طبقة الزلازل الحية
    LaunchedEffect(isMapReady, isEarthquakeLayerActive, earthquakeUpdateTrigger) {
        if (isMapReady && webViewRef != null) {
            if (isEarthquakeLayerActive) {
                webViewRef?.evaluateJavascript("if (window.loadEarthquakes) { window.loadEarthquakes(); }", null)
            } else {
                webViewRef?.evaluateJavascript("if (window.clearEarthquakes) { window.clearEarthquakes(); }", null)
            }
        }
    }

    // 🛰️ مزامنة وتفعيل/إيقاف طبقة ناسا GIBS
    LaunchedEffect(isMapReady, isNasaGibsActive, gibsProduct) {
        if (!isMapReady || webViewRef == null) return@LaunchedEffect
        val allowed = setOf("viirs_noaa20", "viirs_snpp", "truecolor", "landtemp")
        if (isNasaGibsActive && gibsProduct in allowed) {
            val quoted = org.json.JSONObject.quote(gibsProduct)
            webViewRef?.evaluateJavascript(
                "if (window.setGibs) { window.setGibs($quoted); } if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer(true); }",
                null
            )
        } else {
            webViewRef?.evaluateJavascript("if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer(false); }", null)
        }
    }

    // 🛰️ مزامنة طبقة EOX Cloudless (10m) و Esri World Imagery
    LaunchedEffect(isMapReady, isEoxCloudlessActive) {
        if (isMapReady && webViewRef != null) {
            webViewRef?.evaluateJavascript("if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer($isEoxCloudlessActive); }", null)
        }
    }
    LaunchedEffect(isMapReady, isEsriImageryActive) {
        if (isMapReady && webViewRef != null) {
            webViewRef?.evaluateJavascript("if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer($isEsriImageryActive); }", null)
        }
    }

    // 🛰️ تفعيل/تحديث طبقة قمر Meteosat المباشر من EUMETSAT (IODC 41.5°E)
    LaunchedEffect(isMapReady, eumetEnabled, eumetLayer) {
        if (!isMapReady) return@LaunchedEffect
        val safe = if (com.serviseyem.utils.EumetViewCatalog.isSafe(eumetLayer)) eumetLayer else ""
        val quotedSafe = org.json.JSONObject.quote(safe)
        webViewRef?.evaluateJavascript("if (window.setEumet) { window.setEumet(${eumetEnabled && safe.isNotEmpty()}, $quotedSafe); }", null)
    }

    // [نظام خرائط وملاحة عالمي - ملكنا] مزامنة وضع توفير البيانات
    LaunchedEffect(isMapReady, isDataSaverActive) {
        if (isMapReady && webViewRef != null) {
            webViewRef?.evaluateJavascript("if (window.setDataSaver) { window.setDataSaver($isDataSaverActive); }", null)
        }
    }

    // [تعديل] التحكم في التكبير والتصغير التفاعلي وربطه مباشرة مع خريطة OpenStreetMap
    var lastZoomScale by remember { mutableFloatStateOf(zoomScale) }
    LaunchedEffect(zoomScale) {
        if (isMapReady && webViewRef != null) {
            if (zoomScale > lastZoomScale) {
                webViewRef?.evaluateJavascript("if (window.zoomIn) { window.zoomIn(); }", null)
            } else if (zoomScale < lastZoomScale) {
                webViewRef?.evaluateJavascript("if (window.zoomOut) { window.zoomOut(); }", null)
            }
            lastZoomScale = zoomScale
        }
    }

    // [تعديل] إعادة التمركز إلى موقع المستخدم عند استدعاء زر موقعي (panOffset = Offset.Zero)
    LaunchedEffect(panOffset) {
        if (isMapReady && webViewRef != null && panOffset == Offset.Zero) {
            webViewRef?.evaluateJavascript("if (window.recenterUser) { window.recenterUser(); }", null)
        }
    }

    LaunchedEffect(isMapReady, pickMode) {
        if (isMapReady && webViewRef != null) {
            webViewRef?.evaluateJavascript("if (window.setPickMode) { window.setPickMode($pickMode); }", null)
        }
    }

    LaunchedEffect(isMapReady, waypointMarkers) {
        if (isMapReady && webViewRef != null) {
            val arr = org.json.JSONArray()
            waypointMarkers.forEach { (name, lat, lng) ->
                arr.put(
                    org.json.JSONObject()
                        .put("name", name)
                        .put("lat", lat)
                        .put("lng", lng)
                )
            }
            webViewRef?.evaluateJavascript("if (window.setWaypointMarkers) { window.setWaypointMarkers($arr); }", null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.serviseyem.ui.theme.AppBackground)
    ) {
        if (!hasLoadError) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.parseColor("#14202E"))
                        if (com.serviseyem.utils.OfflineTileInterceptor.migrateCacheIfNeeded(ctx)) {
                            clearCache(true)
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = "AlYemenServicesApp/1.0 (Android; OpenStreetMap)"
                            setSupportZoom(true)
                            builtInZoomControls = false
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            allowFileAccess = true
                            allowContentAccess = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        // جسر التواصل بين JavaScript في Leaflet وكود Kotlin الأصلي
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMapReady() {
                                post { isMapReady = true }
                            }

                            @JavascriptInterface
                            fun onMapClicked() {
                                post {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    latestOnDeselect()
                                }
                            }

                            @JavascriptInterface
                            fun onMapTap() {
                                post {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    if (latestOnMapTap != null) {
                                        latestOnMapTap?.invoke()
                                    } else {
                                        latestOnDeselect()
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onMapLongPress(lat: Double, lng: Double) {
                                post {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    latestOnMapLongPress?.invoke(lat, lng)
                                }
                            }

                            @JavascriptInterface
                            fun onTilesLoaded() {
                                post { hasTilesLoaded = true }
                            }

                            @JavascriptInterface
                            fun onMapPicked(lat: Double, lng: Double) {
                                post {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    latestOnMapPicked?.invoke(lat, lng)
                                }
                            }

                            @JavascriptInterface
                            fun onMarkerClicked(id: String, type: String) {
                                post {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    when (type.uppercase()) {
                                        "PROVIDER" -> {
                                            latestProviders.find { it.id == id }?.let { latestOnProviderSelected(it) }
                                        }
                                        "STORE", "RESTAURANT", "MEDICAL" -> {
                                            latestStores.find { it.id == id }?.let { latestOnStoreSelected(it) }
                                        }
                                        "PROPERTY" -> {
                                            latestProperties.find { it.id == id }?.let { latestOnPropertySelected(it) }
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun switchLayer(layerKey: String) {
                                post {
                                    val quoted = org.json.JSONObject.quote(layerKey)
                                    evaluateJavascript("if (window.switchLayer) { window.switchLayer($quoted); }", null)
                                }
                            }

                            @JavascriptInterface
                            fun onEarthquakeDataLoaded(timestamp: Long, count: Int) {
                                post { latestOnEarthquakeDataLoaded?.invoke(timestamp, count) }
                            }

                            @JavascriptInterface
                            fun onEarthquakeDataError(error: String) {
                                post { latestOnEarthquakeDataError?.invoke(error) }
                            }

                            @JavascriptInterface
                            fun onTilesFailedOffline(layerKey: String) {
                                post { latestOnTilesFailedOffline?.invoke(layerKey) }
                            }

                            @JavascriptInterface
                            fun onGibsInfo(product: String, date: String) {
                                post { latestOnGibsInfo?.invoke(product, date) }
                            }

                            @JavascriptInterface
                            fun onOverlayStatus(name: String, ok: Boolean) {
                                post { latestOnOverlayStatus?.invoke(name, ok) }
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                return com.serviseyem.utils.OfflineTileInterceptor.intercept(ctx, request)
                                    ?: super.shouldInterceptRequest(view, request)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                return true // منع فتح أي روابط خارجية أو مواقع إنترنت داخل WebView الخريطة
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isMapReady = true
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                // [تعديل] لا ننتقل للبديل إلا إذا كان الملف الأساسي المحلي نفسه فشل لضمان عدم ظهور شاشة سوداء
                                if (failingUrl?.contains("map.html") == true) {
                                    hasLoadError = true
                                    latestOnMapLoadFailed?.invoke()
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasLoadError = true
                                    latestOnMapLoadFailed?.invoke()
                                }
                            }
                        }

                        loadUrl("file:///android_asset/map.html")
                        webViewRef = this
                        onWebViewCreated(this)
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // الخريطة التفاعلية المحلية البديلة في حالة الطوارئ (100% بدون شاشة سوداء)
            OfflineInteractiveMap(
                userCoords = userCoords,
                nearbyProviders = nearbyProviders,
                nearbyStores = nearbyStores,
                nearbyProperties = nearbyProperties,
                dynamicOffsets = dynamicOffsets,
                selectedCity = selectedCity,
                zoomScale = zoomScale,
                onZoomScaleChange = onZoomScaleChange,
                panOffset = panOffset,
                onPanOffsetChange = onPanOffsetChange,
                selectedEntity = selectedEntity,
                onProviderSelected = onProviderSelected,
                onStoreSelected = onStoreSelected,
                onPropertySelected = onPropertySelected,
                onDeselect = onDeselect,
                onSwitchToRadar = onSwitchToRadar,
                pickMode = pickMode,
                onMapPicked = onMapPicked,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

object MapAssetMemoryCache {
    fun isCached(): Boolean = true
    fun invalidate() {}
    fun getOrLoadSync(context: Context): String = ""
    suspend fun getOrLoadAsync(context: Context): String = ""
}
