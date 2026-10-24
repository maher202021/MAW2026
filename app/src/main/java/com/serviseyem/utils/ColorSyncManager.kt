package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.*
import org.json.JSONArray
import org.json.JSONObject

object ColorSyncManager {

    private const val PREFS_NAME = "yemen_service_prefs"
    private const val KEY_COLOR_SCHEME = "color_scheme_json"
    private const val KEY_PERSONAL_COLORS = "personal_colors_json"
    private const val KEY_SYNC_LOGS = "color_sync_logs_json"

    fun saveLocalColorScheme(context: Context, scheme: ColorSchemeEntity) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_COLOR_SCHEME, serializeColorScheme(scheme)).apply()
    }

    fun getLocalColorScheme(context: Context): ColorSchemeEntity {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_COLOR_SCHEME, null) ?: return ColorSchemeEntity()
        return try {
            deserializeColorScheme(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            ColorSchemeEntity()
        }
    }

    fun saveLocalPersonalColors(context: Context, personal: UserColorsEntity) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_PERSONAL_COLORS, serializePersonalColors(personal)).apply()
    }

    fun getLocalPersonalColors(context: Context): UserColorsEntity {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_PERSONAL_COLORS, null) ?: return UserColorsEntity()
        return try {
            deserializePersonalColors(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            UserColorsEntity()
        }
    }

    fun getSyncLogs(context: Context): List<SyncLogEntity> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_SYNC_LOGS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<SyncLogEntity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val changesList = mutableListOf<String>()
                val changesArr = obj.optJSONArray("changes")
                if (changesArr != null) {
                    for (j in 0 until changesArr.length()) {
                        changesList.add(changesArr.getString(j))
                    }
                }
                list.add(
                    SyncLogEntity(
                        syncId = obj.optString("syncId", ""),
                        timestamp = obj.optString("timestamp", ""),
                        type = obj.optString("type", "colors"),
                        status = obj.optString("status", "success"),
                        changes = changesList,
                        versionFrom = obj.optInt("versionFrom", 1),
                        versionTo = obj.optInt("versionTo", 1)
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveSyncLog(context: Context, log: SyncLogEntity) {
        val currentLogs = getSyncLogs(context).toMutableList()
        currentLogs.add(0, log) // Insert at beginning (most recent)
        if (currentLogs.size > 50) { // Cap at 50 logs to conserve space
            currentLogs.removeAt(currentLogs.size - 1)
        }
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val arr = JSONArray()
            for (item in currentLogs) {
                val obj = JSONObject()
                obj.put("syncId", item.syncId)
                obj.put("timestamp", item.timestamp)
                obj.put("type", item.type)
                obj.put("status", item.status)
                val changesArr = JSONArray()
                item.changes.forEach { changesArr.put(it) }
                obj.put("changes", changesArr)
                obj.put("versionFrom", item.versionFrom)
                obj.put("versionTo", item.versionTo)
                arr.put(obj)
            }
            sp.edit().putString(KEY_SYNC_LOGS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun serializeColorScheme(scheme: ColorSchemeEntity): String {
        val root = JSONObject()
        root.put("version", scheme.version)
        root.put("lastUpdated", scheme.lastUpdated)

        val colorsObj = JSONObject()

        val cats = scheme.colors.categories
        val catsObj = JSONObject().apply {
            put("all", cats.all)
            put("shops", cats.shops)
            put("restaurants", cats.restaurants)
            put("medical", cats.medical)
            put("technicians", cats.technicians)
        }
        colorsObj.put("categories", catsObj)

        val status = scheme.colors.status
        val statusObj = JSONObject().apply {
            put("available", status.available)
            put("busy", status.busy)
            put("unavailable", status.unavailable)
        }
        colorsObj.put("status", statusObj)

        val markers = scheme.colors.markers
        val markersObj = JSONObject().apply {
            put("default", markers.default)
            put("selected", markers.selected)
            put("nearby", markers.nearby)
        }
        colorsObj.put("markers", markersObj)

        val booking = scheme.colors.booking
        val bookingObj = JSONObject().apply {
            put("pending", booking.pending)
            put("confirmed", booking.confirmed)
            put("cancelled", booking.cancelled)
            put("completed", booking.completed)
        }
        colorsObj.put("booking", bookingObj)

        val chat = scheme.colors.chat
        val chatObj = JSONObject().apply {
            put("sent", chat.sent)
            put("received", chat.received)
            put("unread", chat.unread)
        }
        colorsObj.put("chat", chatObj)

        val ui = scheme.colors.ui
        val uiObj = JSONObject().apply {
            put("primary", ui.primary)
            put("secondary", ui.secondary)
            put("accent", ui.accent)
            put("background", ui.background)
            put("surface", ui.surface)
            put("text", ui.text)
            put("textSecondary", ui.textSecondary)
        }
        colorsObj.put("ui", uiObj)

        root.put("colors", colorsObj)
        return root.toString()
    }

    fun deserializeColorScheme(jsonStr: String): ColorSchemeEntity {
        val root = JSONObject(jsonStr)
        val version = root.optInt("version", 1)
        val lastUpdated = root.optString("lastUpdated", "2024-01-15T10:30:00Z")

        val colorsObj = root.optJSONObject("colors") ?: return ColorSchemeEntity(version, lastUpdated)

        val catsObj = colorsObj.optJSONObject("categories")
        val categories = if (catsObj != null) {
            CategoryColors(
                all = catsObj.optString("all", "#4CAF50"),
                shops = catsObj.optString("shops", "#2196F3"),
                restaurants = catsObj.optString("restaurants", "#FF9800"),
                medical = catsObj.optString("medical", "#E91E63"),
                technicians = catsObj.optString("technicians", "#9C27B0")
            )
        } else CategoryColors()

        val statusObj = colorsObj.optJSONObject("status")
        val status = if (statusObj != null) {
            StatusColors(
                available = statusObj.optString("available", "#4CAF50"),
                busy = statusObj.optString("busy", "#FF9800"),
                unavailable = statusObj.optString("unavailable", "#F44336")
            )
        } else StatusColors()

        val markersObj = colorsObj.optJSONObject("markers")
        val markers = if (markersObj != null) {
            MarkerColors(
                default = markersObj.optString("default", "#4CAF50"),
                selected = markersObj.optString("selected", "#FF5722"),
                nearby = markersObj.optString("nearby", "#03A9F4")
            )
        } else MarkerColors()

        val bookingObj = colorsObj.optJSONObject("booking")
        val booking = if (bookingObj != null) {
            BookingColors(
                pending = bookingObj.optString("pending", "#FFC107"),
                confirmed = bookingObj.optString("confirmed", "#4CAF50"),
                cancelled = bookingObj.optString("cancelled", "#F44336"),
                completed = bookingObj.optString("completed", "#9E9E9E")
            )
        } else BookingColors()

        val chatObj = colorsObj.optJSONObject("chat")
        val chat = if (chatObj != null) {
            ChatColors(
                sent = chatObj.optString("sent", "#E1F5FE"),
                received = chatObj.optString("received", "#FFFFFF"),
                unread = chatObj.optString("unread", "#F44336")
            )
        } else ChatColors()

        val uiObj = colorsObj.optJSONObject("ui")
        val ui = if (uiObj != null) {
            UiColors(
                primary = uiObj.optString("primary", "#1A237E"),
                secondary = uiObj.optString("secondary", "#0D47A1"),
                accent = uiObj.optString("accent", "#FF5722"),
                background = uiObj.optString("background", "#FFFFFF"),
                surface = uiObj.optString("surface", "#F5F5F5"),
                text = uiObj.optString("text", "#212121"),
                textSecondary = uiObj.optString("textSecondary", "#757575")
            )
        } else UiColors()

        return ColorSchemeEntity(
            version = version,
            lastUpdated = lastUpdated,
            colors = ColorsHolder(
                categories = categories,
                status = status,
                markers = markers,
                booking = booking,
                chat = chat,
                ui = ui
            )
        )
    }

    fun serializePersonalColors(personal: UserColorsEntity): String {
        val root = JSONObject()
        root.put("colorsLastSynced", personal.colorsLastSynced)

        val pcObj = JSONObject()
        val pc = personal.personalColors
        pcObj.put("favorite", pc.favorite)
        pcObj.put("theme", pc.theme)
        pcObj.put("accent", pc.accent)

        val customObj = JSONObject()
        pc.custom.forEach { (k, v) ->
            customObj.put(k, v)
        }
        pcObj.put("custom", customObj)

        root.put("personalColors", pcObj)
        return root.toString()
    }

    fun deserializePersonalColors(jsonStr: String): UserColorsEntity {
        val root = JSONObject(jsonStr)
        val lastSynced = root.optString("colorsLastSynced", "2024-01-15T10:30:00Z")

        val pcObj = root.optJSONObject("personalColors") ?: return UserColorsEntity(colorsLastSynced = lastSynced)
        val favorite = pcObj.optString("favorite", "#FF5722")
        val theme = pcObj.optString("theme", "dark")
        val accent = pcObj.optString("accent", "#4CAF50")

        val customMap = mutableMapOf<String, String>()
        val customObj = pcObj.optJSONObject("custom")
        if (customObj != null) {
            val keys = customObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                customMap[k] = customObj.getString(k)
            }
        }

        return UserColorsEntity(
            personalColors = PersonalColors(
                favorite = favorite,
                theme = theme,
                accent = accent,
                custom = customMap
            ),
            colorsLastSynced = lastSynced
        )
    }
}
