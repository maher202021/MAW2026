package com.serviseyem.ui.screens.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import com.serviseyem.ui.screens.map.utils.OfflineTileInterceptor
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.resolveThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🗺️ RealLeafletMapView (Vela 3D + OpenAerialMap Satellite Engine)
 * - تحميل خريطة الأقمار الاصطناعية الافتراضية عالية الدقة بدون أي مفتاح API
 * - دعم وضع Vela 3D التفاعلي مع المباني ثلاثية الأبعاد وإمالة الكاميرا
 * - دعم كامل للمس المتعدد والتكبير والتصغير السلس (Pinch / Multi-Touch Zoom)
 * - ربط العلامات الملونة حسب نوع الكيان مع Bottom Sheet تفاعلي
 * - مؤشر نابض أزرق لموقع المستخدم الفعلي
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealLeafletMapView(
    userCoords: Pair<Double, Double>,
    nearbyProviders: List<ProviderEntity>,
    nearbyStores: List<StoreEntity>,
    nearbyProperties: List<PropertyEntity>,
    dynamicOffsets: Map<String, Pair<Double, Double>>,
    currentMapMode: MapMode = MapMode.SATELLITE,
    selectedCity: String = "الكل",
    zoomScale: Float = 1.0f,
    onZoomScaleChange: (Float) -> Unit = {},
    panOffset: Offset = Offset.Zero,
    onPanOffsetChange: (Offset) -> Unit = {},
    selectedEntity: Any? = null,
    onProviderSelected: (ProviderEntity) -> Unit,
    onStoreSelected: (StoreEntity) -> Unit,
    onPropertySelected: (PropertyEntity) -> Unit,
    onDeselect: () -> Unit = {},
    onSwitchToRadar: (() -> Unit)? = null,
    onMapLoadFailed: (() -> Unit)? = null,
    tilt: Float = 35f,
    rotation: Float = 0f,
    routeCoordinates: List<Pair<Double, Double>> = emptyList(),
    routeKey: Int = 0,
    waypointMarkers: List<Triple<String, Double, Double>> = emptyList(),
    followUser: Boolean = false,
    onUserPanned: (() -> Unit)? = null,
    focusPoint: Triple<String, Double, Double>? = null,
    themeColors: VisualThemePalette = resolveThemePalette(AdminSettingsEntity()),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMapReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasLoadError by remember { mutableStateOf(false) }
    var didInitialCenter by remember { mutableStateOf(false) }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val providersState by rememberUpdatedState(nearbyProviders)
    val storesState by rememberUpdatedState(nearbyStores)
    val propertiesState by rememberUpdatedState(nearbyProperties)
    val onProviderSelectedState by rememberUpdatedState(onProviderSelected)
    val onStoreSelectedState by rememberUpdatedState(onStoreSelected)
    val onPropertySelectedState by rememberUpdatedState(onPropertySelected)
    val onDeselectState by rememberUpdatedState(onDeselect)
    val onUserPannedState by rememberUpdatedState(onUserPanned)

    // [المحور الثالث] حماية الشاشة السوداء: مؤقت 6 ثوانٍ. إذا لم تصل إشارة onMapReady يتم التحويل تلقائياً لـ OfflineInteractiveMap
    LaunchedEffect(isMapReady) {
        if (!isMapReady) {
            kotlinx.coroutines.delay(6000L)
            if (!isMapReady) {
                hasLoadError = true
                onMapLoadFailed?.invoke()
            }
        }
    }

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

    // إرسال العلامات للخريطة عند اكتمال الجاهزية أو تغير البيانات بأمان تام عبر Base64
    LaunchedEffect(isMapReady, markersJson) {
        if (isMapReady && webViewRef != null && markersJson.isNotBlank()) {
            val b64 = Base64.encodeToString(markersJson.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val script = "if (window.setMarkersBase64) { window.setMarkersBase64('$b64'); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تبديل أوضاع الخريطة
    LaunchedEffect(isMapReady, currentMapMode) {
        if (isMapReady && webViewRef != null) {
            val modeKey = when (currentMapMode) {
                MapMode.VELA_3D -> "VELA_3D"
                MapMode.OSM -> "OSM"
                MapMode.HOT -> "HOT"
                MapMode.TOPO -> "TOPO"
                MapMode.SATELLITE -> "SATELLITE"
                else -> "SATELLITE"
            }
            val script = "if (window.switchMapMode) { window.switchMapMode('$modeKey'); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تحديث موقع المستخدم الفعلي على الخريطة
    LaunchedEffect(isMapReady, userCoords, followUser) {
        if (isMapReady && webViewRef != null && userCoords.first != 0.0 && userCoords.second != 0.0) {
            val pan = followUser || !didInitialCenter
            didInitialCenter = true
            val script = "if (window.updateUserLocation) { window.updateUserLocation(${userCoords.first}, ${userCoords.second}, 30, $pan); }"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // تمركز الخريطة عند تغيير المدينة (عدم التحويل لمركز صنعاء عند اختيار "الكل")
    LaunchedEffect(isMapReady, selectedCity) {
        if (!isMapReady || webViewRef == null) return@LaunchedEffect
        if (selectedCity == "الكل") return@LaunchedEffect
        val cityCoords = OfflineMapManager.getCityCoordinates(selectedCity)
        val script = "if (window.setCity) { window.setCity('${cityCoords.nameAr}', ${cityCoords.latitude}, ${cityCoords.longitude}, ${cityCoords.defaultZoom}); }"
        webViewRef?.evaluateJavascript(script, null)
    }

    // التحكم في التكبير والتصغير التفاعلي
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

    // إعادة التمركز إلى موقع المستخدم
    LaunchedEffect(panOffset) {
        if (isMapReady && webViewRef != null && panOffset == Offset.Zero) {
            webViewRef?.evaluateJavascript("if (window.recenterUser) { window.recenterUser(); }", null)
        }
    }

    // رسم ومسح مسار الملاحة على خريطة Leaflet
    LaunchedEffect(isMapReady, routeCoordinates, routeKey) {
        if (!isMapReady || webViewRef == null) return@LaunchedEffect
        if (routeCoordinates.size < 2) {
            webViewRef?.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null)
            return@LaunchedEffect
        }
        val b64 = withContext(Dispatchers.Default) {
            val step = maxOf(1, routeCoordinates.size / 2000)
            val arr = JSONArray()
            var i = 0
            while (i < routeCoordinates.size) {
                arr.put(JSONArray().put(routeCoordinates[i].first).put(routeCoordinates[i].second))
                i += step
            }
            val last = routeCoordinates.last()
            arr.put(JSONArray().put(last.first).put(last.second))
            Base64.encodeToString(arr.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
        webViewRef?.evaluateJavascript("if (window.setRouteBase64) { window.setRouteBase64('$b64', ${!followUser}); }", null)
    }

    // محطات المسار المرقمة
    LaunchedEffect(isMapReady, waypointMarkers) {
        if (!isMapReady || webViewRef == null) return@LaunchedEffect
        val arr = JSONArray()
        waypointMarkers.forEach { (label, lat, lng) ->
            arr.put(JSONObject().put("label", label).put("lat", lat).put("lng", lng))
        }
        val b64 = Base64.encodeToString(arr.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        webViewRef?.evaluateJavascript("if (window.setWaypointMarkersBase64) { window.setWaypointMarkersBase64('$b64'); }", null)
    }

    // دبوس نتيجة البحث
    LaunchedEffect(isMapReady, focusPoint) {
        if (!isMapReady || webViewRef == null) return@LaunchedEffect
        if (focusPoint == null) {
            webViewRef?.evaluateJavascript("if (window.clearSearchPin) { window.clearSearchPin(); }", null)
        } else {
            val o = JSONObject().put("name", focusPoint.first).put("lat", focusPoint.second).put("lng", focusPoint.third)
            val b64 = Base64.encodeToString(o.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            webViewRef?.evaluateJavascript("if (window.showSearchPinBase64) { window.showSearchPinBase64('$b64'); }", null)
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
                            allowFileAccess = false
                            allowContentAccess = false
                            javaScriptCanOpenWindowsAutomatically = false
                        }

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMapReady() {
                                mainHandler.post { isMapReady = true }
                            }

                            @JavascriptInterface
                            fun onMapClicked() {
                                mainHandler.post { onDeselectState() }
                            }

                            @JavascriptInterface
                            fun onUserPanned() {
                                mainHandler.post { onUserPannedState?.invoke() }
                            }

                            @JavascriptInterface
                            fun onMarkerSelected(id: String, type: String) {
                                handleMarkerClick(id, type)
                            }

                            @JavascriptInterface
                            fun onMarkerClicked(id: String, type: String) {
                                handleMarkerClick(id, type)
                            }

                            private fun handleMarkerClick(id: String, type: String) {
                                mainHandler.post {
                                    when (type.uppercase()) {
                                        "PROVIDER" -> {
                                            providersState.find { it.id == id }?.let { onProviderSelectedState(it) }
                                        }
                                        "STORE", "RESTAURANT", "MEDICAL" -> {
                                            storesState.find { it.id == id }?.let { onStoreSelectedState(it) }
                                        }
                                        "PROPERTY" -> {
                                            propertiesState.find { it.id == id }?.let { onPropertySelectedState(it) }
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onMapModeChanged(newMode: String) {
                                android.util.Log.d("LeafletBridge", "Map mode: $newMode")
                            }

                            @JavascriptInterface
                            fun getAvailableLayers(): String {
                                return "[\"OSM\",\"HOT\",\"TOPO\",\"SATELLITE\",\"VELA_3D\"]"
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val url = request?.url?.toString() ?: return null
                                return try {
                                    OfflineTileInterceptor.intercept(context, url)
                                } catch (_: Throwable) {
                                    null
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return true
                            }

                            @Deprecated("Deprecated in Java")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return true
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
                                if (failingUrl?.contains("map.html") == true) {
                                    hasLoadError = true
                                    onMapLoadFailed?.invoke()
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
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
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.removeJavascriptInterface("AndroidBridge")
                    webView.destroy()
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
