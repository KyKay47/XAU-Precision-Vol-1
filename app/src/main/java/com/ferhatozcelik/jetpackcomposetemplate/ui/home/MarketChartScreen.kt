package com.ferhatozcelik.jetpackcomposetemplate.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ferhatozcelik.jetpackcomposetemplate.MarketViewModel
import com.ferhatozcelik.jetpackcomposetemplate.ZoneType

@Composable
fun MarketChartScreen(viewModel: MarketViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Signal Notification Header
        state.latestSignal?.let { signal ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (signal.type.name == "BUY") Color(0xFF1B5E20) else Color(0xFFB71C1C))
                    .padding(12.dp)
            ) {
                Text(
                    text = "SIGNAL: ${signal.type} @ ${signal.entryPrice} | SL: ${signal.stopLoss} | TP: ${signal.targetPrice}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Custom Candlestick & Zone Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val candles = state.candles
            if (candles.isEmpty()) return@Canvas

            val minPrice = candles.minOf { it.low }
            val maxPrice = candles.maxOf { it.high }
            val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

            val candleWidth = size.width / candles.size.toFloat()

            // 1. Draw Active POI Zones (FVG / OB)
            for (zone in state.activeZones) {
                if (zone.isMitigated) continue

                val topY = size.height - ((zone.topPrice - minPrice) / priceRange * size.height).toFloat()
                val bottomY = size.height - ((zone.bottomPrice - minPrice) / priceRange * size.height).toFloat()

                val zoneColor = when (zone.type) {
                    ZoneType.BULLISH_FVG, ZoneType.BULLISH_OB -> Color(0x404CAF50) // Semi-transparent Green
                    ZoneType.BEARISH_FVG, ZoneType.BEARISH_OB -> Color(0x40F44336) // Semi-transparent Red
                }

                drawRect(
                    color = zoneColor,
                    topLeft = Offset(0f, topY),
                    size = Size(size.width, bottomY - topY)
                )
            }

            // 2. Draw Candlesticks (Green / Red)
            candles.forEachIndexed { index, candle ->
                val x = index * candleWidth + (candleWidth / 2)

                val highY = size.height - ((candle.high - minPrice) / priceRange * size.height).toFloat()
                val lowY = size.height - ((candle.low - minPrice) / priceRange * size.height).toFloat()
                val openY = size.height - ((candle.open - minPrice) / priceRange * size.height).toFloat()
                val closeY = size.height - ((candle.close - minPrice) / priceRange * size.height).toFloat()

                val isBullish = candle.close >= candle.open
                val candleColor = if (isBullish) Color(0xFF00E676) else Color(0xFFFF5252)

                // Wick Line
                drawLine(
                    color = candleColor,
                    start = Offset(x, highY),
                    end = Offset(x, lowY),
                    strokeWidth = 2f
                )

                // Candle Body
                val bodyTop = Math.min(openY, closeY)
                val bodyHeight = Math.max(Math.abs(openY - closeY), 2f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(x - (candleWidth * 0.35f), bodyTop),
                    size = Size(candleWidth * 0.7f, bodyHeight)
                )
            }
        }
    }
}
