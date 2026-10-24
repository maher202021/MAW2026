package com.serviseyem.data.repositories

import com.serviseyem.utils.*

import android.content.Context
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class RealtimeSyncEvent {
    data class NewItemAdded(val title: String, val category: String) : RealtimeSyncEvent()
}

class RealtimeSyncRepository(context: Context? = null) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun observeProvidersRealtime(onNewItemDetected: ((String) -> Unit)? = null): Flow<List<ProviderEntity>> = callbackFlow {
        var previousCount = -1
        val listener: ListenerRegistration = firestore.collection("providers")
            .whereEqualTo("isDeleted", false)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(ProviderEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                if (previousCount in 0 until list.size) {
                    val newest = list.maxByOrNull { it.createdAt }
                    if (newest != null) {
                        onNewItemDetected?.invoke("فني جديد: ${newest.name}")
                    }
                }
                previousCount = list.size

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observeStoresRealtime(onNewItemDetected: ((String) -> Unit)? = null): Flow<List<StoreEntity>> = callbackFlow {
        var previousCount = -1
        val listener: ListenerRegistration = firestore.collection("stores")
            .whereEqualTo("isDeleted", false)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(StoreEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                if (previousCount in 0 until list.size) {
                    val newest = list.maxByOrNull { it.createdAt }
                    if (newest != null) {
                        val isRest = newest.sectionId.contains("restaurant", ignoreCase = true) ||
                                newest.name.contains("مطعم") || newest.name.contains("كافيه")
                        val label = if (isRest) "مطعم جديد: ${newest.name}" else "محل جديد: ${newest.name}"
                        onNewItemDetected?.invoke(label)
                    }
                }
                previousCount = list.size

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observePropertiesRealtime(onNewItemDetected: ((String) -> Unit)? = null): Flow<List<PropertyEntity>> = callbackFlow {
        var previousCount = -1
        val listener: ListenerRegistration = firestore.collection("properties")
            .whereEqualTo("isDeleted", false)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(PropertyEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                if (previousCount in 0 until list.size) {
                    val newest = list.maxByOrNull { it.createdAt }
                    if (newest != null) {
                        onNewItemDetected?.invoke("عقار جديد: ${newest.title}")
                    }
                }
                previousCount = list.size

                trySend(list)
            }

        awaitClose { listener.remove() }
    }
}
