package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.SpecialOfferEntity
import com.serviseyem.data.repositories.OffersRepository
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {

    private val _offers = MutableStateFlow<List<SpecialOfferEntity>>(emptyList())
    val offers: StateFlow<List<SpecialOfferEntity>> = _offers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    init {
        loadOffers()
    }

    fun loadOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getOffers().onSuccess {
                _offers.value = it
            }.onFailure {
                _error.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun addOffer(offer: SpecialOfferEntity) {
        viewModelScope.launch {
            repository.addOffer(offer)
                .onSuccess {
                    loadOffers()
                    _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة العرض بنجاح ✅"))
                }
                .onFailure { e ->
                    _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل إضافة العرض"))
                    _error.value = e.localizedMessage ?: "فشل إضافة العرض"
                }
        }
    }

    fun updateOffer(offer: SpecialOfferEntity) {
        viewModelScope.launch {
            repository.updateOffer(offer)
                .onSuccess {
                    loadOffers()
                    _eventFlow.emit(DashboardEvent.ShowToast("تم تحديث العرض بنجاح ✅"))
                }
                .onFailure { e ->
                    _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل تحديث العرض"))
                    _error.value = e.localizedMessage ?: "فشل تحديث العرض"
                }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            repository.deleteOffer(offerId)
                .onSuccess {
                    loadOffers()
                    _eventFlow.emit(DashboardEvent.ShowToast("تم حذف العرض بنجاح 🗑️"))
                }
                .onFailure { e ->
                    _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل حذف العرض"))
                    _error.value = e.localizedMessage ?: "فشل حذف العرض"
                }
        }
    }
}
