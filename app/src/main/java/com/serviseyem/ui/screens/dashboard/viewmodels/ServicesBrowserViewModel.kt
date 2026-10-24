package com.serviseyem.ui.screens.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.domain.entities.ProductItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServicesBrowserUiState(
    val isLoading: Boolean = true,
    val products: List<ProductItemEntity> = emptyList(),
    val filteredProducts: List<ProductItemEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCity: String = "الكل",
    val selectedCategory: String = "الكل"
)

/**
 * 🧠 ServicesBrowserViewModel - إدارة منطق وتصفح خدمات التطبيق والمنتجات
 */
@HiltViewModel
class ServicesBrowserViewModel @Inject constructor(
    private val productsRepository: IProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesBrowserUiState())
    val uiState: StateFlow<ServicesBrowserUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            productsRepository.getAllAvailableProducts()
                .catch { /* silent log */ }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = list,
                        filteredProducts = filterList(list, _uiState.value.searchQuery, _uiState.value.selectedCategory)
                    )
                }
        }
    }

    fun updateSearchQuery(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            val state = _uiState.value
            _uiState.value = state.copy(
                searchQuery = query,
                filteredProducts = filterList(state.products, query, state.selectedCategory)
            )
        }
    }

    fun selectCategory(category: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedCategory = category,
            filteredProducts = filterList(state.products, state.searchQuery, category)
        )
    }

    private fun filterList(
        list: List<ProductItemEntity>,
        query: String,
        category: String
    ): List<ProductItemEntity> {
        return list.filter { item ->
            val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "الكل" || item.category == category
            matchesQuery && matchesCategory
        }
    }
}
