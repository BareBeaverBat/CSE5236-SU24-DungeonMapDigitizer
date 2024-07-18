package com.example.dungeontest.composables

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dungeontest.graph.MapRoom
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

private const val firstDropdownTag = firstDropdownMenuTestTag

private const val secondDropdownTag = secondDropdownMenuTestTag

/**
 * Instrumented test, which will execute on an Android device. Specifically for the dialog
 * that adds edges to or removes them from a graph
 */
@RunWith(AndroidJUnit4::class)
class AddRemoveEdgeDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val roomNames = listOf("room1", "room2", "room3", "room4", "room5", "room6", "room7")
    private val rooms = roomNames.mapIndexed { index, name -> MapRoom(index + 1, name) }
    private val edgeIndices =
        listOf(Pair(0, 1), Pair(0, 5), Pair(1, 2), Pair(1, 4), Pair(2, 6), Pair(4, 6), Pair(5, 6))

    private val baseTestGraph: Graph<MapRoom, DefaultEdge>
        get() {
            val mapGraph: Graph<MapRoom, DefaultEdge> =
                DefaultUndirectedGraph(DefaultEdge::class.java)
            rooms.forEach { mapGraph.addVertex(it) }
            edgeIndices.forEach { mapGraph.addEdge(rooms[it.first], rooms[it.second]) }
            return mapGraph
        }

    private var wasDismissCallbackCalled = false;
    private val testDismissCallback = { wasDismissCallbackCalled = true }

    private var resultGraph: Graph<MapRoom, DefaultEdge>? = null
    private val testFinalizeCallback = { modifiedGraph: Graph<MapRoom, DefaultEdge> ->
        resultGraph = modifiedGraph
    }

    //matcher is complicated b/c content description changes when dropdown is expanded
    val matcherForFirstDropdownButton =
        hasContentDescription("button to ", substring = true).and(
            hasContentDescription("list of candidates for the first room", substring = true)
        )
    val matcherForSecondDropdownButton =
        hasContentDescription("button to ", substring = true).and(
            hasContentDescription("list of candidates for the second room", substring = true)
        )
    val matcherForCancelButton = hasText("Cancel")

    @Test
    fun givenDialogOpenedToAddEdge_whenUserSpecifiesValidNewEdge_thenFinalizeMethodCalledWithEdgeAdded() {
        val testGraph = baseTestGraph
        val graphState: MutableState<Graph<MapRoom, DefaultEdge>?> = mutableStateOf(testGraph)

        composeTestRule.setContent {
            AddRemoveEdgeDialog(
                onDismissRequest = testDismissCallback,
                currGraphState = graphState,
                finalizeGraphEdit = testFinalizeCallback,
                isAddingEdge = true
            )
        }

        //Check that things initially rendered as intended

        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true).assertIsDisplayed()
            .assertTextContains(dropdownNotChosenText)

        composeTestRule.onNode(matcherForFirstDropdownButton).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true).assertIsDisplayed()
            .assertTextContains(dropdownNotChosenText)

        composeTestRule.onNode(matcherForSecondDropdownButton)
            .assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()

        val matcherForConfirmButton = hasText("Add Hallway")
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForCancelButton).assertIsDisplayed().assertIsEnabled()

        //Act on page
        composeTestRule.onNode(matcherForFirstDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsDisplayed()
            .onChildren().assertCountEquals(rooms.size)
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()

        composeTestRule.onNodeWithTag(firstDropdownTag)
            .onChildren().filter(hasText(roomNames[3])).onFirst().performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true)
            .assertTextContains(roomNames[3])
        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true)
            .assertTextContains(dropdownNotChosenText)
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForSecondDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        //room4 has no neighbors, but we aren't supporting the addition of an edge from itself to itself
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsDisplayed()
            .onChildren().assertCountEquals(rooms.size - 1)
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNodeWithTag(secondDropdownTag)
            .onChildren().filter(hasText(roomNames[4])).onFirst().performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true)
            .assertTextContains(roomNames[4])
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsEnabled()

        assertFalse(wasDismissCallbackCalled)
        assertNull(resultGraph)

        composeTestRule.onNode(matcherForConfirmButton).performClick()
        assertTrue(wasDismissCallbackCalled)
        assertNotNull(resultGraph)
        assertTrue(resultGraph!!.containsEdge(rooms[3], rooms[4]))
    }


    @Test
    fun givenDialogOpenedToDropEdge_whenUserSpecifiesExistingEdge_thenFinalizeMethodCalledWithEdgeRemoved() {
        val testGraph = baseTestGraph
        val graphState: MutableState<Graph<MapRoom, DefaultEdge>?> = mutableStateOf(testGraph)

        composeTestRule.setContent {
            AddRemoveEdgeDialog(
                onDismissRequest = testDismissCallback,
                currGraphState = graphState,
                finalizeGraphEdit = testFinalizeCallback,
                isAddingEdge = false
            )
        }

        //Check that things initially rendered as intended

        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true).assertIsDisplayed()

        composeTestRule.onNode(matcherForFirstDropdownButton)
            .assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true).assertIsDisplayed()

        composeTestRule.onNode(matcherForSecondDropdownButton)
            .assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()

        val matcherForConfirmButton = hasText("Remove Hallway")
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForCancelButton).assertIsDisplayed().assertIsEnabled()

        //Act on page
        composeTestRule.onNode(matcherForFirstDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsDisplayed()
            .onChildren().assertCountEquals(rooms.size)
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()

        composeTestRule.onNodeWithTag(firstDropdownTag)
            .onChildren().filter(hasText(roomNames[3])).onFirst().performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true)
            .assertTextContains(roomNames[3])
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForSecondDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        //room4 has no neighbors
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsDisplayed()
            .onChildren().assertCountEquals(0)
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForSecondDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true)
            .assertTextContains(roomNames[3])
        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true)
            .assertTextContains(dropdownNotChosenText)
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForFirstDropdownButton).performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag)
            .onChildren().filter(hasText(roomNames[1])).onFirst().performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(firstDropdownLabel, substring = true)
            .assertTextContains(roomNames[1])
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNode(matcherForSecondDropdownButton).performClick()
        composeTestRule.onNodeWithTag(secondDropdownTag).onChildren().assertCountEquals(3)
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNodeWithTag(secondDropdownTag)
            .onChildren().filter(hasText(roomNames[4])).onFirst().performClick()
        composeTestRule.onNodeWithTag(firstDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(secondDropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(secondDropdownLabel, substring = true)
            .assertTextContains(roomNames[4])
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsEnabled()

        assertFalse(wasDismissCallbackCalled)
        assertNull(resultGraph)

        composeTestRule.onNode(matcherForConfirmButton).performClick()
        assertTrue(wasDismissCallbackCalled)
        assertNotNull(resultGraph)
        assertFalse(resultGraph!!.containsEdge(rooms[1], rooms[4]))
    }


}