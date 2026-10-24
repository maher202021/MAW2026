package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IGalleryRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.data.repositories.IRatingsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.catch

class TechnicianDashboardViewModel(
    private val ownerId: String,
    private val dashboardRepository: IDashboardRepository,
    private val productsRepository: IProductsRepository,
    private val ratingsRepository: IRatingsRepository,
    private val galleryRepository: IGalleryRepository
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
                    dashboardRepository.getDashboardStats(ownerId, "PROVIDER")
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
                launch {
                    galleryRepository.getOwnerGallery(ownerId)
                        .catch { e -> _eventFlow.emit(DashboardEvent.ShowToast(e.message ?: "خطأ في المعرض")) }
                        .collect { albums ->
                            _uiState.value = _uiState.value.copy(galleryAlbums = albums)
                        }
                }
            }
        }
    }

    fun addNewProductService(title: String, priceYer: Double, description: String = "", imageUrl: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                _eventFlow.emit(DashboardEvent.ShowToast("يرجى إدخال اسم الخدمة"))
                return@launch
            }
            val newProduct = ProductItemEntity(
                ownerId = ownerId,
                title = title,
                priceYer = priceYer,
                description = description,
                imageUrl = imageUrl,
                category = "TECHNICIAN"
            )
            val res = productsRepository.addProduct(newProduct)
            res.onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تمت إضافة الخدمة بنجاح ✅"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء الإضافة"))
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            productsRepository.deleteProduct(id).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف الخدمة بنجاح 🗑️"))
            }.onFailure { e ->
                _eventFlow.emit(DashboardEvent.ShowToast(e.localizedMessage ?: "فشل حذف الخدمة"))
            }
        }
    }

    fun saveGalleryAlbum(context: android.content.Context, localUriStr: String) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            _eventFlow.emit(DashboardEvent.ShowToast("جاري رفع الصورة... ⏳"))
            val uri = android.net.Uri.parse(localUriStr)
            val uploadResult = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(appContext, uri, "galleries/${ownerId}")
            
            uploadResult.onSuccess { remoteUrl ->
                val album = com.serviseyem.domain.entities.GalleryAlbumEntity(
                    ownerId = ownerId,
                    title = "أعمال سابقة",
                    imageUrls = listOf(remoteUrl)
                )
                galleryRepository.saveGalleryAlbum(album).onSuccess {
                    _eventFlow.emit(DashboardEvent.ShowToast("تم حفظ الصورة بنجاح ✅"))
                }.onFailure {
                    _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء حفظ الصورة"))
                }
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("فشل رفع الصورة ❌"))
            }
        }
    }

    fun deleteGalleryAlbum(albumId: String) {
        viewModelScope.launch {
            galleryRepository.deleteGalleryAlbum(albumId).onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast("تم حذف الصورة بنجاح 🗑️"))
            }.onFailure {
                _eventFlow.emit(DashboardEvent.ShowToast("حدث خطأ أثناء الحذف"))
            }
        }
    }
}
