
package com.ferhatozcelik.jetpackcomposetemplate.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferhatozcelik.jetpackcomposetemplate.data.repository.GoldRepository
import com.ferhatozcelik.jetpackcomposetemplate.data.repository.MultiTimeframeCandles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GoldUiState {
    object Idle : GoldUiState
    object Loading : GoldUiState
    data class Success(val data: MultiTimeframeCandles) : GoldUiState
    data class Error(val message: String) : GoldUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goldRepository: GoldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoldUiState>(GoldUiState.Idle)
    val uiState: StateFlow<GoldUiState> = _uiState.asStateFlow()

    fun analyzeGoldMarket() {
        viewModelScope.launch {
            _uiState.value = GoldUiState.Loading
            try {
                val candleData = goldRepository.fetchGoldCandles()
                _uiState.value = GoldUiState.Success(candleData)
            } catch (e: Exception) {
                _uiState.value = GoldUiState.Error(e.message ?: "Failed to fetch market data")
            }
        }
    }
}
