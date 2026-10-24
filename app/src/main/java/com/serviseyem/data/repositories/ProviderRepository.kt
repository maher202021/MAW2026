package com.serviseyem.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.helpers.AppState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val appState: AppState
) {
    private var providersListener: ListenerRegistration? = null

    /**
     * Starts a real-time listener for service providers.
     * Updates the AppState directly to ensure all screens reflect changes immediately.
     */
    fun startSyncingProviders() {
        if (providersListener != null) return

        providersListener = db.collection("providers")
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ProviderRepository", "Error syncing providers: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val providers = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ProviderEntity::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    appState._providers.value = providers
                    appState._isProvidersLoading.value = false
                    android.util.Log.d("ProviderRepository", "Synced ${providers.size} providers")
                }
            }
    }

    fun stopSyncing() {
        providersListener?.remove()
        providersListener = null
    }

    /**
     * Reactive flow of providers if needed by specific components.
     */
    fun getProvidersFlow(): Flow<List<ProviderEntity>> = callbackFlow {
        val listener = db.collection("providers")
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(ProviderEntity::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
