package com.ferhatozcelik.jetpackcomposetemplate.ui.activitys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

// REPLACE WITH YOUR FREE TWELVEDATA API KEY
private const val API_KEY = "YOUR_TWELVEDATA_API_KEY_HERE"

data class AnalysisResult(
    val currentPrice: Double,
    val bias: String,
    val confidence: String,
    val entryLow: Double,
    val entryHigh: Double,
    val stopLoss: Double,
    val tp1: Double,
    val tp2: Double,
    val support: Double,
    val resistance: Double
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TradingDashboard()
                }
            }
        }
    }
}

@Composable
fun TradingDashboard() {
    var analysis by remember { mutableStateOf<AnalysisResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun runMarketAnalysis() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = performXauAnalysis()
            if (result != null) {
                analysis = result
            } else {
                errorMessage = "Failed to fetch market data. Check API key/connection."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        runMarketAnalysis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "XAU Precision",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )
        
        Text(
            text = "Engine: Volume 1 (Algorithmic)",
            fontSize = 14.sp,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        // Price Display Box
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "XAU/USD SPOT PRICE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
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

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { runMarketAnalysis() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth(0.9f).height(48.dp),
            enabled = !isLoading
        ) {
            Text("Analyze Market 🔄", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        analysis?.let { data ->
            val isBuy = data.bias.contains("BUY")
            val biasColor = if (isBuy) Color(0xFF4CAF50) else Color(0xFFFF5252)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "LIVE ALGORITHMIC ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TradingRow("Signal Bias:", data.bias, biasColor, bold = true)
                    TradingRow("Confidence:", data.confidence, Color(0xFFFFD700), bold = true)
                    
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray)
                    
                    TradingRow("Entry Zone:", "${fmt(data.entryLow)} - ${fmt(data.entryHigh)}", Color.Cyan, bold = true)
                    TradingRow("Stop Loss (SL):", fmt(data.stopLoss), Color(0xFFFF5252), bold = true)
                    TradingRow("Take Profit 1 (TP1):", fmt(data.tp1), Color(0xFF4CAF50))
                    TradingRow("Take Profit 2 (TP2):", fmt(data.tp2), Color(0xFF4CAF50), bold = true)
                    
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray)

                    TradingRow("Key Support:", fmt(data.support), Color.LightGray)
                    TradingRow("Key Resistance:", fmt(data.resistance), Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun TradingRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Text(
            text = value, 
            color = valueColor, 
            fontSize = 14.sp, 
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value)

// Real Algorithmic Calculation Engine
suspend fun performXauAnalysis(): AnalysisResult? {
    return withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.twelvedata.com/time_series?symbol=XAU/USD&interval=15min&outputsize=30&apikey=$API_KEY"
            val response = URL(urlString).readText()
            val json = JSONObject(response)
            
            if (!json.has("values")) return@withContext null

            val values = json.getJSONArray("values")
            val closes = mutableListOf<Double>()
            val highs = mutableListOf<Double>()
            val lows = mutableListOf<Double>()

            for (i in 0 until values.length()) {
                val item = values.getJSONObject(i)
                closes.add(item.getString("close").toDouble())
                highs.add(item.getString("high").toDouble())
                lows.add(item.getString("low").toDouble())
            }

            // Calculations based on recent 15m candles
            val currentPrice = closes[0]
            
            // Calculate Simple Moving Average (SMA 14)
            val sma14 = closes.take(14).average()
            
            // Estimate basic ATR (Average True Range over 10 periods)
            var atrSum = 0.0
            for (i in 0 until 10) {
                atrSum += (highs[i] - lows[i])
            }
            val atr = (atrSum / 10.0).coerceAtLeast(3.0) // Min $3 range

            // Determine Trend Bias
            val isBullish = currentPrice > sma14
            val bias = if (isBullish) "BULLISH BUY" else "BEARISH SELL"
            
            // Calculate Confidence based on distance from SMA
            val diffPercent = (abs(currentPrice - sma14) / sma14) * 100
            val confidence = String.format(Locale.US, "%.1f%%", (70.0 + (diffPercent * 10)).coerceIn(72.0, 94.5))

            // Dynamic SL/TP based on market volatility (ATR)
            val entryLow: Double
            val entryHigh: Double
            val stopLoss: Double
            val tp1: Double
            val tp2: Double

            if (isBullish) {
                entryLow = currentPrice - (atr * 0.2)
                entryHigh = currentPrice + (atr * 0.1)
                stopLoss = currentPrice - (atr * 1.5)
                tp1 = currentPrice + (atr * 1.5)
                tp2 = currentPrice + (atr * 3.0)
            } else {
                entryLow = currentPrice - (atr * 0.1)
                entryHigh = currentPrice + (atr * 0.2)
                stopLoss = currentPrice + (atr * 1.5)
                tp1 = currentPrice - (atr * 1.5)
                tp2 = currentPrice - (atr * 3.0)
            }

            val support = lows.take(15).minOrNull() ?: (currentPrice - 10.0)
            val resistance = highs.take(15).maxOrNull() ?: (currentPrice + 10.0)

            AnalysisResult(
                currentPrice = currentPrice,
                bias = bias,
                confidence = confidence,
                entryLow = entryLow,
                entryHigh = entryHigh,
                stopLoss = stopLoss,
                tp1 = tp1,
                tp2 = tp2,
                support = support,
                resistance = resistance
            )
        } catch (e: Exception) {
            null
        }
    }
}
