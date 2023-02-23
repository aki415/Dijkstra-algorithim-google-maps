class Graph {
    private val vertices: MutableMap<String, Vertex> = mutableMapOf()
    private val edges: MutableMap<Vertex, MutableMap<Vertex, Int>> = mutableMapOf()

    data class Vertex(val name: String, val latitude: Double, val longitude: Double)

    fun addVertex(name: String, latitude: Double, longitude: Double) {
        vertices[name] = Vertex(name, latitude, longitude)
        edges[vertices[name]!!] = mutableMapOf()
    }

    fun addEdge(source: Vertex, destination: Vertex, weight: Int) {
        edges[source]?.put(destination, weight)
    }

    fun getVertex(name: String): Vertex? {
        return vertices[name]
    }

    fun getVertices(): List<Vertex> {
        return vertices.values.toList()
    }

    fun getNeighbors(vertex: Vertex): List<Vertex> {
        return edges[vertex]?.keys?.toList() ?: emptyList()
    }

    fun getEdgeWeight(source: Vertex, destination: Vertex): Int {
        return edges[source]?.get(destination) ?: 0
    }

    fun findShortestPath(startVertex: Vertex, endVertex: Vertex): List<Vertex> {
        val distances = mutableMapOf<Vertex, Int>()
        val previous = mutableMapOf<Vertex, Vertex>()
        val unvisited = mutableSetOf<Vertex>()

        vertices.values.forEach { vertex ->
            if (vertex == startVertex) {
                distances[vertex] = 0
            } else {
                distances[vertex] = Int.MAX_VALUE
            }
            unvisited.add(vertex)
        }

        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances[it] ?: Int.MAX_VALUE }
            if (current == null) break

            unvisited.remove(current)

            edges[current]?.forEach { (neighbor, weight) ->
                val alternative = distances[current]!! + weight
                if (alternative < distances[neighbor]!!) {
                    distances[neighbor] = alternative
                    previous[neighbor] = current
                }
            }
        }

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