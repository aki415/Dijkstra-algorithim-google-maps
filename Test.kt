import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    println("Enter the number of vertices:")
    val vertices = scanner.nextInt()

    val graph = Graph(vertices) // Create a graph with user-defined vertices

    println("Enter the number of edges:")
    val edges = scanner.nextInt()

    // Add edges between vertices. addEdge(vertex1, vertex2, edgeWeight)
    for (i in 1..edges) {
        println("Enter the start vertex, end vertex and the weight of edge $i:")
        val startVertex = scanner.nextInt()
        val endVertex = scanner.nextInt()
        val weight = scanner.nextInt()

        graph.addEdge(startVertex, endVertex, weight)
    }

    println("Enter the start node for Dijkstra's Algorithm:")
    val startNode = scanner.nextInt()

    val paths = graph.shortestPath(startNode)

    // Print the shortest paths from startNode to all other nodes
    for (i in 0 until vertices) {
        println("The shortest path from node $startNode to node $i is ${paths[i]}")
    }

    scanner.close()
}