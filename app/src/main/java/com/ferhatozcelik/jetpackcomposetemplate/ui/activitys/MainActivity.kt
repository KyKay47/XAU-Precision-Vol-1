@file:OptIn(ExperimentalMaterial3Api::class)

package com.ferhatozcelik.jetpackcomposetemplate.ui.activitys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private const val API_KEY = "1ea0815d07484662b581a62d339707bd"

object SignalCache {
    var cachedResult: AnalysisResult? = null
    var lastFetchTime: Long = 0L
    const val CACHE_DURATION_MS = 180_000L
}

data class Candle(val close: Double, val high: Double, val low: Double)

data class AnalysisResult(
    val currentPrice: Double,
    val bias: String,
    val confluenceRate: String,
    val marketStructure: String,
    val rsiValue: Double,
    val h4Bias: String,
    val h1Bias: String,
    val m15Bias: String,
    val entryLow: Double,
    val entryHigh: Double,
    val stopLoss: Double,
    val tp1: Double,
    val tp2: Double,
    val isCached: Boolean = false
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    MarketStructureDashboard()
                }
            }
        }
    }
}

@Composable
fun MarketStructureDashboard() {
    var analysis by remember { mutableStateOf<AnalysisResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var balanceInput by remember { mutableStateOf("100") }
    var riskPercentInput by remember { mutableStateOf("1") }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun runAnalysis(forceRefresh: Boolean = false) {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            val currentTime = System.currentTimeMillis()
            if (!forceRefresh && SignalCache.cachedResult != null && 
                (currentTime - SignalCache.lastFetchTime) < SignalCache.CACHE_DURATION_MS) {
                analysis = SignalCache.cachedResult?.copy(isCached = true)
                isLoading = false
                return@launch
            }

            val result = fetchAndComputeStructureSignal()
            if (result != null) {
                SignalCache.cachedResult = result
                SignalCache.lastFetchTime = System.currentTimeMillis()
                analysis = result
            } else {
                errorMessage = "API Rate limit reached or connection error. Please wait 1 minute."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        runAnalysis(forceRefresh = false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("XAU PRECISION PRO", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
        Text("Market Structure & Trend Engine v3.1", fontSize = 11.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LIVE XAU/USD SPOT PRICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = analysis?.let { "$${String.format(Locale.US, "%.2f", it.currentPrice)}" } ?: "--.--",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = balanceInput,
                onValueChange = { balanceInput = it },
                label = { Text("Account Balance ($)", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.2f).height(56.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = riskPercentInput,
                onValueChange = { riskPercentInput = it },
                label = { Text("Risk %", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.8f).height(56.dp)
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = Color(0xFFFF5252), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { runAnalysis(forceRefresh = true) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth().height(46.dp),
            enabled = !isLoading
        ) {
            Text("Analyze Market Structure 🔄", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        analysis?.let { data ->
            val isBuy = data.bias.contains("BUY")
            val isNeutral = data.bias.contains("WAIT") || data.bias.contains("NEUTRAL")
            val biasColor = when {
                isBuy -> Color(0xFF4CAF50)
                isNeutral -> Color(0xFFFFC107)
                else -> Color(0xFFFF5252)
            }

            val balance = balanceInput.toDoubleOrNull() ?: 100.0
            val riskPercent = riskPercentInput.toDoubleOrNull() ?: 1.0
            val maxRiskCash = balance * (riskPercent / 100.0)
            
            val slDistance = abs(data.currentPrice - data.stopLoss)
            val riskPerStandardLot = slDistance * 100.0
            val calculatedLotSize = if (riskPerStandardLot > 0) maxRiskCash / riskPerStandardLot else 0.01
            val recommendedLotSize = String.format(Locale.US, "%.2f", calculatedLotSize)
            val isHighRisk = calculatedLotSize < 0.01

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SIGNAL & MARKET STRUCTURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    TradingRow("Signal Bias:", data.bias, biasColor, bold = true)
                    TradingRow("Confluence Rate:", data.confluenceRate, Color(0xFFFFD700), bold = true)
                    TradingRow("Market Structure:", data.marketStructure, getStructureColor(data.marketStructure), bold = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF2C2C2C))

                    Text("TIMEFRAME TREND ALIGNMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    TradingRow("H4 Macro Bias:", data.h4Bias, getBiasColor(data.h4Bias))
                    TradingRow("H1 Intermediate:", data.h1Bias, getBiasColor(data.h1Bias))
                    TradingRow("M15 Execution:", data.m15Bias, getBiasColor(data.m15Bias))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF2C2C2C))

                    Text("RISK MANAGEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    TradingRow(
                        label = "Rec. Lot Size:",
                        value = if (isHighRisk) "0.01 (High Risk)" else "$recommendedLotSize Lots",
                        valueColor = if (isHighRisk) Color(0xFFFF5252) else Color(0xFF4CAF50),
                        bold = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF2C2C2C))

                    Text("TRADE EXECUTION BOUNDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    TradingRow("Entry Zone:", "${fmt(data.entryLow)} - ${fmt(data.entryHigh)}", Color.Cyan, bold = true)
                    TradingRow("Stop Loss (SL):", fmt(data.stopLoss), Color(0xFFFF5252), bold = true)
                    TradingRow("Take Profit 1 (TP1):", fmt(data.tp1), Color(0xFF4CAF50))
                    TradingRow("Take Profit 2 (TP2):", fmt(data.tp2), Color(0xFF4CAF50), bold = true)
                }
            }
        }
    }
}

@Composable
fun TradingRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

fun getStructureColor(str: String): Color = when {
    str.contains("BULLISH") -> Color(0xFF4CAF50)
    str.contains("BEARISH") -> Color(0xFFFF5252)
    else -> Color(0xFFFFC107)
}

fun getBiasColor(bias: String): Color = when {
    bias.contains("BULLISH") -> Color(0xFF4CAF50)
    bias.contains("BEARISH") -> Color(0xFFFF5252)
    else -> Color.Gray
}

fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value)

fun detectMarketStructure(candles: List<Candle>): String {
    if (candles.size < 20) return "STRUCTURE UNCONFIRMED"
    
    val swingHighs = mutableListOf<Double>()
    val swingLows = mutableListOf<Double>()

    for (i in 2 until candles.size - 2) {
        val c = candles[i]
        if (c.high > candles[i-1].high && c.high > candles[i-2].high &&
            c.high > candles[i+1].high && c.high > candles[i+2].high) {
            swingHighs.add(c.high)
        }
        if (c.low < candles[i-1].low && c.low < candles[i-2].low &&
            c.low < candles[i+1].low && c.low < candles[i+2].low) {
            swingLows.add(c.low)
        }
    }

    if (swingHighs.size >= 2 && swingLows.size >= 2) {
        val h1 = swingHighs[0]
        val h2 = swingHighs[1]
        val l1 = swingLows[0]
        val l2 = swingLows[1]

        return when {
            h1 > h2 && l1 > l2 -> "BULLISH (HH + HL) 🟢"
            h1 < h2 && l1 < l2 -> "BEARISH (LH + LL) 🔴"
            else -> "RANGE / CHOPPY 🟡"
        }
    }
    return "CHOPPY / CONSOLIDATING 🟡"
}

suspend fun fetchAndComputeStructureSignal(): AnalysisResult? {
    return withContext(Dispatchers.IO) {
        try {
            val m15Candles = fetchCandlesForInterval("15min") ?: return@withContext null
            val h1Candles = fetchCandlesForInterval("1h") ?: return@withContext null
            val h4Candles = fetchCandlesForInterval("4h") ?: return@withContext null

            val currentPrice = m15Candles[0].close

            val h4Ema20 = calculateEMA(h4Candles.map { it.close }, 20)
            val h1Ema20 = calculateEMA(h1Candles.map { it.close }, 20)
            val m15Ema20 = calculateEMA(m15Candles.map { it.close }, 20)
            val m15Ema50 = calculateEMA(m15Candles.map { it.close }, 50)

            val rsi14 = calculateRSI(m15Candles.map { it.close }, 14)
            val atr14 = calculateATR(m15Candles, 14)

            val structure = detectMarketStructure(m15Candles)

            val h4Bias = if (currentPrice > h4Ema20) "BULLISH ↑" else "BEARISH ↓"
            val h1Bias = if (currentPrice > h1Ema20) "BULLISH ↑" else "BEARISH ↓"
            val m15Bias = if (m15Ema20 > m15Ema50) "BULLISH ↑" else "BEARISH ↓"

            val isMaBull = h4Bias.contains("BULLISH") && m15Bias.contains("BULLISH")
            val isMaBear = h4Bias.contains("BEARISH") && m15Bias.contains("BEARISH")
            val isStructBull = structure.contains("BULLISH")
            val isStructBear = structure.contains("BEARISH")

            val signalBias: String
            val confluenceScore: String

            if (isMaBull && isStructBull && rsi14 in 45.0..68.0) {
                signalBias = "STRONG BUY 🟢"
                confluenceScore = "96% (High Confluence)"
            } else if (isMaBear && isStructBear && rsi14 in 32.0..55.0) {
                signalBias = "STRONG SELL 🔴"
                confluenceScore = "96% (High Confluence)"
            } else if (isMaBull || isStructBull) {
                signalBias = "WAIT / CHOPPY 🟡"
                confluenceScore = "60% (Unconfirmed Structure)"
            } else {
                signalBias = "WAIT / NO SIGNAL 🟡"
                confluenceScore = "50% (Conflicting Market)"
            }

            val isBuy = signalBias.contains("BUY")
            val entryLow = if (isBuy) currentPrice - (atr14 * 0.2) else currentPrice - (atr14 * 0.1)
            val entryHigh = if (isBuy) currentPrice + (atr14 * 0.1) else currentPrice + (atr14 * 0.2)
            val stopLoss = if (isBuy) currentPrice - (atr14 * 1.5) else currentPrice + (atr14 * 1.5)
            val tp1 = if (isBuy) currentPrice + (atr14 * 1.5) else currentPrice - (atr14 * 1.5)
            val tp2 = if (isBuy) currentPrice + (atr14 * 3.0) else currentPrice - (atr14 * 3.0)

            AnalysisResult(
                currentPrice = currentPrice,
                bias = signalBias,
                confluenceRate = confluenceScore,
                marketStructure = structure,
                rsiValue = rsi14,
                h4Bias = h4Bias,
                h1Bias = h1Bias,
                m15Bias = m15Bias,
                entryLow = entryLow,
                entryHigh = entryHigh,
                stopLoss = stopLoss,
                tp1 = tp1,
                tp2 = tp2
            )
        } catch (e: Exception) {
            null
        }
    }
}

fun calculateEMA(prices: List<Double>, period: Int): Double {
    if (prices.size < period) return prices.firstOrNull() ?: 0.0
    val k = 2.0 / (period + 1)
    val reversed = prices.reversed()
    var ema = reversed.take(period).average()
    for (i in period until reversed.size) {
        ema = (reversed[i] * k) + (ema * (1 - k))
    }
    return ema
}

fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
    if (prices.size <= period) return 50.0
    val reversed = prices.reversed()
    var gains = 0.0
    var losses = 0.0
    for (i in 1..period) {
        val change = reversed[i] - reversed[i - 1]
        if (change >= 0) gains += change else losses += abs(change)
    }
    val avgGain = gains / period
    val avgLoss = losses / period
    if (avgLoss == 0.0) return 100.0
    val rs = avgGain / avgLoss
    return 100.0 - (100.0 / (1.0 + rs))
}

fun calculateATR(candles: List<Candle>, period: Int = 14): Double {
    if (candles.size <= period) return 3.5
    var totalTr = 0.0
    for (i in 0 until period) {
        val high = candles[i].high
        val low = candles[i].low
        val prevClose = if (i + 1 < candles.size) candles[i + 1].close else low
        val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
        totalTr += tr
    }
    return (totalTr / period).coerceAtLeast(2.5)
}

suspend fun fetchCandlesForInterval(interval: String): List<Candle>? {
    return try {
        val urlString = "https://api.twelvedata.com/time_series?symbol=XAU/USD&interval=$interval&outputsize=60&apikey=$API_KEY"
        val response = URL(urlString).readText()
        val json = JSONObject(response)
        if (!json.has("values")) return null

        val values = json.getJSONArray("values")
        val list = mutableListOf<Candle>()
        for (i in 0 until values.length()) {
            val item = values.getJSONObject(i)
            list.add(
                Candle(
                    close = item.getString("close").toDouble(),
                    high = item.getString("high").toDouble(),
                    low = item.getString("low").toDouble()
                )
            )
        }
        list
    } catch (e: Exception) {
        null
    }
}
