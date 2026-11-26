package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.data.repositories.IRatingsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.catch

class StoreDashboardViewModel(
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

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadDashboardData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            supervisorScope {
                launch {
                    dashboardRepository.getDashboardStats(ownerId, "STORE")
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

    fun addProduct(title: String, priceYer: Double, description: String = "", imageUrl: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                _eventFlow.emit(DashboardEvent.ShowToast("يرجى إدخال اسم المنتج"))
                return@launch
            }
            val newProduct = ProductItemEntity(
                ownerId = ownerId,
                title = title,
                priceYer = priceYer,
                description = description,
                imageUrl = imageUrl,
                category = "STORE"
            )
            val res = productsRepository.addProduct(newProduct)
            res.onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة المنتج بنجاح 📦"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء حفظ المنتج"))
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            productsRepository.deleteProduct(id).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف المنتج 🗑️"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل حذف المنتج"))
            }
        }
    }

    fun updateProduct(product: ProductItemEntity) {
        viewModelScope.launch {
            productsRepository.updateProduct(product).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم تحديث المنتج بنجاح ✅"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل تحديث المنتج"))
            }
        }
    }

    fun toggleProductAvailability(product: ProductItemEntity) {
        viewModelScope.launch {
            val next = !product.isAvailable
            productsRepository.updateProduct(product.copy(isAvailable = next)).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast(if (next) "المنتج متاح الآن ✅" else "تم إيقاف توفر المنتج ⏸️"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل تحديث حالة المنتج"))
            }
        }
    }

    fun replyToReview(reviewId: String, replyText: String) {
        viewModelScope.launch {
            ratingsRepository.replyToRating(reviewId, replyText).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم إرسال الرد على التقييم بنجاح 💬"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل إرسال الرد"))
            }
        }
    }

    fun dispose() {
        loadJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        loadJob = null
    }
}
