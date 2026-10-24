package com.serviseyem.data.repositories

import androidx.annotation.Keep
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * ⚠️ Sensitive keys (banking, payment) must be stored in Cloud Functions Secrets, not Firestore.
 * This unified repository abstracts API key management and allows transitioning
 * to secure serverless secret storage seamlessly in the future.
 * TODO: نقل جميع المفاتيح الحساسة وبوابات الدفع إلى خوادم Cloud Functions و Google Cloud Secret Manager بالكامل
 */
@Keep
data class ApiKeysEntity(
    val geminiApiKey: String = "",
    val openaiApiKey: String = "",
    val selectedAiModel: String = "gemini-1.5-flash",
    val googleMapsKey: String = "",
    val mapboxKey: String = "",
    val selectedMapEngine: String = "OPEN_STREET_MAP",
    val kuraimiToken: String = "",
    val jawwalPayKey: String = "",
    val floosakKey: String = "",
    val oneCashKey: String = "",
    val webhookUrl: String = "",
    val whatsappToken: String = "",
    val smsGatewayKey: String = "",
    val customKeys: List<Map<String, String>> = emptyList(),
    val updatedAt: Long = 0L
)

interface IApiKeyRepository {
    suspend fun getApiKey(keyName: String): Result<String>
    suspend fun setApiKey(keyName: String, value: String): Result<Unit>
}

class ApiKeyRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IApiKeyRepository {

    /**
     * 🔑 جلب مفتاح API عبر Cloud Function الآمن
     */
    override suspend fun getApiKey(keyName: String): Result<String> {
        val cleanName = keyName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Key name must not be blank"))
        }
        return try {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val result = functions
                .getHttpsCallable("getApiKey")
                .call(mapOf("keyName" to cleanName))
                .await()

            val data = result.data as? Map<*, *>
            val value = data?.get("value") as? String

            if (value != null && value.isNotBlank()) {
                Result.success(value)
            } else {
                Result.failure(Exception("Key not found: $cleanName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 💾 حفظ مفتاح API عبر Cloud Function الآمن في Secret Manager
     */
    override suspend fun setApiKey(keyName: String, value: String): Result<Unit> {
        val cleanName = keyName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Key name must not be blank"))
        }
        return try {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            functions
                .getHttpsCallable("setApiKey")
                .call(mapOf(
                    "keyName" to cleanName,
                    "value" to value
                ))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
