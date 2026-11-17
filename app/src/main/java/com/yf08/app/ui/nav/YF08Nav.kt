package com.yf08.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yf08.app.ui.ControlScreen
import com.yf08.app.ui.OtaScreen
import com.yf08.app.ui.RecordsScreen
import com.yf08.app.ui.ScanScreen
import com.yf08.app.ui.SettingsScreen
import com.yf08.app.viewmodel.MainViewModel

@Composable
fun YF08Nav(navController: NavHostController, vm: MainViewModel) {
    NavHost(navController = navController, startDestination = "control") {
        composable("control") { ControlScreen(vm) }
        composable("records") { RecordsScreen(vm) }
        composable("ota") { OtaScreen(vm) }
        composable("settings") { SettingsScreen(vm) }
        composable("scan") { ScanScreen(vm, onBack = { navController.popBackStack() }) }
    }
}
