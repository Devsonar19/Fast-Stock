package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.FinnhubQuoteResponse
import org.example.project.Stock

class StockApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchStock(query: String): Stock {
        // Replace this with the key you got from Finnhub
        val apiKey = "da2pu29r01qmq2qavn30da2pu29r01qmq2qavn3g"
        val url = "https://finnhub.io/api/v1/quote?symbol=${query.uppercase()}&token=$apiKey"

        val response: FinnhubQuoteResponse = httpClient.get(url).body()

        return Stock(
            symbol = query.uppercase(),
            companyName = "Live Market Data",
            currentPrice = response.currentPrice,
            percentageChange = response.percentageChange
        )
    }
}