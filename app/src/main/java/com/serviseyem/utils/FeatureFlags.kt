package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * FeatureFlags manager to toggle features dynamically across the application.
 */
object FeatureFlags {
    private const val PREFS_NAME = "app_feature_flags"
    private const val KEY_ENABLE_AI_RECOMMENDATIONS = "enable_ai_recommendations"
    private const val KEY_ENABLE_INSTANT_URGENT_NOTIFS = "enable_instant_urgent_notifs"
    private const val KEY_ENABLE_ADVANCED_FILTERS = "enable_advanced_filters"
    private const val KEY_ENABLE_PAYMENT_GATEWAY = "enable_payment_gateway"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAiRecommendationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLE_AI_RECOMMENDATIONS, true)
    }

    fun setAiRecommendationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLE_AI_RECOMMENDATIONS, enabled).apply()
    }

    fun isInstantUrgentNotifsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLE_INSTANT_URGENT_NOTIFS, true)
    }

    fun isAdvancedFiltersEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLE_ADVANCED_FILTERS, true)
    }

    fun isPaymentGatewayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLE_PAYMENT_GATEWAY, true)
    }
}
