package org.example.project
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FinnhubQuoteResponse(
    @SerialName("c") val currentPrice: Double,
    @SerialName("dp") val percentageChange: Double,
    @SerialName("h") val highPrice: Double,
    @SerialName("l") val lowPrice: Double,
    @SerialName("pc") val previousClose: Double
)
@Serializable
data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val percentageChange: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val previousClose: Double
)