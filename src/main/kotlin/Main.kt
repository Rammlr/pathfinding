import java.io.File

data class Node(val row: Int, val column: Int, var cost: Int) {
    var startDistance: Int = Int.MAX_VALUE
    var heuristicEndDistance: Int = Int.MAX_VALUE

    val totalDistance get() = startDistance + heuristicEndDistance
}

fun main() {
    val nodes = mutableListOf<MutableList<Node>>()
    var i = 0

    File("input.txt").forEachLine { line ->
        nodes.add(mutableListOf())
        line.forEachIndexed { j, char ->
            nodes[nodes.size - 1].add(Node(i, j, char.digitToInt()))
        }
        i++
    }

    doAStar(nodes)

    println()

    val expandedNodes = nodes.expand()

    doAStar(expandedNodes)
}

private fun MutableList<MutableList<Node>>.expand(): MutableList<MutableList<Node>> {
    val rows = this.size
    val columns = this[0].size

    val expandedNodes = mutableListOf<MutableList<Node>>()
    // generate new dummy matrix
    for (row in 0 until rows * 5) {
        expandedNodes.add(mutableListOf())
        for (column in 0 until columns * 5) {
            expandedNodes[row].add(Node(0, 0, 0))
        }
    }

    for (row in 0 until rows * 5) {
        for (column in 0 until columns * 5) {
            val originalNode = this[row % rows][column % columns]
            val newCost = row / rows + column / columns + originalNode.cost
            expandedNodes[row][column] = Node(row, column, newCost.wrap(9))
        }
    }

    return expandedNodes
}

private fun Int.wrap(limit: Int): Int {
    return if(this > limit){
        this % limit
    } else this
}

private fun doAStar(nodes: MutableList<MutableList<Node>>) {
    val open = mutableListOf<Node>()
    val cameFrom = mutableMapOf<Node, Node>()

    val goal = nodes[nodes.size - 1][nodes[0].size - 1]
    val startingPoint = nodes[0][0].apply {
        startDistance = 0
        heuristicEndDistance = calculateHeuristicEndDistance(this, goal)
    }
    open.add(startingPoint)


    while (open.isNotEmpty()) {
        open.sortBy { it.totalDistance }
        val currentNode = open.first()
        if (currentNode == goal) {
            val finalPath = cameFrom.reconstructPath(goal)
            println("final path: $finalPath")
            println("final cost: ${finalPath.sumOf { it.cost } - startingPoint.cost}")
            return
        }

        open.remove(currentNode)
        nodes.getNeighbours(currentNode).forEach { neighbourNode ->
            val newStartDistance = neighbourNode.cost + currentNode.startDistance

            if (newStartDistance < neighbourNode.startDistance) {
                cameFrom[neighbourNode] = currentNode
                neighbourNode.apply {
                    startDistance = newStartDistance
                    heuristicEndDistance = newStartDistance + calculateHeuristicEndDistance(this, goal)
                }
                if (neighbourNode !in open) {
                    open.add(neighbourNode)
                }
            }
        }
    }

    println("aw man")
}

private fun Map<Node, Node>.reconstructPath(goal: Node): List<Node> {
    var current = goal
    val finalPath = mutableListOf(current)
    while (current in this.keys) {
        current = this[current]!!
        finalPath.add(current)
    }
    return finalPath.reversed()
}

private fun List<List<Node>>.getNeighbours(node: Node): List<Node> {
    val neighbours = mutableListOf<Node>()

    this[node.row].getOrNull(node.column + 1)?.let { neighbours.add(it) }
    this[node.row].getOrNull(node.column - 1)?.let { neighbours.add(it) }
    this.getOrNull(node.row + 1)?.get(node.column)?.let { neighbours.add(it) }
    this.getOrNull(node.row - 1)?.get(node.column)?.let { neighbours.add(it) }

    return neighbours
}

private fun calculateHeuristicEndDistance(node: Node, goal: Node): Int {
    return goal.row - node.row + goal.column - node.column // eig abs
}