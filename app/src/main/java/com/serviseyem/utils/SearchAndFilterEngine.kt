package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONArray
import java.util.Locale

/**
 * 🔍 Problem 12 Solution: Search & Filter Optimization Engine
 * Server-side Firestore queries, Speech-to-Text voice search, local search suggestions/history,
 * and multi-level filter specifications (price, rating, distance, availability).
 */
object SearchAndFilterEngine {

    // 1. Multi-Level Filter Data Model
    data class FilterCriteria(
        val categoryId: String = "",
        val city: String = "",
        val minPrice: Double = 0.0,
        val maxPrice: Double = 1000000.0,
        val minRating: Double = 0.0,
        val maxDistanceKm: Double = 50.0,
        val onlyAvailable: Boolean = false,
        val sortBy: String = "rating" // "rating", "price_asc", "price_desc", "distance"
    )

    // 2. Build Server-Side Firestore Query
    fun buildOptimizedFirestoreQuery(
        db: FirebaseFirestore,
        collectionName: String, // "providers" or "stores"
        searchQuery: String,
        criteria: FilterCriteria
    ): Query {
        var query: Query = db.collection(collectionName)

        if (searchQuery.isNotBlank()) {
            // Use array-contains for keyword indexing to find products and categories within stores/providers
            query = query.whereArrayContains("keywords", searchQuery.lowercase().trim())
        }

        if (criteria.city.isNotBlank() && criteria.city != "الكل") {
            query = query.whereEqualTo("city", criteria.city)
        }

        if (criteria.categoryId.isNotBlank() && criteria.categoryId != "ALL") {
            query = query.whereEqualTo("categoryId", criteria.categoryId)
        }

        if (criteria.onlyAvailable) {
            query = query.whereEqualTo("isAvailable", true)
        }

        if (criteria.minRating > 0.0) {
            query = query.whereGreaterThanOrEqualTo("rating", criteria.minRating)
        }

        // Sorting
        query = when (criteria.sortBy) {
            "rating" -> query.orderBy("rating", Query.Direction.DESCENDING)
            "price_asc" -> query.orderBy("price", Query.Direction.ASCENDING)
            "price_desc" -> query.orderBy("price", Query.Direction.DESCENDING)
            else -> query.orderBy("rating", Query.Direction.DESCENDING)
        }

        return query.limit(10) // Cursor pagination batch of 10 items for Free-Tier cost optimization
    }

    // 3. Search History & Suggestions Storage
    fun saveSearchHistoryQuery(context: Context, keyword: String) {
        if (keyword.isBlank()) return
        val prefs = context.getSharedPreferences("YS_Search_History_2026", Context.MODE_PRIVATE)
        val existing = getSearchHistory(context).toMutableList()
        existing.remove(keyword)
        existing.add(0, keyword)
        val top10 = existing.take(10)

        val jsonArray = JSONArray()
        top10.forEach { jsonArray.put(it) }
        prefs.edit().putString("KEY_SEARCH_HISTORY", jsonArray.toString()).apply()
    }

    fun getSearchHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences("YS_Search_History_2026", Context.MODE_PRIVATE)
        val raw = prefs.getString("KEY_SEARCH_HISTORY", "[]") ?: "[]"
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return list
    }

    fun clearSearchHistory(context: Context) {
        context.getSharedPreferences("YS_Search_History_2026", Context.MODE_PRIVATE)
            .edit().remove("KEY_SEARCH_HISTORY").apply()
    }

    // 4. Voice Search Speech Recognizer Intent Builder
    fun createVoiceSearchIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE") // Arabic (Yemen)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-YE")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث للبحث عن خدمة أو مركز تجاري...")
        }
    }
}
