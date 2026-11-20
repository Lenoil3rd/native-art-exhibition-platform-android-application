package com.example.naepapp

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.naepapp.ui.components.AvailabilityToggle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AvailabilityToggleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun availabilityToggle_displaysStateAndHandlesToggles() {
        var isAvailable = true

        composeTestRule.setContent {
            AvailabilityToggle(
                available = isAvailable,
                onToggle = { isAvailable = !isAvailable }
            )
        }

        composeTestRule.onNodeWithTag("availability_switch").assertIsOn()
        composeTestRule.onNodeWithTag("availability_switch").performClick()
        assertFalse(isAvailable)
    }
}
