package com.serviseyem.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.helpers.AppState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val appState: AppState
) {
    private var storesListener: ListenerRegistration? = null

    fun startSyncingStores() {
        if (storesListener != null) return

        storesListener = db.collection("stores")
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("StoreRepository", "Error syncing stores: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val stores = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(StoreEntity::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }.let { stores ->
                        
                        appState._stores.value = stores
                        android.util.Log.d("StoreRepository", "Synced ${stores.size} stores")
                    }
                }
            }
    }

    fun stopSyncing() {
        storesListener?.remove()
        storesListener = null
    }
}
