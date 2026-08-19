package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.ktor.utils.io.errors.IOException

@Composable
fun AndroidApp() {
    MaterialTheme {
        StockSearchScreen()
    }
}

@Composable
fun StockSearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // 1. Single source of truth for our UI state
    var uiState by remember { mutableStateOf<StockUiState>(StockUiState.Idle) }

    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { StockApiClient() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Enter Stock Ticker (e.g., AAPL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    uiState = StockUiState.Loading // Switch to loading spinner

                    try {
                        val stock = apiClient.fetchStock(searchQuery)
                        uiState = StockUiState.Success(stock) // Success! Show the card
                    } catch (e: Exception) {
                        // 2. Catch all errors and extract a user-friendly message
                        val errorMessage = when (e) {
                            is IOException -> "No internet connection. Please check your network."
                            else -> e.message ?: "An unexpected error occurred."
                        }
                        uiState = StockUiState.Error(errorMessage) // Switch to error text
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotBlank() && uiState !is StockUiState.Loading
        ) {
            Text(if (uiState is StockUiState.Loading) "Searching..." else "Search")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Cleanly render the UI based on the current state
        when (val state = uiState) {
            is StockUiState.Idle -> {
                Text("Enter a ticker symbol above to see live prices.", color = Color.Gray)
            }
            is StockUiState.Loading -> {
                CircularProgressIndicator()
            }
            is StockUiState.Success -> {
                StockCard(state.stock)
            }
            is StockUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StockCard(stock: Stock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Symbol: ${stock.symbol}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Company: ${stock.companyName}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Price: $${stock.currentPrice}")
            Text("Change: ${stock.percentageChange}%")
        }
    }
}