package com.serviseyem.data.repositories

import androidx.annotation.Keep
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Keep
data class QuickServiceSettingsEntity(
    val isEnabled: Boolean = true,
    val maxRequestsPerDay: Int = 10,
    val offerDeadlineHours: Int = 24,
    val maxOffersPerRequest: Int = 5,
    val allowedCities: List<String> = emptyList(),
    val allowedCategories: List<String> = emptyList(),
    val requireApproval: Boolean = false,
    val autoAssignNearest: Boolean = false,
    val showProviderRating: Boolean = true,
    val showExpectedPrice: Boolean = true,
    val allowImages: Boolean = true,
    val allowAudio: Boolean = true,
    val maxImages: Int = 5,
    val maxAudioDuration: Int = 60,
    val customFields: List<Map<String, String>> = emptyList() // Custom dynamic form fields configured by Admin
)

@Keep
class QuickServiceAdminManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val settingsDocRef = firestore.collection("quick_service_settings").document("main_settings")

    /**
     * Get real-time settings flow from Firestore to enable/disable features or fields dynamically
     */
    fun getSettingsFlow(): Flow<QuickServiceSettingsEntity> = callbackFlow {
        val listener = settingsDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(QuickServiceSettingsEntity()) // Return default settings on failure
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val settings = snapshot.toObject(QuickServiceSettingsEntity::class.java) ?: QuickServiceSettingsEntity()
                trySend(settings)
            } else {
                trySend(QuickServiceSettingsEntity())
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Save/update dynamic settings in Firestore from the Admin panel
     */
    fun updateSettings(
        settings: QuickServiceSettingsEntity,
        onResult: (Boolean, String?) -> Unit
    ) {
        settingsDocRef.set(settings)
            .addOnSuccessListener {
                onResult(true, "تم حفظ إعدادات الخدمة العاجلة بنجاح!")
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }
}
