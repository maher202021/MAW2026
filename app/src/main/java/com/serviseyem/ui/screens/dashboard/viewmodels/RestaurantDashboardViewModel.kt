package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.data.repositories.IRatingsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.catch

class RestaurantDashboardViewModel(
    private val ownerId: String,
    private val dashboardRepository: IDashboardRepository,
    private val productsRepository: IProductsRepository,
    private val ratingsRepository: IRatingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            supervisorScope {
                launch {
                    dashboardRepository.getDashboardStats(ownerId, "RESTAURANT")
                        .catch { e -> 
                            _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في الإحصائيات")) 
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        .collect { stats ->
                            _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
                        }
                }
                launch {
                    productsRepository.getOwnerProducts(ownerId)
                        .catch { e -> _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في المنتجات")) }
                        .collect { prods ->
                            _uiState.value = _uiState.value.copy(products = prods)
                        }
                }
                launch {
                    ratingsRepository.getTargetRatings(ownerId)
                        .catch { e -> _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في التقييمات")) }
                        .collect { revs ->
                            _uiState.value = _uiState.value.copy(reviews = revs)
                        }
                }
            }
        }
    }

    fun addMeal(title: String, priceYer: Double, description: String = "", imageUrl: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                _eventFlow.emit(DashboardEvent.ShowToast("يرجى إدخال اسم الوجبة"))
                return@launch
            }
            val meal = ProductItemEntity(
                ownerId = ownerId,
                title = title,
                priceYer = priceYer,
                description = description,
                imageUrl = imageUrl,
                category = "RESTAURANT"
            )
            productsRepository.addProduct(meal).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة الوجبة لقائمة الطعام 🍽️"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل إضافة الوجبة"))
            }
        }
    }

    fun deleteMeal(id: String) {
        viewModelScope.launch {
            productsRepository.deleteProduct(id).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف الوجبة 🗑️"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل حذف الوجبة"))
            }
        }
    }
}
