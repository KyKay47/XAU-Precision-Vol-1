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
    var isAnalyzed by remember { mutableStateOf(false) }

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
        
        Button(
            onClick = { isAnalyzed = !isAnalyzed },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(
                text = if (isAnalyzed) "Reset Analysis" else "Initialize Analysis", 
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
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
                        text = "MARKET ANALYSIS (XAU/USD)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TradingRow("Signal Bias:", "BULLISH BUY", Color(0xFF4CAF50), bold = true)
                    TradingRow("Confidence:", "88.4%", Color(0xFFFFD700), bold = true)
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.DarkGray
                    )
                    
                    TradingRow("Entry Zone:", "2650.00 - 2652.50", Color.Cyan, bold = true)
                    TradingRow("Stop Loss (SL):", "2642.00", Color(0xFFFF5252), bold = true)
                    TradingRow("Take Profit 1 (TP1):", "2668.00", Color(0xFF4CAF50))
                    TradingRow("Take Profit 2 (TP2):", "2680.00", Color(0xFF4CAF50), bold = true)
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.DarkGray
                    )

                    TradingRow("Key Support:", "2645.50", Color.LightGray)
                    TradingRow("Key Resistance:", "2682.10", Color.LightGray)
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
