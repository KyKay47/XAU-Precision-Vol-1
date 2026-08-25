package com.ferhatozcelik.jetpackcomposetemplate

data class MarketAnalysisState(
    val candles: List<Candle> = emptyList(),
    val structuralBreaks: List<StructuralBreak> = emptyList(),
    val activeZones: List<SmartMoneyZone> = emptyList(),
    val latestSignal: TradeSignal? = null
)

class MarketRepository(
    private val structureEngine: MarketStructureEngine = MarketStructureEngine(swingLookback = 3),
    private val zoneEngine: ZoneDetectionEngine = ZoneDetectionEngine(fvgMinThreshold = 0.5),
    private val triggerEngine: PriceActionTriggerEngine = PriceActionTriggerEngine(riskRewardRatio = 2.0)
) {

    fun processCandles(candles: List<Candle>): MarketAnalysisState {
        if (candles.size < 5) return MarketAnalysisState(candles = candles)

        // 1. Detect BOS / CHOCH Breaks
        val breaks = structureEngine.analyze(candles)

        // 2. Identify FVGs and Order Blocks from ZoneDetectionEngine (in OrderBlockEngine.kt)
        val fvgs = zoneEngine.detectFairValueGaps(candles)
        val orderBlocks = zoneEngine.detectOrderBlocks(candles, breaks)
        val allZones = fvgs + orderBlocks

        // 3. Update zone mitigation states across candles
        for (candle in candles) {
            zoneEngine.updateMitigationState(allZones, candle)
        }

        // 4. Check for active entry signals on the latest closed candle
        val currentCandle = candles.last()
        val previousCandle = candles[candles.size - 2]
        val signal = triggerEngine.evaluateEntry(currentCandle, previousCandle, allZones)

        return MarketAnalysisState(
            candles = candles,
            structuralBreaks = breaks,
            activeZones = allZones,
            latestSignal = signal
        )
    }
}
