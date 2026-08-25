package com.ferhatozcelik.jetpackcomposetemplate.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ferhatozcelik.jetpackcomposetemplate.MarketViewModel
import com.ferhatozcelik.jetpackcomposetemplate.ui.home.MarketChartScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "market_chart"
    ) {
        composable(route = "market_chart") {
            val marketViewModel: MarketViewModel = viewModel()
            MarketChartScreen(viewModel = marketViewModel)
        }
    }
}
