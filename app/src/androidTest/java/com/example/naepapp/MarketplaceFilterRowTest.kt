package com.example.naepapp

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.naepapp.ui.components.MarketplaceFilterRow
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.FeedSort
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MarketplaceFilterRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun marketplaceFilterRow_displaysOptionsAndHandlesSelection() {
        var selectedSort = FeedSort.Likes
        var selectedCategory: Int? = null

        val categories = listOf(
            CategoryUi(id = 1, name = "Paintings", bio = ""),
            CategoryUi(id = 2, name = "Sculptures", bio = "")
        )

        composeTestRule.setContent {
            MarketplaceFilterRow(
                sort = selectedSort,
                onSortSelected = { selectedSort = it },
                categories = categories,
                selectedCategoryId = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
        }

        composeTestRule.onNodeWithText("Most Liked").assertIsSelected()
        composeTestRule.onNodeWithText("Most Viewed").performClick()
        assertEquals(FeedSort.Views, selectedSort)

        composeTestRule.onNodeWithText("Paintings").performClick()
        assertEquals(1, selectedCategory)
    }
}
