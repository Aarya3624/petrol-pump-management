package com.aarya.csaassistant.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.aarya.csaassistant.screens.EmployeesScreen
import com.aarya.csaassistant.screens.EntriesScreen
import com.aarya.csaassistant.screens.EntryDetailsScreen
import com.aarya.csaassistant.screens.HomeScreen
import com.aarya.csaassistant.screens.LoadingScreen
import com.aarya.csaassistant.screens.SettingsScreen
import com.aarya.csaassistant.screens.SettlementPage
import com.aarya.csaassistant.screens.ShiftChangeScreen
import com.aarya.csaassistant.screens.SignUpScreen
import com.aarya.csaassistant.viewmodel.EntryViewModel

object Routes {
    const val HOME = "home"
    const val ENTRY_FORM_FLOW = "entryFormFlow"
    const val SHIFT_CHANGE = "shiftChange"
    const val SETTLEMENT = "settlement"
    const val ENTRIES = "entries"
    const val ENTRY_DETAILS = "entryDetails"
    const val ENTRY_ID_ARG = "entryId"
    const val EMPLOYEES = "employees"
    const val LOADING = "loading"
    const val SIGNUP = "signup"
    const val SETTINGS = "settings"

}

@Composable
fun NavigationGraph(
) {
    val navController = rememberNavController()

    val defaultEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition) = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500) // Adjust duration as needed
        ) + fadeIn(animationSpec = tween(500))
    }
    val defaultExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition) = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500)
        ) + fadeOut(animationSpec = tween(500))
    }
    val defaultPopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition) = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(500)
        ) + fadeIn(animationSpec = tween(500))
    }
    val defaultPopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition) = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(500)
        ) + fadeOut(animationSpec = tween(500))
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOADING,
        modifier = Modifier,
        enterTransition = defaultEnterTransition,
        exitTransition = defaultExitTransition,
        popEnterTransition = defaultPopEnterTransition,
        popExitTransition = defaultPopExitTransition
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onShiftChange = { navController.navigate(Routes.SHIFT_CHANGE) },
                onSalesClick = {}, onEntriesClick = {navController.navigate(Routes.ENTRIES)},
                onEmployeesClick = { navController.navigate(Routes.EMPLOYEES) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        entryFormGraph(navController)

        composable(Routes.ENTRIES) {
            EntriesScreen(
                navController = navController
            )
        }

        composable(
            route = "${Routes.ENTRY_DETAILS}/{${Routes.ENTRY_ID_ARG}}",
            arguments = listOf(navArgument(Routes.ENTRY_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(Routes.ENTRY_ID_ARG)
            if (entryId != null) {
                EntryDetailsScreen(
                    entryId = entryId,
                    navController = navController
                )
            } else {
                // Optionally handle the case where entryId is null,
                // though NavController should prevent this if the argument is not nullable.
                // For safety, or if the argument could be optional:
                navController.popBackStack()
            }
        }

        composable(Routes.EMPLOYEES) {
            EmployeesScreen()
        }
        composable(Routes.LOADING) {
            LoadingScreen(onLoaded = { navController.navigate(Routes.HOME) }, onNotAuthenticated = { navController.navigate(Routes.SIGNUP) })
        }
        composable(Routes.SIGNUP) {
            SignUpScreen(onSignUpSuccess = { navController.navigate(Routes.HOME) }, onLoginClick = { navController.navigate(Routes.SIGNUP) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBackClick = { navController.popBackStack() }, onSignOut = { navController.navigate(Routes.SIGNUP) })
        }
    }
}

fun NavGraphBuilder.entryFormGraph(navController: NavController) {
    navigation(
        startDestination = Routes.SHIFT_CHANGE,
        route = Routes.ENTRY_FORM_FLOW
    ) {
        composable(Routes.SHIFT_CHANGE) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.ENTRY_FORM_FLOW)
            }
            val entryViewModel: EntryViewModel = hiltViewModel(parentEntry)

            ShiftChangeScreen(
                navController = navController,
                entryViewModel = entryViewModel,
            )
        }

        composable(Routes.SETTLEMENT) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.ENTRY_FORM_FLOW)
            }
            val entryViewModel: EntryViewModel = hiltViewModel(parentEntry)

            SettlementPage(
                navController = navController,
                viewModel = entryViewModel
            )
        }
    }
}