package com.serviseyem.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.helpers.AppState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val appState: AppState
) {
    private var propertiesListener: ListenerRegistration? = null

    fun startSyncingProperties() {
        if (propertiesListener != null) return

        propertiesListener = db.collection("properties")
            .whereEqualTo("isDeleted", false)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("PropertyRepository", "Error syncing properties: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val properties = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(PropertyEntity::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }.filter { it.isApproved }
                    
                    appState._properties.value = properties
                    android.util.Log.d("PropertyRepository", "Synced ${properties.size} properties")
                }
            }
    }

    fun stopSyncing() {
        propertiesListener?.remove()
        propertiesListener = null
    }
}
