package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val percentageChange: Double
)