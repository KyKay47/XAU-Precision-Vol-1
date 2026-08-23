package com.ferhatozcelik.jetpackcomposetemplate.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferhatozcelik.jetpackcomposetemplate.util.CompleteMarketAnalysis
import com.ferhatozcelik.jetpackcomposetemplate.util.TradeSignal

@Composable
fun MainScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "XAU/USD Precision Engine",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = { viewModel.analyzeGoldMarket() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Gold Market")
        }

        when (val state = uiState) {
            is GoldUiState.Idle -> Text("Tap Analyze to evaluate live structure.")
            is GoldUiState.Loading -> CircularProgressIndicator()
            is GoldUiState.Success -> AnalysisCard(analysis = state.data)
            is GoldUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
        }
    }
}

@Composable
fun AnalysisCard(analysis: CompleteMarketAnalysis) {
    val signalColor = when (analysis.signal) {
        TradeSignal.BUY -> Color(0xFF2E7D32)
        TradeSignal.SELL -> Color(0xFFC62828)
        TradeSignal.WAIT -> Color(0xFFEF6C00)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Signal Badge
            Box(
                modifier = Modifier
                    .background(signalColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SIGNAL: ${analysis.signal}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text("Rationale: ${analysis.rationale}", fontSize = 14.sp)
            HorizontalDivider()

            // Market Structure
            Text("Market Structure", fontWeight = FontWeight.Bold)
            Text("• H4 Trend: ${analysis.structure.h4Trend}")
            Text("• H1 Trend: ${analysis.structure.h1Trend}")
            Text("• Key Swing High: $${analysis.structure.swingHigh}")
            Text("• Key Swing Low: $${analysis.structure.swingLow}")
            HorizontalDivider()

            // Price Action & Movement
            Text("Price Action & Movement", fontWeight = FontWeight.Bold)
            Text("• Candle Pattern: ${analysis.action.pattern}")
            Text("• Rejection Wick: ${if (analysis.action.hasRejection) "Yes" else "No"}")
            Text("• Bar Range: $${"%.2f".format(analysis.movement.rangeInDollars)}")
            Text("• Movement: ${if (analysis.movement.isExplosive) "Explosive" else "Normal"}")
        }
    }
}
