package com.serviseyem.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.DestinationChooserSheet
import com.serviseyem.ui.screens.map.PlacesDownloadDialog
import com.serviseyem.ui.screens.map.ValhallaOfflineDialog
import com.serviseyem.utils.LandmarkAddress
import com.serviseyem.utils.LandmarkAddressStore
import com.serviseyem.utils.RecentDestinations

@Composable
internal fun NavigationDialogsHost(
    context: Context,
    showDestChooser: Boolean,
    showValhallaDialog: Boolean,
    showPlacesDownload: Boolean,
    userLat: Double,
    userLng: Double,
    providersList: List<ProviderEntity>,
    storesList: List<StoreEntity>,
    savedAddresses: List<LandmarkAddress>,
    addressStore: LandmarkAddressStore,
    onSavedAddressesUpdated: (List<LandmarkAddress>) -> Unit,
    onDismissDestChooser: () -> Unit,
    onPickDestination: (name: String, lat: Double, lng: Double) -> Unit,
    onStartMapPick: () -> Unit,
    onOpenPlacesDownload: () -> Unit,
    onDismissValhallaDialog: () -> Unit,
    onValhallaFilesUpdated: (statusMessage: String?, announce: Boolean) -> Unit,
    onDismissPlacesDownload: () -> Unit,
    onPlacesDownloadCompleted: (String) -> Unit
) {
    if (showDestChooser) {
        DestinationChooserSheet(
            userLat = userLat,
            userLng = userLng,
            hasRealLocation = userLat != 0.0 && userLng != 0.0,
            providers = providersList,
            stores = storesList,
            savedAddresses = savedAddresses,
            onPick = { name, la, lo ->
                RecentDestinations(context).add(name, la, lo)
                onPickDestination(name, la, lo)
            },
            onPickFromMap = onStartMapPick,
            onDownloadPlaces = onOpenPlacesDownload,
            onDismiss = onDismissDestChooser,
            onAddressSaved = {
                onSavedAddressesUpdated(addressStore.all())
            }
        )
    }

    if (showValhallaDialog) {
        ValhallaOfflineDialog(
            onDismiss = onDismissValhallaDialog,
            onStartNavigation = {
                onValhallaFilesUpdated(null, false)
            },
            onOpenPlacesDownload = onOpenPlacesDownload,
            onStatusMessage = { msg ->
                onValhallaFilesUpdated(msg, false)
            }
        )
    }

    if (showPlacesDownload) {
        PlacesDownloadDialog(
            onDismiss = onDismissPlacesDownload,
            onCompleted = onPlacesDownloadCompleted
        )
    }
}
