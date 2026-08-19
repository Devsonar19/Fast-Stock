package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StockApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(WebSockets)
    }

    suspend fun fetchStock(query: String): Stock = coroutineScope{

        val apiKey = "da2pu29r01qmq2qavn30da2pu29r01qmq2qavn3g"
        val url = "https://finnhub.io/api/v1/quote?symbol=${query.uppercase()}&token=$apiKey"
        val symbol = query.uppercase()
        val response: FinnhubQuoteResponse = httpClient.get(url).body()


        if (response.currentPrice == 0.0) {
            throw Exception("Ticker '$query' not found.")
        }


        val quoteRequest = async {
            httpClient.get("https://finnhub.io/api/v1/quote?symbol=$symbol&token=$apiKey").body<FinnhubQuoteResponse>()
        }
        val profileRequest = async {
            httpClient.get("https://finnhub.io/api/v1/stock/profile2?symbol=$symbol&token=$apiKey").body<FinnhubProfileResponse>()
        }

        val quote = quoteRequest.await()
        val profile = profileRequest.await()

        val currentPrice = quote.currentPrice ?: 0.0
        val openPrice = quote.openPrice ?: 0.0
        val lowPrice = quote.lowPrice ?: 0.0
        val highPrice = quote.highPrice ?: 0.0
        val previousClose = quote.previousClose ?: 0.0
        val percentageChange = quote.percentageChange ?: 0.0

        if (quote.currentPrice == 0.0) throw Exception("Ticker '$symbol' not found.")

        val initialChartPoints = listOf(
            openPrice.toFloat(),
            lowPrice.toFloat(),
            ((highPrice + lowPrice) / 2).toFloat(),
            highPrice.toFloat(),
            currentPrice.toFloat()
        )


        Stock(
            symbol = symbol,
            companyName = profile.companyName ?: "Unknown Company",
            currentPrice = response.currentPrice,
            percentageChange = response.percentageChange,
            highPrice = response.highPrice,
            lowPrice = response.lowPrice,
            previousClose = response.previousClose,
            marketCap = profile.marketCap ?: 0.0,
            chartDataPoints = initialChartPoints,
        )
    }


    fun observePriceUpdates(symbol: String): Flow<Double> = flow {
        val apiKey = "da2pu29r01qmq2qavn30da2pu29r01qmq2qavn3g"
        val wsUrl = "wss://ws.finnhub.io?token=$apiKey"
        val jsonParser = Json { ignoreUnknownKeys = true }

        httpClient.wss(urlString = wsUrl) {
            // Tell Finnhub we want to listen to this specific ticker
            val subscribeMessage = """{"type":"subscribe","symbol":"${symbol.uppercase()}"}"""
            send(Frame.Text(subscribeMessage))

            // Continuously listen for incoming messages
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        // Parse the JSON
                        val message = jsonParser.decodeFromString<FinnhubTradeMessage>(text)

                        // If it's a trade message, extract the latest price and emit it
                        if (message.type == "trade" && !message.data.isNullOrEmpty()) {
                            val latestPrice = message.data.last().price
                            emit(latestPrice)
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors for pings/pongs
                    }
                }
            }
        }
    }
}