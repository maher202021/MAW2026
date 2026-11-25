package com.serviseyem.navigation

import org.json.JSONObject

internal fun buildNavigationLeafletHtml(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double,
    destName: String,
    preferOsmOffline: Boolean = false
): String {
    val primaryTileUrl = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    val subdomainsValue = "abc"
    val safeDestJson = JSONObject.quote(destName)
    return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="file:///android_asset/leaflet.css" />
        <script src="file:///android_asset/leaflet.js"></script>
        <style>
            html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #14202E; }
            #map.no-tiles {
                background: #14202E;
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
                background: rgba(11, 18, 32, 0.94);
                color: #FCD34D;
                border: 1px solid rgba(252, 211, 77, 0.45);
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
                background: rgba(11, 18, 32, 0.82);
                color: #CBD5E1;
                border-radius: 8px;
                padding: 2px 7px;
                font-size: 9.5px;
                font-family: sans-serif;
                pointer-events: none;
            }
            .user-nav-arrow {
                width: 30px; height: 30px;
                display: flex; align-items: center; justify-content: center;
                transition: transform 0.25s ease;
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
            // [نظام خرائط وملاحة عالمي - ملكنا]
            function isValidGps(lat, lng) {
                return isFinite(lat) && isFinite(lng) && !(lat === 0 && lng === 0) && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
            }
            var initStartValid = isValidGps($startLat, $startLon);
            var initCenter = initStartValid ? [$startLat, $startLon] : [$endLat, $endLon];

            var map = L.map('map', {
                center: initCenter,
                zoom: initStartValid ? 15 : 14,
                zoomControl: false,
                zoomSnap: 1,
                zoomDelta: 1,
                preferCanvas: true,
                fadeAnimation: false,
                markerZoomAnimation: false
            });

            var TRANSPARENT_TILE = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';
            var baseLayer = L.tileLayer('$primaryTileUrl', {
                maxZoom: 19,
                subdomains: '$subdomainsValue',
                updateWhenIdle: true,
                updateWhenZooming: false,
                keepBuffer: 4,
                detectRetina: false,
                crossOrigin: false,
                errorTileUrl: TRANSPARENT_TILE
            }).addTo(map);

            var tileErrCount = 0;
            var notifiedTilesLoaded = false;
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
                if (tileErrCount >= 5) {
                    notifyNavTiles(true);
                    if (window.NavBridge && window.NavBridge.onTilesFailed) {
                        window.NavBridge.onTilesFailed();
                    }
                }
            });
            baseLayer.on('tileload', function() {
                tileErrCount = 0;
                notifyNavTiles(false);
                if (!notifiedTilesLoaded) {
                    notifiedTilesLoaded = true;
                    if (window.NavBridge && window.NavBridge.onTilesLoaded) {
                        window.NavBridge.onTilesLoaded();
                    }
                }
            });

            function makeUserHtml(deg) {
                var rot = isFinite(deg) ? deg : 0;
                return '<div class="user-nav-arrow" style="transform:rotate(' + rot + 'deg)">' +
                    '<svg width="30" height="30" viewBox="0 0 30 30">' +
                    '<circle cx="15" cy="15" r="12" fill="rgba(58,107,255,0.28)" />' +
                    '<path d="M15 3 L23 24 L15 19 L7 24 Z" fill="#00E5FF" stroke="#FFFFFF" stroke-width="2" stroke-linejoin="round"/>' +
                    '</svg></div>';
            }
            var userIcon = L.divIcon({ className: '', html: makeUserHtml(0), iconSize: [30, 30], iconAnchor: [15, 15] });
            var destIcon = L.divIcon({ className: 'dest-pulse', iconSize: [26, 26], iconAnchor: [13, 13] });

            var userMarker = initStartValid ? L.marker([$startLat, $startLon], { icon: userIcon }).addTo(map) : null;
            var destMarker = L.marker([$endLat, $endLon], { icon: destIcon }).addTo(map).bindPopup('');
            destMarker.getPopup().setContent($safeDestJson);

            var routeGlow = null;
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
                window.renderNavigationRoute(args.sLat, args.sLng, args.eLat, args.eLng, args.coords || [], args.title || '');
            };

            window.renderNavigationRoute = function(sLat, sLng, eLat, eLng, coords, title) {
                if (isValidGps(sLat, sLng)) {
                    if (!userMarker) {
                        userMarker = L.marker([sLat, sLng], { icon: userIcon }).addTo(map);
                    } else {
                        userMarker.setLatLng([sLat, sLng]);
                    }
                }
                if (isValidGps(eLat, eLng)) {
                    destMarker.setLatLng([eLat, eLng]);
                }
                destMarker.setPopupContent(String(title || ''));

                if (routeGlow) { map.removeLayer(routeGlow); routeGlow = null; }
                if (routePolyline) { map.removeLayer(routePolyline); routePolyline = null; }

                var latlngs = [];
                for (var i = 0; i < coords.length; i++) {
                    if (isValidGps(coords[i][0], coords[i][1])) {
                        latlngs.push([coords[i][0], coords[i][1]]);
                    }
                }

                if (latlngs.length > 0) {
                    routeGlow = L.polyline(latlngs, {
                        color: '#1D4ED8',
                        weight: 12,
                        opacity: 0.35,
                        lineJoin: 'round'
                    }).addTo(map);
                    routePolyline = L.polyline(latlngs, {
                        color: '#00E5FF',
                        weight: 6,
                        opacity: 0.95,
                        lineJoin: 'round'
                    }).addTo(map);
                    map.fitBounds(routePolyline.getBounds(), { padding: [80, 80] });
                } else if (isValidGps(eLat, eLng)) {
                    map.setView([eLat, eLng], 15, { animate: true });
                }
            };

            window.updateUserPosition = function(lat, lng, heading) {
                if (!isValidGps(lat, lng)) return;
                if (!userMarker) {
                    userMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map);
                } else {
                    userMarker.setLatLng([lat, lng]);
                }
                var arrow = userMarker.getElement() && userMarker.getElement().querySelector('.user-nav-arrow');
                if (arrow && isFinite(heading)) {
                    arrow.style.transform = 'rotate(' + heading + 'deg)';
                }
            };

            window.applySmartZoom = function(zoomLevel) {
                if (!isFinite(zoomLevel)) return;
                var cur = map.getZoom();
                if (Math.abs(cur - zoomLevel) >= 1) {
                    map.setZoom(zoomLevel, { animate: true });
                }
            };

            window.centerOnUser = function(lat, lng) {
                if (!isValidGps(lat, lng)) return;
                map.setView([lat, lng], 17, { animate: true });
            };

            window.centerOnDestination = function(lat, lng) {
                if (!isValidGps(lat, lng)) return;
                map.setView([lat, lng], 15, { animate: true });
            };
        </script>
    </body>
    </html>
    """.trimIndent()
}
