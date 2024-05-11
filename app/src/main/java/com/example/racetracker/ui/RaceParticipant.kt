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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * This class represents a state holder for race participant.
 */
class RaceParticipant(
    val name: String,
    val maxProgress: Int = 100,
    val progressDelayMillis: Long = 500L,
    val progressIncrement: Int = 1,
    val initialProgress: Int = 0
) {
    init {
//        require(maxProgress > 0) { "maxProgress=$maxProgress; must be > 0" }
//        require(progressIncrement > 0) { "progressIncrement=$progressIncrement; must be > 0" }
        require(progressIncrement != 0) { "progressIncrement=$progressIncrement; must not be 0" }
    }

    /**
     * Indicates the race participant's current progress
     */
    var currentProgress by mutableIntStateOf(initialProgress)
        private set

    /**
     * Updates the value of [currentProgress] by value [progressIncrement] until it reaches
     * [maxProgress]. There is a delay of [progressDelayMillis] between each update.
     */
    suspend fun run() {
//        while (currentProgress < maxProgress) {
//            delay(progressDelayMillis)
//            currentProgress += progressIncrement
//        }
        // Check if it is increasing or decreasing
        if (initialProgress < maxProgress && progressIncrement > 0) {
            while (currentProgress < maxProgress) {
                delay(progressDelayMillis)
                currentProgress += progressIncrement
            }
        }
        else if (initialProgress > maxProgress && progressIncrement < 0) {
            while (currentProgress > maxProgress) {
                delay(progressDelayMillis)
                currentProgress += progressIncrement
            }
        }
    }

    suspend fun run(customProgressIncrement: Int) {
        // Check if it is increasing or decreasing
        if (initialProgress < maxProgress && customProgressIncrement > 0) {
            while (currentProgress < maxProgress) {
                delay(progressDelayMillis)
                currentProgress += customProgressIncrement
                if (currentProgress > maxProgress) {
                    currentProgress = maxProgress
                }
            }
        }
        else if (initialProgress > maxProgress && customProgressIncrement < 0) {
            while (currentProgress > maxProgress) {
                delay(progressDelayMillis)
                currentProgress += customProgressIncrement
                if (currentProgress < maxProgress) {
                    currentProgress = maxProgress
                }
            }
        }
    }

    /**
     * Regardless of the value of [initialProgress] the reset function will reset the
     * [currentProgress] to 0
     */
    fun reset() {
        currentProgress = initialProgress
    }

    /**
     * Check if the race participant has reached the [maxProgress]
     */
    fun hasFinished(): Boolean {
        return (progressIncrement > 0 && currentProgress >= maxProgress) ||
                (progressIncrement < 0 && currentProgress <= maxProgress)
    }

    /**
     * Set the current progress to [progress]
     * Usable for Stats in Congratulation Page
     */
    fun setProgress(progress: Int) {
        currentProgress = progress
    }
}

/**
 * The Linear progress indicator expects progress value in the range of 0-1. This property
 * calculate the progress factor to satisfy the indicator requirements.
 */
val RaceParticipant.progressFactor: Float
    get() = if (progressIncrement > 0) {
        currentProgress / maxProgress.toFloat()
    } else {
        (maxProgress - currentProgress) / (maxProgress - initialProgress).toFloat()
    }


