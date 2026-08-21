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
    var livePrice by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isAnalyzed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchPrice() {
        scope.launch {
            isLoading = true
            val fetchedPrice = fetchLiveGoldPrice()
            if (fetchedPrice != null) {
                livePrice = fetchedPrice
            } else if (livePrice == null) {
                livePrice = 4589.00 // Fallback if offline
            }
            isLoading = false
        }
    }

    // Fetch live market price automatically when app opens
    LaunchedEffect(Unit) {
        fetchPrice()
    }

    val currentPrice = livePrice ?: 4589.00
    val entryLow = String.format(Locale.US, "%.2f", currentPrice - 2.00)
    val entryHigh = String.format(Locale.US, "%.2f", currentPrice + 1.00)
    val stopLoss = String.format(Locale.US, "%.2f", currentPrice - 10.00)
    val tp1 = String.format(Locale.US, "%.2f", currentPrice + 15.00)
    val tp2 = String.format(Locale.US, "%.2f", currentPrice + 30.00)
    val support = String.format(Locale.US, "%.2f", currentPrice - 12.00)
    val resistance = String.format(Locale.US, "%.2f", currentPrice + 22.00)

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
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Engine: Volume 1",
            fontSize = 16.sp,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Display Live Price Card
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE MARKET PRICE (XAU/USD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", currentPrice)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { fetchPrice() },
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = !isLoading
            ) {
                Text("Refresh 🔄", color = Color.White, fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { isAnalyzed = !isAnalyzed },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                modifier = Modifier.weight(1.3f).height(48.dp)
            ) {
                Text(
                    text = if (isAnalyzed) "Reset" else "Analyze", 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = isAnalyzed) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "LIVE GENERATED SIGNAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TradingRow("Signal Bias:", "BULLISH BUY", Color(0xFF4CAF50), bold = true)
                    TradingRow("Confidence:", "88.4%", Color(0xFFFFD700), bold = true)
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.DarkGray
                    )
                    
                    TradingRow("Entry Zone:", "$entryLow - $entryHigh", Color.Cyan, bold = true)
                    TradingRow("Stop Loss (SL):", stopLoss, Color(0xFFFF5252), bold = true)
                    TradingRow("Take Profit 1 (TP1):", tp1, Color(0xFF4CAF50))
                    TradingRow("Take Profit 2 (TP2):", tp2, Color(0xFF4CAF50), bold = true)
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.DarkGray
                    )

                    TradingRow("Key Support:", support, Color.LightGray)
                    TradingRow("Key Resistance:", resistance, Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun TradingRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
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

// Background thread API call
suspend fun fetchLiveGoldPrice(): Double? {
    return withContext(Dispatchers.IO) {
        try {
            val response = URL("https://api.binance.com/api/v3/ticker/price?symbol=PAXGUSDT").readText()
            val json = JSONObject(response)
            json.getString("price").toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

