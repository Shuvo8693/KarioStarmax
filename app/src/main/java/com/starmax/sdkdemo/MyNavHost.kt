package com.starmax.sdkdemo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.starmax.sdkdemo.pages.CustomHealthGoalTasksPage
import com.starmax.sdkdemo.pages.CustomHealthGoalsPage
import com.starmax.sdkdemo.pages.DevicePage
import com.starmax.sdkdemo.pages.EventReminderPage
import com.starmax.sdkdemo.pages.GoalsDayAndNightPage
import com.starmax.sdkdemo.pages.GoalsNotUpPage
import com.starmax.sdkdemo.pages.Gts10HealthIntervalPage
import com.starmax.sdkdemo.pages.Gts10PairPage
import com.starmax.sdkdemo.pages.HealthHistoryPage
import com.starmax.sdkdemo.pages.HomePage
import com.starmax.sdkdemo.pages.InstructionListPage
import com.starmax.sdkdemo.pages.QuickBatteryModePage
import com.starmax.sdkdemo.pages.ScanPage

enum class NavPage {
    HomePage,
    ScanPage,
    GoalsDayAndNightPage,
    GoalsNotUpPage,
    CustomHealthGoalsPage,
    CustomHealthGoalTasksPage,
    QuickBatteryModePage,
    Gts10HealthIntervalPage,
    InstructionListPage,
    EventReminderPage,
    Gts10PairPage,
    HealthHistoryPage,
    DevicePage
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided!")
}

@Composable
fun MyNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavPage.HomePage.name) {
        composable(NavPage.HomePage.name){
            HomePage(navController)
        }
        composable(NavPage.ScanPage.name){
            ScanPage(navController)
        }
        composable(NavPage.GoalsDayAndNightPage.name){
            GoalsDayAndNightPage(navController)
        }
        composable(NavPage.GoalsNotUpPage.name){
            GoalsNotUpPage(navController)
        }
        composable(NavPage.CustomHealthGoalsPage.name){
            CustomHealthGoalsPage(navController)
        }
        composable(NavPage.CustomHealthGoalTasksPage.name+"/{index}", arguments = listOf(
            navArgument("index"){
                type = NavType.StringType
                defaultValue = "0"
                nullable = false
            }
        )){
            val index = it.arguments?.getString("index") ?: "0"
            CustomHealthGoalTasksPage(index.toInt(),navController)
        }
        composable(NavPage.QuickBatteryModePage.name){
            QuickBatteryModePage(navController)
        }
        composable(NavPage.Gts10HealthIntervalPage.name){
            Gts10HealthIntervalPage(navController)
        }
        composable(NavPage.InstructionListPage.name){
            InstructionListPage(navController)
        }
        composable(NavPage.EventReminderPage.name){
            EventReminderPage(navController)
        }
        composable(NavPage.Gts10PairPage.name){
            Gts10PairPage(navController)
        }
        composable(NavPage.HealthHistoryPage.name){
            HealthHistoryPage(navController)
        }
        composable(NavPage.DevicePage.name){
            DevicePage(navController)
        }
    }
}