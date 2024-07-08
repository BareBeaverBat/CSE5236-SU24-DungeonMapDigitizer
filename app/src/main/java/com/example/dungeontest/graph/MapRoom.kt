package com.example.dungeontest.graph

data class MapRoom(val id: Int, var label: String) {

    override fun equals(other: Any?): Boolean {
        return other is MapRoom && hashCode() == other.hashCode()
    }

    /**
     * basing identity solely on the id field so that nodes in the graph can be renamed dynamically
     * without a gratuitously expensive process of removing an immutable node from the graph (
     * recording its neighbors) then inserting a new immutable node with the same id but different
     * label (and then re-adding the edges to its neighbors)
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }
}
