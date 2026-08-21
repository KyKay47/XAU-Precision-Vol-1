package com.ferhatozcelik.jetpackcomposetemplate
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Forces a dark theme matching the trading aesthetic
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main App Title
        Text(
            text = "XAU Precision",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700) // Hex code for Gold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Engine: Volume 1",
            fontSize = 18.sp,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Interactive Button
        Button(
            onClick = { /* We will add the actual math/analysis logic here later! */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
        ) {
            Text(
                text = "Initialize Analysis", 
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

