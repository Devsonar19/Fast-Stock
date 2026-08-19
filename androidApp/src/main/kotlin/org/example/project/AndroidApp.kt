package org.example.project

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            onValueChange = { searchQuery = it.uppercase() }, // Auto-capitalize
            label = { Text("Enter Ticker") },
            singleLine = true, // Prevents the box from expanding vertically
            shape = RoundedCornerShape(12.dp), // Matches the card
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
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
    // Determine if the stock is up or down for a subtle indicator
    val isPositive = stock.percentageChange >= 0
    val trendIcon = if (isPositive) "▲" else "▼"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(24.dp)
    ) {
        // Header: Symbol and Current Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "$${stock.currentPrice}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-header: Company Name and Percentage Change
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stock.companyName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = "$trendIcon ${kotlin.math.abs(stock.percentageChange)}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black // Keeping it monochrome rather than green/red
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Detail Grid: High, Low, Previous Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "High", value = "$${stock.highPrice}")
            DetailItem(label = "Low", value = "$${stock.lowPrice}")
            DetailItem(label = "Prev Close", value = "$${stock.previousClose}")
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}