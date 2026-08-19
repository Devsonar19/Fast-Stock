package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    MaterialTheme {
        StockSearchScreen()
    }
}

@Composable
fun StockSearchScreen() {
    // 1. State Variables
    var searchQuery by remember { mutableStateOf("") }
    var stockResult by remember { mutableStateOf<Stock?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 2. Tools we need for background work and networking
    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { StockApiClient() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. The Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Enter Stock Ticker (e.g., AAPL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. The Search Button
        Button(
            onClick = {
                // We launch a coroutine to call our 'suspend' function without freezing the app
                coroutineScope.launch {
                    isLoading = true
                    try {
                        stockResult = apiClient.fetchStock(searchQuery)
                    } catch (e: Exception) {
                        println("Error fetching data: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotBlank() && !isLoading
        ) {
            Text(if (isLoading) "Searching..." else "Search")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Display the Result
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            stockResult?.let { stock ->
                StockCard(stock)
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