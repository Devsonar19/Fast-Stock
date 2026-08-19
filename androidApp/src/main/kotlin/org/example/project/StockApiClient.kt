package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay

class StockApiClient {
    // 1. Configure the HTTP Client
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Crucial: Ignores extra JSON fields we don't need
            })
        }
    }

    // 2. The function to fetch data
    // The 'suspend' keyword tells Kotlin this function runs in the background (asynchronously)
    suspend fun fetchStock(query: String): Stock {
        // In a real app, you would make an actual network call here:
        // val response = httpClient.get("https://your-stock-api.com/search?q=$query")
        // return response.body()

        // For testing right now, let's simulate a network delay and return mock data
        delay(1500)

        return Stock(
            symbol = query.uppercase(),
            companyName = "Tech Corp Inc.",
            currentPrice = 142.50,
            percentageChange = 1.25
        )
    }
}