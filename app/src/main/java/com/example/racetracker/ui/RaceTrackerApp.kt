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
package com.example.racetracker.ui

import android.util.Log
//import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.racetracker.R
import com.example.racetracker.ui.theme.RaceTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RaceTrackerApp(navigation: NavHostController) {
    /**
     * Note: To survive the configuration changes such as screen rotation, [rememberSaveable] should
     * be used with custom Saver object. But to keep the example simple, and keep focus on
     * Coroutines that implementation detail is stripped out.
     */

    var playerProgressIncrement by remember { mutableStateOf(List(2) { 1 }) }
    var playerProgressDecrement by remember { mutableStateOf(List(2) { -1 }) }

    val playerOne = remember {
        RaceParticipant(name = "Player 1", progressIncrement = 1)
    }
    val playerTwo = remember {
        RaceParticipant(name = "Player 2", progressIncrement = 2)
    }
    val playerThree = remember {
        RaceParticipant(
            name = "Player 3",
            progressIncrement = -1,
            maxProgress = 0,
            initialProgress = 100
        )
    }
    val playerFour = remember {
        RaceParticipant(
            name = "Player 4",
            progressIncrement = -2,
            maxProgress = 0,
            initialProgress = 100
        )
    }

    var raceInProgress by remember { mutableStateOf(false) }
    var raceEnded by remember { mutableStateOf(false) }
    // Enable LocalContext if necessary (As we are using Toast)
//    val context = LocalContext.current

    LaunchedEffect(raceInProgress) {
        if (raceInProgress) {
            playerProgressIncrement = List(2) { Random.nextInt(1, 4) }
            playerProgressDecrement = List(2) { Random.nextInt(1, 4) * (-1) }
            Log.d("Progress Rate", "playerOne: ${playerProgressIncrement[0]}")
            Log.d("Progress Rate", "playerTwo: ${playerProgressIncrement[1]}")
            Log.d("Progress Rate", "playerThree: ${playerProgressDecrement[0]}")
            Log.d("Progress Rate", "playerFour: ${playerProgressDecrement[1]}")
            coroutineScope {
                val jobOne = launch { playerOne.run(playerProgressIncrement[0]) }
                val jobTwo = launch { playerTwo.run(playerProgressIncrement[1]) }
                val jobThree = launch { playerThree.run(playerProgressDecrement[0]) }
                val jobFour = launch { playerFour.run(playerProgressDecrement[1]) }

                val jobs = listOf(jobOne, jobTwo, jobThree, jobFour)
                jobs.forEachIndexed { index, job ->
                    job.invokeOnCompletion {throwable ->
                        if (throwable == null) {
                            val currentWinner = when (index) {
                                0 -> if (playerOne.hasFinished()) playerOne.name else null
                                1 -> if (playerTwo.hasFinished()) playerTwo.name else null
                                2 -> if (playerThree.hasFinished()) playerThree.name else null
                                3 -> if (playerFour.hasFinished()) playerFour.name else null
                                else -> null
                            }
                            if (currentWinner != null) {
                                // Cancel all other jobs as we have a winner
                                jobs.forEach { it.cancel() }
                                raceInProgress = false
                                raceEnded = true

                                // Showing the toast on UI thread or Navigate to Congratulation screen
                                launch(Dispatchers.Main) {
//                                    Toast.makeText(context, currentWinner, Toast.LENGTH_LONG).show()
                                    navigation.navigate("congratulation/$currentWinner")
                                }

                            }
                        }

                    }
                }
            }
        }

    }

    RaceTrackerScreen(
        playerOne = playerOne,
        playerTwo = playerTwo,
        playerThree = playerThree,
        playerFour = playerFour,
        isRunning = raceInProgress,
        onRunStateChange = { raceInProgress = it },
        isEnded = raceEnded,
        onEndStateChange = { raceEnded = it },
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
    )
}

@Composable
private fun RaceTrackerScreen(
    playerOne: RaceParticipant,
    playerTwo: RaceParticipant,
    playerThree: RaceParticipant,
    playerFour: RaceParticipant,
    isRunning: Boolean,
    onRunStateChange: (Boolean) -> Unit,
    isEnded: Boolean,
    onEndStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.run_a_race),
            style = MaterialTheme.typography.headlineSmall,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_walk),
                contentDescription = null,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            )
            StatusIndicator(
                participantName = playerOne.name,
                currentProgress = playerOne.currentProgress,
                maxProgress = stringResource(
                    R.string.progress_percentage,
                    playerOne.maxProgress
                ),
                progressFactor = playerOne.progressFactor,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_large)))
            StatusIndicator(
                participantName = playerTwo.name,
                currentProgress = playerTwo.currentProgress,
                maxProgress = stringResource(
                    R.string.progress_percentage,
                    playerTwo.maxProgress
                ),
                progressFactor = playerTwo.progressFactor,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_large)))
            StatusIndicator(
                participantName = playerThree.name,
                currentProgress = playerThree.currentProgress,
                maxProgress = stringResource(
                    R.string.progress_percentage,
                    playerThree.maxProgress
                ),
                progressFactor = playerThree.progressFactor,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_large)))
            StatusIndicator(
                participantName = playerFour.name,
                currentProgress = playerFour.currentProgress,
                maxProgress = stringResource(
                    R.string.progress_percentage,
                    playerFour.maxProgress
                ),
                progressFactor = playerFour.progressFactor,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.padding_large)))
            RaceControls(
                isRunning = isRunning,
                isEnded = isEnded,
                onRunStateChange = onRunStateChange,
                onReset = {
                    playerOne.reset()
                    playerTwo.reset()
                    playerThree.reset()
                    playerFour.reset()
                    onRunStateChange(false)
                    onEndStateChange(false)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    participantName: String,
    currentProgress: Int,
    maxProgress: String,
    progressFactor: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = participantName,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            LinearProgressIndicator(
                progress = progressFactor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.progress_indicator_height))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.progress_indicator_corner_radius)))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.progress_percentage, currentProgress),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = maxProgress,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RaceControls(
    onRunStateChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    isRunning: Boolean = true,
    isEnded: Boolean = false
) {
    Column(
        modifier = modifier.padding(top = dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        Button(
            onClick = { onRunStateChange(!isRunning) },
            enabled = !isEnded,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isRunning) stringResource(R.string.pause) else stringResource(R.string.start))
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.reset))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RaceTrackerAppPreview() {
    RaceTrackerTheme {
        RaceTrackerApp(rememberNavController())
    }
}
