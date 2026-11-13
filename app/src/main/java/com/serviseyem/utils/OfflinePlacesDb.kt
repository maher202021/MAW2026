package com.serviseyem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.serviseyem.navigation.HaversinePrecisionNavigator
import com.serviseyem.navigation.RealValhallaNavigator
import java.io.File
import java.util.Locale

object OfflinePlacesDb {
    @Volatile
    private var db: SQLiteDatabase? = null

    fun file(context: Context) = File(context.filesDir, "places/places.db")

    fun isInstalled(context: Context): Boolean = file(context).let { it.exists() && it.length() > 100_000L }

    fun rowCount(context: Context): Long {
        val database = open(context) ?: return 0L
        return try {
            database.rawQuery("SELECT COUNT(*) FROM places", null).use { c ->
                if (c.moveToFirst()) c.getLong(0) else 0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    @Synchronized
    private fun open(context: Context): SQLiteDatabase? {
        db?.let { if (it.isOpen) return it }
        val f = file(context)
        if (!f.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(f.absolutePath, null, SQLiteDatabase.OPEN_READONLY).also { db = it }
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun close() {
        try {
            db?.close()
        } catch (_: Exception) {
        }
        db = null
    }

    private fun likeEsc(s: String) = s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun iconFor(kind: String) = when (kind) {
        "city" -> "🏙️"
        "town" -> "🏘️"
        "village", "hamlet" -> "🏡"
        "road" -> "🛣️"
        "poi" -> "📌"
        else -> "📍"
    }

    private fun arabicKind(kind: String) = when (kind) {
        "city" -> "مدينة"
        "town" -> "بلدة"
        "village" -> "قرية"
        "hamlet" -> "قرية صغيرة"
        "suburb" -> "ضاحية"
        "neighbourhood" -> "حي"
        "road" -> "شارع"
        "poi" -> "معلم"
        else -> "مكان"
    }

    private data class Row(
        val name: String,
        val norm: String,
        val kind: String,
        val imp: Int,
        val lat: Double,
        val lng: Double
    )

    fun search(
        context: Context,
        query: String,
        userLat: Double,
        userLng: Double,
        limit: Int = 20
    ): List<RealValhallaNavigator.SearchAddressResult> {
        val database = open(context) ?: return emptyList()
        val variants = DialectSynonyms.expand(query)
            .ifEmpty { listOf(ArabicNormalizer.normalize(query)) }
            .filter { it.length >= 2 }
            .take(3)
        val rows = LinkedHashMap<String, Pair<Row, Int>>()
        for (v in variants) {
            val esc = likeEsc(v)
            try {
                database.rawQuery(
                    "SELECT name, name_norm, kind, importance, lat, lng FROM places WHERE name_norm LIKE ? ESCAPE '\\' ORDER BY importance DESC LIMIT 80",
                    arrayOf("%$esc%")
                ).use { c ->
                    while (c.moveToNext()) {
                        val r = Row(c.getString(0), c.getString(1), c.getString(2), c.getInt(3), c.getDouble(4), c.getDouble(5))
                        val q = if (r.norm == v) 2 else if (r.norm.startsWith(v)) 1 else 0
                        val k = "${r.norm}_${"%.3f".format(Locale.US, r.lat)}_${"%.3f".format(Locale.US, r.lng)}"
                        val prev = rows[k]
                        if (prev == null || prev.second < q) rows[k] = r to q
                    }
                }
            } catch (_: Exception) {
                // تجاهل فشل استعلام واحد
            }
        }
        val hasUser = LandmarkAddress.isValid(userLat, userLng)
        return rows.values.sortedWith(
            compareByDescending<Pair<Row, Int>> { it.second }
                .thenByDescending { it.first.imp }
                .thenBy {
                    if (hasUser) HaversinePrecisionNavigator.calculateDistance(userLat, userLng, it.first.lat, it.first.lng)
                    else 0.0
                }
        ).take(limit).map { (r, _) ->
            RealValhallaNavigator.SearchAddressResult(
                displayName = "${r.name} — ${arabicKind(r.kind)}",
                shortName = r.name,
                latitude = r.lat,
                longitude = r.lng,
                category = "offline",
                icon = iconFor(r.kind)
            )
        }
    }

    /** أقرب مكان مسمّى ضمن maxMeters (لتسمية النقطة المختارة من الخريطة). */
    fun nearest(context: Context, lat: Double, lng: Double, maxMeters: Double = 400.0): String? {
        val database = open(context) ?: return null
        val dLat = maxMeters / 111_195.0
        val dLng = dLat / kotlin.math.cos(Math.toRadians(lat)).coerceAtLeast(0.2)
        return try {
            database.rawQuery(
                "SELECT name, lat, lng FROM places WHERE lat BETWEEN ? AND ? AND lng BETWEEN ? AND ? LIMIT 60",
                arrayOf((lat - dLat).toString(), (lat + dLat).toString(), (lng - dLng).toString(), (lng + dLng).toString())
            ).use { c ->
                var best: String? = null
                var bestD = maxMeters
                while (c.moveToNext()) {
                    val d = HaversinePrecisionNavigator.calculateDistance(lat, lng, c.getDouble(1), c.getDouble(2))
                    if (d < bestD) {
                        bestD = d
                        best = c.getString(0)
                    }
                }
                best
            }
        } catch (_: Exception) {
            null
        }
    }

    fun listMajor(context: Context, limit: Int = 150): List<RealValhallaNavigator.SearchAddressResult> {
        val database = open(context) ?: return emptyList()
        return try {
            database.rawQuery(
                "SELECT name, kind, lat, lng FROM places WHERE kind IN ('city','town','suburb','neighbourhood') ORDER BY importance DESC LIMIT ?",
                arrayOf(limit.toString())
            ).use { c ->
                val out = mutableListOf<RealValhallaNavigator.SearchAddressResult>()
                while (c.moveToNext()) {
                    out.add(
                        RealValhallaNavigator.SearchAddressResult(
                            displayName = c.getString(0),
                            shortName = c.getString(0),
                            latitude = c.getDouble(2),
                            longitude = c.getDouble(3),
                            category = "offline",
                            icon = iconFor(c.getString(1))
                        )
                    )
                }
                out
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
