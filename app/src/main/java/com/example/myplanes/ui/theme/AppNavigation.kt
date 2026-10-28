package com.example.myplanes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myplanes.data.Quadrant

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onAddTask = { quadrant -> navController.navigate("add/${quadrant.name}") }
            )
        }
        composable("add/{quadrant}") { entry ->
            val quadrant = entry.arguments?.getString("quadrant")
                ?.let { runCatching { Quadrant.valueOf(it) }.getOrNull() }
                ?: Quadrant.URGENT_IMPORTANT

            AddTaskScreen(
                initialQuadrant = quadrant,
                onBack = { navController.popBackStack() }
            )
        }
    }
}