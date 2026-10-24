package com.serviseyem.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.helpers.AppState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PropertyManagementViewModel @Inject constructor(
    val appState: AppState,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    val crud = AdminCrudOperations(db)

    val _properties: MutableStateFlow<List<PropertyEntity>> get() = appState._properties
    val properties: StateFlow<List<PropertyEntity>> = _properties.asStateFlow()

    var onTriggerNotification: ((String) -> Unit)? = null

    fun saveProperty(property: PropertyEntity) {
        viewModelScope.launch {
            val targetId = property.id.ifBlank { db.collection("properties").document().id }
            val resolvedImages = property.images.mapIndexed { idx, img ->
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                    img,
                    com.serviseyem.utils.FirebaseStorageUploader.getPropertyPhotoPath(targetId, idx),
                    maxSizeBytes = 250 * 1024L
                )
            }
            val resolvedPdf = if (property.pdfFileBase64.isNotBlank()) {
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                    property.pdfFileBase64,
                    "properties/$targetId/brochure.pdf"
                )
            } else ""
            val cleanProp = property.copy(
                id = targetId,
                images = resolvedImages,
                pdfFileBase64 = resolvedPdf
            )
            crud.saveEntity("properties", targetId, cleanProp,
                onSuccess = { onTriggerNotification?.invoke("✅ تم حفظ العقار بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل حفظ العقار: ${it.message}") }
            )
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            crud.deleteEntity("properties", propertyId, softDelete = true,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم نقل العقار للمهملات") },
                onError = { onTriggerNotification?.invoke("❌ فشل الحذف: ${it.message}") }
            )
        }
    }

    fun restoreProperty(propertyId: String) {
        viewModelScope.launch {
            crud.updateFields("properties", propertyId, mapOf("isDeleted" to false, "deletedAt" to null),
                onSuccess = { onTriggerNotification?.invoke("♻️ تم استعادة العقار بنجاح") }
            )
        }
    }

    fun deletePropertyPermanently(propertyId: String) {
        viewModelScope.launch {
            crud.deleteEntity("properties", propertyId, softDelete = false,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم حذف العقار نهائياً") }
            )
        }
    }

    fun setPropertyActive(propertyId: String, isActive: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isActive", isActive)
        }
    }

    fun setPropertyPinned(propertyId: String, isPinned: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isPinned", isPinned)
        }
    }

    fun setPropertyVip(propertyId: String, isVip: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isVip", isVip)
        }
    }

    fun setPropertyVerified(propertyId: String, isVerified: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isVerified", isVerified)
        }
    }

    fun setPropertyRecommended(propertyId: String, isRecommended: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isRecommended", isRecommended)
        }
    }

    fun setPropertyBlocked(propertyId: String, isBlocked: Boolean, reason: String = "") {
        viewModelScope.launch {
            crud.updateFields("properties", propertyId, mapOf("isBlocked" to isBlocked, "blockReason" to reason))
        }
    }

    fun setPropertyChatDisabled(propertyId: String, isDisabled: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isChatDisabled", isDisabled)
        }
    }

    fun setPropertyNotificationsDisabled(propertyId: String, isDisabled: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isNotificationsDisabled", isDisabled)
        }
    }

    fun setPropertyPaymentEnabled(propertyId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isPaymentEnabled", isEnabled)
        }
    }

    fun approvePropertyPdf(propertyId: String, approve: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("properties", propertyId, "isPdfApproved", approve)
        }
    }
}
