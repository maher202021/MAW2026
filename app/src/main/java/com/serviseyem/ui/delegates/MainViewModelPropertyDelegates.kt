package com.serviseyem.ui

import androidx.lifecycle.viewModelScope
import com.serviseyem.data.PropertyEntity
import kotlinx.coroutines.launch

fun MainViewModel.updatePropertyEntity(property: PropertyEntity) {
    viewModelScope.launch { adminViewModel.crud.saveEntity("properties", property.id, property) }
    triggerNotification("✅ تم تحديث بيانات العقار بنجاح")
}

fun MainViewModel.setPropertyBlocked(propertyId: String, isBlocked: Boolean, reason: String = "") =
    adminViewModel.setPropertyBlocked(propertyId, isBlocked, reason)

fun MainViewModel.toggleBlockProperty(propertyId: String) {
    val prop = _properties.value.find { it.id == propertyId }
    if (prop != null) {
        adminViewModel.setPropertyBlocked(propertyId, !prop.isBlocked)
    } else if (propertyId.isNotBlank()) {
        db.collection("properties").document(propertyId).get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val currentlyBlocked = doc.getBoolean("isBlocked") == true || doc.getBoolean("blocked") == true
                adminViewModel.setPropertyBlocked(propertyId, !currentlyBlocked)
            }
        }
    }
}
