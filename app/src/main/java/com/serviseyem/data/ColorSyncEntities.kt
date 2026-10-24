package com.serviseyem.data

import androidx.annotation.Keep

@Keep
enum class ColorSyncStatus {
    SYNCED,
    SYNCING,
    NOT_SYNCED,
    CONFLICT
}

@Keep
data class CategoryColors(
    val all: String = "#4CAF50",
    val shops: String = "#2196F3",
    val restaurants: String = "#FF9800",
    val medical: String = "#E91E63",
    val technicians: String = "#9C27B0"
)

@Keep
data class StatusColors(
    val available: String = "#4CAF50",
    val busy: String = "#FF9800",
    val unavailable: String = "#F44336"
)

@Keep
data class MarkerColors(
    val default: String = "#4CAF50",
    val selected: String = "#FF5722",
    val nearby: String = "#03A9F4"
)

@Keep
data class BookingColors(
    val pending: String = "#FFC107",
    val confirmed: String = "#4CAF50",
    val cancelled: String = "#F44336",
    val completed: String = "#9E9E9E"
)

@Keep
data class ChatColors(
    val sent: String = "#E1F5FE",
    val received: String = "#FFFFFF",
    val unread: String = "#F44336"
)

@Keep
data class UiColors(
    val primary: String = "#1A237E",
    val secondary: String = "#0D47A1",
    val accent: String = "#FF5722",
    val background: String = "#FFFFFF",
    val surface: String = "#F5F5F5",
    val text: String = "#212121",
    val textSecondary: String = "#757575"
)

@Keep
data class ColorsHolder(
    val categories: CategoryColors = CategoryColors(),
    val status: StatusColors = StatusColors(),
    val markers: MarkerColors = MarkerColors(),
    val booking: BookingColors = BookingColors(),
    val chat: ChatColors = ChatColors(),
    val ui: UiColors = UiColors()
)

@Keep
data class ColorSchemeEntity(
    val version: Int = 1,
    val lastUpdated: String = "2024-01-15T10:30:00Z",
    val colors: ColorsHolder = ColorsHolder()
)

@Keep
data class PersonalColors(
    val favorite: String = "#FF5722",
    val theme: String = "dark",
    val accent: String = "#4CAF50",
    val custom: Map<String, String> = emptyMap()
)

@Keep
data class UserColorsEntity(
    val personalColors: PersonalColors = PersonalColors(),
    val colorsLastSynced: String = "2024-01-15T10:30:00Z"
)

@Keep
data class SyncLogEntity(
    val syncId: String = "",
    val timestamp: String = "",
    val type: String = "colors",
    val status: String = "success", // success / failed / conflict
    val changes: List<String> = emptyList(),
    val versionFrom: Int = 1,
    val versionTo: Int = 1
)
