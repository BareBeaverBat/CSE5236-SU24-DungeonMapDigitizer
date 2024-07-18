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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dungeontest.graph.MapRoom
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val dropdownTag = roomDropdownTestTag

/**
 * Instrumented test, which will execute on an Android device. Specifically for the dialog
 * that renames nodes in a graph
 */
@RunWith(AndroidJUnit4::class)
class RenameNodeDialogTest {
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
    val matcherForDropdownButton = hasContentDescription("button to ", substring = true).and(
        hasContentDescription("list of rooms", substring = true)
    )

    val matcherForConfirmButton = hasText(renameConfirmButtonText)
    val matcherForCancelButton = hasText("Cancel")

    @Test
    fun givenDialogOpenedToRenameNode_whenUserSpecifiesValidNodeAndName_thenFinalizeMethodCalledWithNodeRenamed() {
        val testGraph = baseTestGraph
        val graphState: MutableState<Graph<MapRoom, DefaultEdge>?> = mutableStateOf(testGraph)

        composeTestRule.setContent {
            RenameNodeDialog(
                onDismissRequest = testDismissCallback,
                currGraphState = graphState,
                finalizeGraphEdit = testFinalizeCallback,
            )
        }

        //Check that things initially rendered as intended
        composeTestRule.onNodeWithText(roomDropdownLabel, substring = true).assertIsDisplayed()
            .assertTextContains(dropdownNotChosenText)

        composeTestRule.onNode(matcherForDropdownButton)
            .assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(dropdownTag).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(newRoomNameFieldLabel).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNode(matcherForCancelButton).assertIsDisplayed().assertIsEnabled()

        //Act on page
        composeTestRule.onNode(matcherForDropdownButton).performClick()
        composeTestRule.onNodeWithTag(dropdownTag).assertIsDisplayed()
            .onChildren().assertCountEquals(rooms.size)

        composeTestRule.onNodeWithTag(dropdownTag)
            .onChildren().filter(hasText(roomNames[5])).onFirst().performClick()
        composeTestRule.onNodeWithTag(dropdownTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(roomDropdownLabel, substring = true)
            .assertTextContains(roomNames[5])

        val newName = "room6Renamed!"
        composeTestRule.onNodeWithText(newRoomNameFieldLabel).performTextClearance()
        composeTestRule.onNodeWithText(newRoomNameFieldLabel).performTextInput(newName)

        composeTestRule.onNode(matcherForConfirmButton).assertIsDisplayed().assertIsEnabled()

        assertFalse(wasDismissCallbackCalled)
        assertNull(resultGraph)

        composeTestRule.onNode(matcherForConfirmButton).performClick()
        assertTrue(wasDismissCallbackCalled)
        assertNotNull(resultGraph)
        val resultGraphRoomNames = resultGraph!!.vertexSet().map { it.label }
        assertFalse(resultGraphRoomNames.contains(roomNames[5]))
        assertTrue(resultGraphRoomNames.contains(newName))
    }


}