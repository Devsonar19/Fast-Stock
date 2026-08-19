package org.example.project

sealed class StockUiState {
    object Idle : StockUiState()
    object Loading : StockUiState()
    data class Success(val stock: Stock) : StockUiState()
    data class Error(val message: String) : StockUiState()
}