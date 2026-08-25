package com.ferhatozcelik.jetpackcomposetemplate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketViewModel(
    private val repository: MarketRepository = MarketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketAnalysisState())
    val uiState: StateFlow<MarketAnalysisState> = _uiState.asStateFlow()

    /**
     * Call this whenever new Binance candle data is fetched or updated.
     */
    fun onNewCandlesReceived(candles: List<Candle>) {
        viewModelScope.launch {
            val updatedState = repository.processCandles(candles)
            _uiState.value = updatedState
        }
    }
}
