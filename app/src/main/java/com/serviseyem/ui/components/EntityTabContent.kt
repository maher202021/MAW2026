package com.serviseyem.ui.components

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.EntityType
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.entities.*
import com.serviseyem.utils.VisualThemePalette

@Composable
fun EntityTabContent(
    entityType: EntityType,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onEntityClick: ((Any) -> Unit)? = null,
    onChatClick: (() -> Unit)? = null,
    onRequestActionClick: (() -> Unit)? = null
) {
    when (entityType) {
        EntityType.STORE, EntityType.TECHNICIAN -> {
            StoresScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onStoreClick = { store ->
                    onEntityClick?.invoke(store)
                },
                onChatClick = { store ->
                    if (onChatClick != null) {
                        onChatClick()
                    } else {
                        viewModel.openDirectChat(
                            targetUserId = store.id.ifBlank { store.phone },
                            targetUserName = store.name,
                            targetUserPhoto = store.logoImage.ifBlank { store.coverImage },
                            relatedEntityId = store.id,
                            relatedEntityType = "STORE"
                        )
                    }
                },
                onRequestServiceClick = {
                    onRequestActionClick?.invoke()
                }
            )
        }
        EntityType.PROPERTY, EntityType.REAL_ESTATE -> {
            PropertiesScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onPropertyClick = { prop ->
                    onEntityClick?.invoke(prop)
                },
                onChatClick = { prop ->
                    if (onChatClick != null) {
                        onChatClick()
                    } else {
                        viewModel.openDirectChat(
                            targetUserId = prop.id.ifBlank { prop.phone },
                            targetUserName = prop.title.ifBlank { prop.ownerName },
                            targetUserPhoto = prop.images.firstOrNull() ?: "",
                            relatedEntityId = prop.id,
                            relatedEntityType = "PROPERTY"
                        )
                    }
                },
                onRequestInspectionClick = {
                    onRequestActionClick?.invoke()
                }
            )
        }
        EntityType.RESTAURANT -> {
            RestaurantsScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onRestaurantClick = { res ->
                    onEntityClick?.invoke(res)
                },
                onChatClick = { res ->
                    if (onChatClick != null) {
                        onChatClick()
                    } else {
                        viewModel.openDirectChat(
                            targetUserId = res.id.ifBlank { res.phone },
                            targetUserName = res.name,
                            targetUserPhoto = res.logoImage.ifBlank { res.coverImage },
                            relatedEntityId = res.id,
                            relatedEntityType = "RESTAURANT"
                        )
                    }
                },
                onOrderMealClick = {
                    onRequestActionClick?.invoke()
                }
            )
        }
        EntityType.MEDICAL -> {
            MedicalCentersScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onMedicalCenterClick = { med ->
                    onEntityClick?.invoke(med)
                },
                onChatClick = { med ->
                    if (onChatClick != null) {
                        onChatClick()
                    } else {
                        viewModel.openDirectChat(
                            targetUserId = med.id.ifBlank { med.phone },
                            targetUserName = med.name,
                            targetUserPhoto = med.profileImage,
                            relatedEntityId = med.id,
                            relatedEntityType = "MEDICAL"
                        )
                    }
                },
                onBookAppointmentClick = {
                    onRequestActionClick?.invoke()
                }
            )
        }
        EntityType.JOB, EntityType.JOB_POSTER -> {
            StoresScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onStoreClick = { store -> onEntityClick?.invoke(store) },
                onChatClick = { store ->
                    if (onChatClick != null) {
                        onChatClick()
                    } else {
                        viewModel.openDirectChat(
                            targetUserId = store.id.ifBlank { store.phone },
                            targetUserName = store.name,
                            targetUserPhoto = store.logoImage.ifBlank { store.coverImage },
                            relatedEntityId = store.id,
                            relatedEntityType = "JOB"
                        )
                    }
                },
                onRequestServiceClick = { onRequestActionClick?.invoke() }
            )
        }
    }
}
