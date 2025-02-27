/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.screenshot

import android.content.pm.PackageManager
import android.content.pm.PackageManager.ComponentInfoFlags
import android.content.pm.PackageManager.MATCH_ANY_USER
import android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS
import android.view.Display
import android.view.IWindowManager
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.android.systemui.res.R
import javax.inject.Inject

class ScreenshotDetectionController
@Inject
constructor(
    private val windowManager: IWindowManager,
    private val packageManager: PackageManager,
) {
    /**
     * Notify potentially listening apps of the screenshot. Return a list of the names of the apps
     * notified.
     */
    fun maybeNotifyOfScreenshot(data: ScreenshotData): List<CharSequence> {
        // No notification for screenshots from overview.
        if (data.source == WindowManager.ScreenshotSource.SCREENSHOT_OVERVIEW) return listOf()

        // Notify listeners, retrieve a list of listening components.
        val components = windowManager.notifyScreenshotListeners(Display.DEFAULT_DISPLAY)

        // Convert component names to app names.
        return components.map {
            packageManager
                .getActivityInfo(it, ComponentInfoFlags.of(
                    (MATCH_DISABLED_COMPONENTS or MATCH_ANY_USER).toLong()))
                .loadLabel(packageManager)
        }
    }

    fun populateView(view: ViewGroup, appNames: List<CharSequence>) {
        assert(appNames.isNotEmpty())

        val textView: TextView = view.requireViewById(R.id.screenshot_detection_notice_text)
        if (appNames.size == 1) {
            textView.text =
                view.resources.getString(R.string.screenshot_detected_template, appNames[0])
        } else {
            textView.text =
                view.resources.getString(
                    R.string.screenshot_detected_multiple_template,
                    appNames[0]
                )
        }
    }
}
