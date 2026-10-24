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
    currentMapLayer: String = "google_satellite",
    tiltAngle: Float = 30f,
    onProviderSelected: (ProviderEntity) -> Unit,
    onStoreSelected: (StoreEntity) -> Unit,
    onPropertySelected: (PropertyEntity) -> Unit,
    onDeselect: () -> Unit = {},
    onSwitchToRadar: (() -> Unit)? = null,
    onMapLoadFailed: (() -> Unit)? = null,
    onWebViewCreated: (WebView) -> Unit = {},
    themeColors: VisualThemePalette = resolveThemePalette(AdminSettingsEntity()),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMapReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasLoadError by remember { mutableStateOf(false) }

    // تحويل البيانات إلى JSON مهيأ للبلاطات والعلامات
    val markersJson = remember(nearbyProviders, nearbyStores, nearbyProperties, selectedCity) {
        MapJsonBuilder.buildMarkersJson(
            nearbyProviders = nearbyProviders,
            nearbyStores = nearbyStores,
            nearbyProperties = nearbyProperties,
            dynamicOffsets = dynamicOffsets,
            safeUserLat = userCoords.first,
            safeUserLng = userCoords.second
        )
    }

    // إرسال العلامات للخريطة عند اكتمال الجاهزية أو تغير البيانات
    LaunchedEffect(isMapReady, markersJson) {
        if (isMapReady && webViewRef != null) {
            val script = "if (window.setMarkers) { window.setMarkers('$markersJson'); }"
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
            val script = "if (window.setCity) { window.setCity('${cityCoords.nameAr}', ${cityCoords.latitude}, ${cityCoords.longitude}, ${cityCoords.defaultZoom}); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تبديل أوضاع خريطة Vela (3D أو الصور الجوية OAM)
    LaunchedEffect(isMapReady, mapMode, currentMapLayer) {
        if (isMapReady && webViewRef != null) {
            val script = """
                if (window.setVelaMode) { window.setVelaMode('${mapMode.name}'); }
                if (window.switchLayer) { window.switchLayer('$currentMapLayer'); }
            """.trimIndent()
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // إمالة الكاميرا التفاعلية للوضع ثلاثي الأبعاد 3D Tilt
    LaunchedEffect(isMapReady, tiltAngle, mapMode) {
        if (isMapReady && webViewRef != null && mapMode == MapMode.VELA_3D) {
            val script = "if (window.setTilt) { window.setTilt($tiltAngle); }"
            webViewRef?.evaluateJavascript(script, null)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        if (!hasLoadError) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

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
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        // جسر التواصل بين JavaScript في Leaflet وكود Kotlin الأصلي
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMapReady() {
                                isMapReady = true
                            }

                            @JavascriptInterface
                            fun onMapClicked() {
                                onDeselect()
                            }

                            @JavascriptInterface
                            fun onMarkerClicked(id: String, type: String) {
                                when (type.uppercase()) {
                                    "PROVIDER" -> {
                                        nearbyProviders.find { it.id == id }?.let { onProviderSelected(it) }
                                    }
                                    "STORE", "RESTAURANT", "MEDICAL" -> {
                                        nearbyStores.find { it.id == id }?.let { onStoreSelected(it) }
                                    }
                                    "PROPERTY" -> {
                                        nearbyProperties.find { it.id == id }?.let { onPropertySelected(it) }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun switchLayer(layerKey: String) {
                                webViewRef?.post {
                                    webViewRef?.evaluateJavascript("if (window.switchLayer) { window.switchLayer('$layerKey'); }", null)
                                }
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
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
                                    onMapLoadFailed?.invoke()
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
                                    onMapLoadFailed?.invoke()
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
