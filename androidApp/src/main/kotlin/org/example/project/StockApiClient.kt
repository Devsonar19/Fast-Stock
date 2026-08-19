package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class StockApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
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

        if (quote.currentPrice == 0.0) throw Exception("Ticker '$symbol' not found.")

        val mockChartPoints = listOf(
            quote.openPrice.toFloat(),
            quote.lowPrice.toFloat(),
            ((quote.highPrice + quote.lowPrice) / 2).toFloat(),
            quote.highPrice.toFloat(),
            quote.currentPrice.toFloat()
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
            chartDataPoints = mockChartPoints,
        )
    }
}