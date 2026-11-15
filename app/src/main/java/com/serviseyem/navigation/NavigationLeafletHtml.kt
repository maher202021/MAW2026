package com.serviseyem.navigation

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

internal fun fetchNominatimPlaces(query: String): List<NavigationSearchResult> {
    return try {
        val encoded = URLEncoder.encode("$query, Yemen", "UTF-8")
        val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&countrycodes=ye&limit=5&addressdetails=1")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("User-Agent", "YemenServicesGuide/1.0 (Android)")
        }

        if (conn.responseCode == 200) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            val list = mutableListOf<NavigationSearchResult>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val displayName = obj.optString("display_name", "")
                val parts = displayName.split(",")
                val title = parts.firstOrNull()?.trim() ?: displayName
                val subtitle = parts.drop(1).take(2).joinToString(", ").trim()
                val lat = obj.optDouble("lat", 0.0)
                val lon = obj.optDouble("lon", 0.0)
                if (lat != 0.0 && lon != 0.0) {
                    list.add(NavigationSearchResult(title = title, subtitle = subtitle, lat = lat, lng = lon, isLocal = false))
                }
            }
            list
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun buildNavigationLeafletHtml(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double,
    destName: String,
    preferOsmOffline: Boolean = false
): String {
    val primaryTileUrl = if (preferOsmOffline) {
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    } else {
        "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
    }
    return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #070B14; }
            #map.no-tiles {
                background: #131B26;
                background-image:
                    linear-gradient(rgba(255,255,255,.04) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255,255,255,.04) 1px, transparent 1px);
                background-size: 64px 64px;
            }
            #offline-tiles-banner {
                display: none;
                position: fixed;
                bottom: 92px;
                left: 50%;
                transform: translateX(-50%);
                z-index: 900;
                background: rgba(15, 23, 42, 0.92);
                color: #FCD34D;
                border: 1px solid rgba(252, 211, 77, 0.4);
                border-radius: 999px;
                padding: 6px 14px;
                font-size: 11px;
                font-family: sans-serif;
                white-space: nowrap;
            }
            #osm-attribution {
                position: fixed;
                bottom: 8px;
                right: 8px;
                z-index: 900;
                background: rgba(15, 23, 42, 0.78);
                color: #CBD5E1;
                border-radius: 8px;
                padding: 2px 7px;
                font-size: 9.5px;
                font-family: sans-serif;
                pointer-events: none;
            }
            .user-pulse {
                width: 22px; height: 22px;
                background: #00E5FF;
                border: 3px solid #ffffff;
                border-radius: 50%;
                box-shadow: 0 0 15px #00E5FF;
            }
            .dest-pulse {
                width: 26px; height: 26px;
                background: #EF4444;
                border: 3px solid #ffffff;
                border-radius: 50%;
                box-shadow: 0 0 18px #EF4444;
            }
            .leaflet-control-attribution { display: none !important; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <div id="offline-tiles-banner">الخريطة تحتاج إنترنت أو حزمة عرض</div>
        <div id="osm-attribution">© مساهمو OpenStreetMap</div>
        <script>
            var map = L.map('map', {
                center: [$startLat, $startLon],
                zoom: 15,
                zoomControl: false
            });

            var baseLayer = L.tileLayer('$primaryTileUrl', {
                maxZoom: 19,
                subdomains: 'abcd'
            }).addTo(map);

            var tileErrCount = 0;
            function notifyNavTiles(failing) {
                var el = document.getElementById('map');
                var b = document.getElementById('offline-tiles-banner');
                if (el) el.classList.toggle('no-tiles', failing);
                if (b) b.style.display = failing ? 'block' : 'none';
            }
            baseLayer.on('tileerror', function(error, tile) {
                if (tile && !tile._hasOsmFallback && '$primaryTileUrl'.indexOf('openstreetmap.org') === -1) {
                    tile._hasOsmFallback = true;
                    tile.src = 'https://tile.openstreetmap.org/' + error.coords.z + '/' + error.coords.x + '/' + error.coords.y + '.png';
                    return;
                }
                tileErrCount++;
                if (tileErrCount >= 4) notifyNavTiles(true);
            });
            baseLayer.on('tileload', function() { tileErrCount = 0; notifyNavTiles(false); });

            var userIcon = L.divIcon({ className: 'user-pulse', iconSize: [22, 22] });
            var destIcon = L.divIcon({ className: 'dest-pulse', iconSize: [26, 26] });

            var userMarker = L.marker([$startLat, $startLon], { icon: userIcon }).addTo(map);
            var destMarker = L.marker([$endLat, $endLon], { icon: destIcon }).addTo(map).bindPopup('$destName');

            var routePolyline = null;
            var pickMode = false;
            window.setPickMode = function(on) {
                pickMode = !!on;
                var el = document.getElementById('map');
                if (el) el.style.cursor = on ? 'crosshair' : '';
            };
            map.on('click', function(e) {
                if (!pickMode) return;
                if (window.NavBridge && window.NavBridge.onNavMapPicked) {
                    window.NavBridge.onNavMapPicked(e.latlng.lat, e.latlng.lng);
                }
            });

            window.renderNavigationRouteSafe = function(args) {
                var sLat = args.sLat;
                var sLng = args.sLng;
                var eLat = args.eLat;
                var eLng = args.eLng;
                var coords = args.coords || [];
                var title = args.title || '';
                window.renderNavigationRoute(sLat, sLng, eLat, eLng, coords, title);
            };

            window.renderNavigationRoute = function(sLat, sLng, eLat, eLng, coords, title) {
                userMarker.setLatLng([sLat, sLng]);
                destMarker.setLatLng([eLat, eLng]);
                destMarker.setPopupContent(title);

                if (routePolyline) {
                    map.removeLayer(routePolyline);
                }

                var latlngs = [];
                for (var i = 0; i < coords.length; i++) {
                    latlngs.push([coords[i][0], coords[i][1]]);
                }

                routePolyline = L.polyline(latlngs, {
                    color: '#00E5FF',
                    weight: 6,
                    opacity: 0.9,
                    lineJoin: 'round'
                }).addTo(map);

                map.fitBounds(routePolyline.getBounds(), { padding: [80, 80] });
            };

            window.updateUserPosition = function(lat, lng, heading) {
                userMarker.setLatLng([lat, lng]);
            };

            window.centerOnUser = function(lat, lng) {
                map.setView([lat, lng], 17, { animate: true });
            };
        </script>
    </body>
    </html>
    """.trimIndent()
}
