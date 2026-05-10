package com.example.pr_6_2.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pr_6_2.domain.model.NobelLaureate
import com.example.pr_6_2.domain.usecase.GetLaureatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NobelListUiState {
    data object Loading : NobelListUiState
    data class Success(val laureates: List<NobelLaureate>) : NobelListUiState
    data class Error(val message: String) : NobelListUiState
}

class NobelListViewModel(
    private val getLaureatesUseCase: GetLaureatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NobelListUiState>(NobelListUiState.Loading)
    val uiState: StateFlow<NobelListUiState> = _uiState.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadLaureates()
    }

    fun loadLaureates() {
        viewModelScope.launch {
            _uiState.value = NobelListUiState.Loading
            try {
                val laureates = getLaureatesUseCase(
                    year = _selectedYear.value,
                    category = _selectedCategory.value
                )
                _uiState.value = NobelListUiState.Success(laureates)
            } catch (e: Exception) {
                _uiState.value = NobelListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setYear(year: Int?) {
        _selectedYear.value = year
        loadLaureates()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        loadLaureates()
    }
}
