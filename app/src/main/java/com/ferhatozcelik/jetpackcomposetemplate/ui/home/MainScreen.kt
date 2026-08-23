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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ferhatozcelik.jetpackcomposetemplate.domain.model.CompleteMarketAnalysis
import com.ferhatozcelik.jetpackcomposetemplate.domain.model.TradeSignal

@Composable
fun MainScreen(viewModel: GoldViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            Divider()

            // Market Structure
            Text("Market Structure", fontWeight = FontWeight.Bold)
            Text("• H4 Trend: ${analysis.structure.h4Trend}")
            Text("• H1 Trend: ${analysis.structure.h1Trend}")
            Text("• Key Swing High: $${analysis.structure.swingHigh}")

            Divider()

            // Price Action & Movement
            Text("Price Action & Movement", fontWeight = FontWeight.Bold)
            Text("• Candle Pattern: ${analysis.action.pattern}")
            Text("• Rejection Wick: ${if (analysis.action.hasRejection) "Yes" else "No"}")
            Text("• Bar Range: $${"%.2f".format(analysis.movement.rangeInDollars)}")
            Text("• Movement: ${if (analysis.movement.isExplosive) "Explosive" else "Normal"}")
        }
    }
}
