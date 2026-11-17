package com.serviseyem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Shared utilities for the modularized Stores & Real Estate components.
 *
 * Modularized files (all in package com.serviseyem):
 * - SmartRecommendationsSection.kt (SmartRecommendationsSection, RecommendationItemCard)
 * - StoresCatalogComponents.kt (StoresTabContent, StoreListItemCard)
 * - PropertiesCatalogComponents.kt (PropertiesTabContent, PropertyListItemCard)
 * - StoreDetailsDialog.kt (StoreDetailsDialog)
 * - StoreProductModals.kt (BulkPriceAdjusterDialog, QuickAddProductDialog)
 * - PropertyDetailsDialog.kt (PropertyDetailsDialog)
 * - StoreCreateEditDialog.kt (StoreCreateEditDialog)
 * - PropertyCreateEditDialog.kt (PropertyCreateEditDialog)
 */

/**
 * Centralized Yemeni phone validator.
 * Used by: StoreCreateEditDialog, PropertyCreateEditDialog, CreateBookingScreen (via BookingValidation).
 */
internal fun isValidYemeniPhone(phone: String): Boolean {
    val clean = phone.trim().replace(" ", "").replace("+", "")
    return clean.length == 9 &&
        (clean.startsWith("77") || clean.startsWith("73") ||
         clean.startsWith("71") || clean.startsWith("70") ||
         clean.startsWith("78"))
}

@Composable
fun rememberBase64Bitmap(base64Str: String): ImageBitmap? {
    return remember(base64Str) {
        if (base64Str.isNotEmpty() && !base64Str.startsWith("http") && !base64Str.startsWith("content")) {
            try {
                val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
