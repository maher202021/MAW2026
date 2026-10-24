package com.serviseyem.ui.screens.bookings

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class InstantRequestUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val showCreateDialog: Boolean = false,
    val selectedRequestForOffers: InstantRequestEntity? = null,
    val selectedRequestForSubmitOffer: InstantRequestEntity? = null,
    val offerSortType: String = "PRICE", // PRICE, RATING, TIME
    val complaintTarget: String? = null // For Complaint dialog
)

@HiltViewModel
class InstantRequestScreenViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(InstantRequestUiState())
    val uiState: StateFlow<InstantRequestUiState> = _uiState.asStateFlow()

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setShowCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show) }
    }

    fun setSelectedRequestForOffers(request: InstantRequestEntity?) {
        _uiState.update { it.copy(selectedRequestForOffers = request) }
    }

    fun setSelectedRequestForSubmitOffer(request: InstantRequestEntity?) {
        _uiState.update { it.copy(selectedRequestForSubmitOffer = request) }
    }
    
    fun setOfferSortType(type: String) {
        _uiState.update { it.copy(offerSortType = type) }
    }

    fun setComplaintTarget(targetId: String?) {
        _uiState.update { it.copy(complaintTarget = targetId) }
    }

    fun sortOffers(offers: List<RequestOfferEntity>): List<RequestOfferEntity> {
        return when (_uiState.value.offerSortType) {
            "PRICE" -> offers.sortedBy { it.price }
            "RATING" -> offers.sortedByDescending { it.technicianRating }
            "TIME" -> offers.sortedBy { it.distanceKm }
            else -> offers.sortedBy { it.price }
        }
    }
}
