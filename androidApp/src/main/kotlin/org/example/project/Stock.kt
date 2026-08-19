package org.example.project
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FinnhubQuoteResponse(
    @SerialName("c") val currentPrice: Double? = 0.0,
    @SerialName("dp") val percentageChange: Double? = 0.0,
    @SerialName("h") val highPrice: Double? = 0.0,
    @SerialName("l") val lowPrice: Double? = 0.0,
    @SerialName("o") val openPrice: Double? = 0.0,
    @SerialName("pc") val previousClose: Double? = 0.0
)
@Serializable
data class FinnhubProfileResponse(
    @SerialName("name") val companyName: String? = "Unknow name",
    @SerialName("marketCapitalization") val marketCap: Double? = 0.0,
    @SerialName("currency") val currency: String? = "USD"
)

@Serializable
data class FinnhubTradeMessage(
    val type: String,
    val data: List<FinnhubTradeData>? = null
)

@Serializable
data class FinnhubTradeData(
    @SerialName("p") val price: Double,
    @SerialName("s") val symbol: String,
    @SerialName("t") val timestamp: Long,
    @SerialName("v") val volume: Double
)

data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double?,
    val percentageChange: Double?,
    val highPrice: Double?,
    val lowPrice: Double?,
    val previousClose: Double?,
    val chartDataPoints: List<Float>,
    val marketCap: Double
)