
class Graph {
    //// Create an empty mutable map to store vertices
    private val vertices: MutableMap<String, Vertex> = mutableMapOf()
    private val edges: MutableMap<Vertex, MutableMap<Vertex, Int>> = mutableMapOf()
   // Declare a data class Vertex with properties name, latitude, and longitude
    data class Vertex(val name: String, val latitude: Double, val longitude: Double)
   // Function to add a new vertex to the graph
    fun addVertex(name: String, latitude: Double, longitude: Double) {
        vertices[name] = Vertex(name, latitude, longitude)
        edges[vertices[name]!!] = mutableMapOf()
    }
// Function to add an edge between two vertices
    fun addEdge(source: Vertex, destination: Vertex, weight: Int) {
        // Add the destination vertex and its weight to the source vertex's edges
        edges[source]?.put(destination, weight)
    }

    fun getVertex(name: String): Vertex? {
        return vertices[name]
    }

    // Function to return a vertex given its name
    fun getVertices(): List<Vertex> {
        return vertices.values.toList()
    }
    // Function to return a list of neighbors of a given vertex
    fun getNeighbors(vertex: Vertex): List<Vertex> {
        return edges[vertex]?.keys?.toList() ?: emptyList()
    }

    // Function to return the weight of an edge between two given vertices
    fun getEdgeWeight(source: Vertex, destination: Vertex): Int {
        return edges[source]?.get(destination) ?: 0
    }

    fun findShortestPath(startVertex: Vertex, endVertex: Vertex): List<Vertex> {
        val distances = mutableMapOf<Vertex, Int>()
        val previous = mutableMapOf<Vertex, Vertex>()
        // Create a set to store the unvisited vertices
        val unvisited = mutableSetOf<Vertex>()

        vertices.values.forEach { vertex ->
            if (vertex == startVertex) {
                distances[vertex] = 0
            } else {
                distances[vertex] = Int.MAX_VALUE
            }
            unvisited.add(vertex)
        }
       // Start the loop to find the shortest path
        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances[it] ?: Int.MAX_VALUE }
            if (current == null) break

            unvisited.remove(current)
            // Update the distances to neighboring vertices
            edges[current]?.forEach { (neighbor, weight) ->
                val alternative = distances[current]!! + weight
                if (alternative < distances[neighbor]!!) {
                    distances[neighbor] = alternative
                    previous[neighbor] = current
                }
            }
        }
        // Backtrack to find the shortest path
        val path = mutableListOf<Vertex>()
        var current = endVertex
        while (previous.containsKey(current)) {
            path.add(current)
            current = previous[current]!!
        }

        path.add(startVertex)
        return path.reversed()
    }
}