/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.racetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.racetracker.ui.Congratulation
import com.example.racetracker.ui.RaceTrackerApp
import com.example.racetracker.ui.theme.RaceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RaceTrackerTheme {
                // Navigation Controller
                val navController = rememberNavController()

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // NavHost between composable
                    NavHost(navController, startDestination = "raceTracker") {
                        composable("raceTracker") { RaceTrackerApp(navigation = navController) }
                        composable("congratulation/{winner}") { backStackEntry ->
                            Log.d("MainActivity", "onCreate: ${backStackEntry.arguments?.getString("winner")}")
                            val winner = backStackEntry.arguments?.getString("winner")
                            if (winner != null) {
                                Congratulation(
                                    winner,
                                    navigation = navController,
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .safeDrawingPadding()
                                        .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}
