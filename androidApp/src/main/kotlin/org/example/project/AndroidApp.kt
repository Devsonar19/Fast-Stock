package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class Screen {
    Welcome,
    Search
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Welcome) }

    MaterialTheme {
        when (currentScreen) {
            Screen.Welcome -> {
                WelcomeScreen(
                    onTimeout = { currentScreen = Screen.Search }
                )
            }
            Screen.Search -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "FAST STOCK",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    // Make sure your existing StockSearchScreen accepts the modifier
                    StockSearchScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onTimeout: () -> Unit) {
    // LaunchedEffect runs as soon as the screen is displayed
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF121212), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(60.dp)
                )
            }
            Text(
                text = "FAST STOCK",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Live Market Insights",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun StockSearchScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf<StockUiState>(StockUiState.Idle) }

    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { StockApiClient() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search Input with Icons
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.uppercase() },
            placeholder = { Text("Search ticker (e.g., AAPL)") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color.Black,
                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Action Button
        Button(
            onClick = {
                coroutineScope.launch {
                    uiState = StockUiState.Loading
                    try {
                        val stock = apiClient.fetchStock(searchQuery)
                        uiState = StockUiState.Success(stock)
                    } catch (e: Exception) {
                        val errorMessage = when (e) {
                            is IOException -> "No internet connection. Please check your network."
                            else -> e.message ?: "An unexpected error occurred."
                        }
                        uiState = StockUiState.Error(errorMessage)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            enabled = searchQuery.isNotBlank() && uiState !is StockUiState.Loading
        ) {
            Text(
                text = if (uiState is StockUiState.Loading) "SEARCHING..." else "SEARCH",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Dynamic State Rendering
        when (val state = uiState) {
            is StockUiState.Idle -> {
                Column(
                    modifier = Modifier.padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Ready to Explore?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter a ticker symbol above to see live market data and fundamentals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            is StockUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }
            is StockUiState.Success -> {
                var currentStock by remember(state.stock.symbol) { mutableStateOf(state.stock) }

                LaunchedEffect(currentStock.symbol) {
                    try {
                        apiClient.observePriceUpdates(currentStock.symbol).collect { livePrice ->
                            val safeLivePrice = livePrice ?: currentStock.currentPrice ?: 0.0
                            val safePrevClose = currentStock.previousClose ?: 0.0

                            val priceDiff = safeLivePrice - safePrevClose

                            val newChange = if (safePrevClose != 0.0) (priceDiff / safePrevClose) * 100 else 0.0
                            val roundedChange = kotlin.math.round(newChange * 100) / 100.0

                            val newChartPoints = currentStock.chartDataPoints.toMutableList()
                            newChartPoints.add(safeLivePrice.toFloat())

                            if (newChartPoints.size > 30) {
                                newChartPoints.removeAt(0)
                            }

                            currentStock = currentStock.copy(
                                currentPrice = livePrice,
                                percentageChange = roundedChange,
                                chartDataPoints = newChartPoints
                            )
                        }
                    } catch (e: Exception) {
                        println("WebSocket disconnected or failed: ${e.message}")
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    StockCard(currentStock)
                    Spacer(modifier = Modifier.height(16.dp))
                    FundamentalsCard(currentStock)
                }
            }
            is StockUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StockCard(stock: Stock) {
    val percentageChange = stock.percentageChange ?: 0.0
    val isPositive = percentageChange >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD50000)
    val trendSign = if (isPositive) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Symbol, Company & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = stock.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${stock.currentPrice}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = trendColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "$trendSign$percentageChange%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = trendColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sparkline Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3.5f)
            ) {
                SparklineChart(
                    dataPoints = stock.chartDataPoints,
                    color = trendColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Market Data Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "Prev Close", value = "$${stock.previousClose}")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(label = "Low", value = "$${stock.lowPrice}")
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "High", value = "$${stock.highPrice}")
                    Spacer(modifier = Modifier.height(8.dp))
                    val formattedCap = if (stock.marketCap > 0) "${(stock.marketCap / 1000).toInt()}B" else "N/A"
                    DetailRow(label = "Mkt Cap", value = formattedCap)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Surface(
            color = Color(0xFFF8F8F8),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEEEEEE))
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun FundamentalsCard(stock: Stock) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Fundamentals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First Row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "P/E Ratio", value = if (stock.peRatio > 0) stock.peRatio.toString() else "N/A")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(label = "Div Yield", value = if (stock.dividendYield > 0) "${stock.dividendYield}%" else "N/A")
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Finnhub returns 10-day average volume in millions
                    val formattedVolume = if (stock.volume > 0) "${stock.volume}M" else "N/A"
                    DetailRow(label = "Volume (10d)", value = formattedVolume)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(label = "52W High", value = "$${stock.fiftyTwoWeekHigh}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second Row for 52W Low (spanning the grid gracefully)
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "52W Low", value = "$${stock.fiftyTwoWeekLow}")
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Empty column to keep the grid aligned
                }
            }
        }
    }
}