package com.mintly.app.navigation

import androidx.annotation.DrawableRes
import com.mintly.app.R
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mintly.app.ui.auth.AuthViewModel
import com.mintly.app.ui.auth.LoginScreen
import com.mintly.app.ui.auth.SignupScreen
import com.mintly.app.ui.budget.BudgetScreen
import com.mintly.app.ui.home.HomeScreen
import com.mintly.app.ui.ranking.RankingScreen
import com.mintly.app.ui.settings.SettingsScreen
import com.mintly.app.ui.shop.ShopScreen
import com.mintly.app.ui.splash.OnboardingScreen
import com.mintly.app.ui.splash.SplashScreen
import com.mintly.app.ui.theme.거지방Colors

sealed class Screen(val route: String) {
    object Splash     : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login      : Screen("login")
    object Signup     : Screen("signup")
    object Main       : Screen("main")
}

sealed class MainTab(val route: String, val label: String, @DrawableRes val iconRes: Int) {
    object Shop     : MainTab("shop",     "상점",  R.drawable.ic_nav_shop)
    object Ranking  : MainTab("ranking",  "랭킹",  R.drawable.ic_nav_ranking)
    object Home     : MainTab("home",     "홈",    R.drawable.ic_nav_home)
    object Budget   : MainTab("budget",   "가계부", R.drawable.ic_nav_budget)
    object Settings : MainTab("settings", "설정",  R.drawable.ic_nav_settings)
}

private val TABS = listOf(
    MainTab.Shop,
    MainTab.Ranking,
    MainTab.Home,
    MainTab.Budget,
    MainTab.Settings,
)

@Composable
fun 거지방App() {
    val rootNav = rememberNavController()

    NavHost(
        navController = rootNav,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn() },
        exitTransition  = { fadeOut() },
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { isLoggedIn ->
                    val dest = if (isLoggedIn) Screen.Main.route else Screen.Onboarding.route
                    rootNav.navigate(dest) { popUpTo(Screen.Splash.route) { inclusive = true } }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    rootNav.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                }
            )
        }
        composable(Screen.Login.route) {
            val vm: AuthViewModel = hiltViewModel()
            LoginScreen(
                vm = vm,
                onLoginSuccess = {
                    rootNav.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onGoSignup = { rootNav.navigate(Screen.Signup.route) }
            )
        }
        composable(Screen.Signup.route) {
            val vm: AuthViewModel = hiltViewModel()
            SignupScreen(
                vm = vm,
                onSignupSuccess = {
                    rootNav.navigate(Screen.Main.route) { popUpTo(Screen.Signup.route) { inclusive = true } }
                },
                onGoLogin = { rootNav.popBackStack() }
            )
        }
        composable(Screen.Main.route) {
            MainScaffold(
                onLogout = {
                    rootNav.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScaffold(onLogout: () -> Unit) {
    val tabNav = rememberNavController()
    val backStackEntry by tabNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = 거지방Colors.Gray100, thickness = 1.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    TABS.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                tabNav.navigate(tab.route) {
                                    popUpTo(tabNav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(28.dp),
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 14.sp,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = 거지방Colors.Mint400,
                                selectedTextColor   = 거지방Colors.Mint400,
                                indicatorColor      = 거지방Colors.Mint50,
                                unselectedIconColor = 거지방Colors.Gray400,
                                unselectedTextColor = 거지방Colors.Gray400,
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNav,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition  = { fadeOut() },
        ) {
            composable(MainTab.Shop.route)     { ShopScreen() }
            composable(MainTab.Ranking.route)  { RankingScreen() }
            composable(MainTab.Home.route)     {
                HomeScreen(
                    onNavigateToBudget  = { tabNav.navigate(MainTab.Budget.route) { launchSingleTop = true } },
                    onNavigateToRanking = { tabNav.navigate(MainTab.Ranking.route) { launchSingleTop = true } },
                )
            }
            composable(MainTab.Budget.route)   { BudgetScreen() }
            composable(MainTab.Settings.route) { SettingsScreen(onLogout = onLogout) }
        }
    }
}
