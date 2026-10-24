package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IFavoritesRepository
import com.serviseyem.domain.entities.FavoriteItemEntity
import com.serviseyem.ui.screens.dashboard.DashboardEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<FavoriteItemEntity> = emptyList(),
    val errorMessage: String? = null
)

/**
 * 🧠 FavoritesViewModel - إدارة تفضيلات المستخدم والمفضلة
 */
class FavoritesViewModel(
    private val userId: String,
    private val favoritesRepository: IFavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DashboardEvent>()
    val eventFlow: SharedFlow<DashboardEvent> = _eventFlow.asSharedFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.getUserFavorites(userId).collect { list ->
                _uiState.value = FavoritesUiState(isLoading = false, favorites = list)
            }
        }
    }

    fun toggleFavorite(item: FavoriteItemEntity) {
        viewModelScope.launch {
            // 1. احفظ الحالة السابقة للـ rollback
            val previousList = _uiState.value.favorites.toList()
            val isFav = previousList.any { it.targetId == item.targetId }

            // 2. Optimistic update — غيّر UI فوراً
            val newList = if (isFav) {
                previousList.filter { it.targetId != item.targetId }
            } else {
                listOf(item.copy(userId = userId)) + previousList
            }
            _uiState.value = _uiState.value.copy(favorites = newList)

            // 3. نفّذ العملية الفعلية
            val result = if (isFav) {
                favoritesRepository.removeFavorite(userId, item.targetId)
            } else {
                favoritesRepository.addFavorite(item.copy(userId = userId))
            }

            // 4. على النجاح → رسالة نجاح
            //    على الفشل → rollback + رسالة خطأ
            result.onSuccess {
                _eventFlow.emit(DashboardEvent.ShowToast(
                    if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة للمفضلة"
                ))
            }.onFailure { e ->
                // rollback
                _uiState.value = _uiState.value.copy(favorites = previousList)
                _eventFlow.emit(DashboardEvent.ShowToast("فشل العملية: ${e.localizedMessage ?: "خطأ غير معروف"}"))
            }
        }
    }
}
