package com.serviseyem.ui

import androidx.lifecycle.viewModelScope
import com.serviseyem.data.StoreEntity
import kotlinx.coroutines.launch
import java.util.UUID

fun MainViewModel.updateStoreEntity(store: StoreEntity) {
    viewModelScope.launch { adminViewModel.crud.saveEntity("stores", store.id, store) }
    triggerNotification("✅ تم تحديث بيانات المتجر بنجاح")
}

fun MainViewModel.toggleBlockStore(storeId: String) {
    val store = _stores.value.find { it.id == storeId }
    if (store != null) {
        adminViewModel.toggleStoreBlocked(storeId, !store.isBlocked)
    } else if (storeId.isNotBlank()) {
        db.collection("stores").document(storeId).get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val currentlyBlocked = doc.getBoolean("isBlocked") == true || doc.getBoolean("blocked") == true
                adminViewModel.toggleStoreBlocked(storeId, !currentlyBlocked)
            }
        }
    }
}

fun MainViewModel.addNewStore(
    name: String,
    phone: String,
    cityId: String,
    localNeighborhood: String,
    categoryId: String,
    coverImage: String,
    workingHours: String
) {
    val newStore = StoreEntity(
        id = UUID.randomUUID().toString(),
        name = name,
        phone = phone,
        cityId = cityId,
        localNeighborhood = localNeighborhood,
        categoryId = categoryId,
        coverImage = coverImage,
        workingHours = workingHours,
        isApproved = true,
        createdAt = System.currentTimeMillis()
    )
    adminViewModel.saveStore(newStore)
}

fun MainViewModel.setStoreBlocked(storeId: String, isBlocked: Boolean, reason: String = "") =
    adminViewModel.setStoreBlocked(storeId, isBlocked, reason)
