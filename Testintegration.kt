import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var graph: Graph

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_maps)

    // Initialize the graph
    graph = Graph()

    // Request location permission
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    } else {
        initializeMap()
        addVerticesAndEdges()
    }
}

private fun addVerticesAndEdges() {
    // Add vertices
    val vertexA = graph.addVertex("A", 37.7749, -122.4194) // Example vertex with name "A" and coordinates (37.7749, -122.4194)
    val vertexB = graph.addVertex("B", 37.3352, -121.8811) // Example vertex with name "B" and coordinates (37.3352, -121.8811)

    // Add edges
    graph.addEdge(vertexA, vertexB, 10) // Example edge from vertex A to vertex B with weight 10
    // Add more vertices and edges as needed
}

    private fun initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        // Called when the map is ready to be used
        googleMap = map
        googleMap.isMyLocationEnabled = true

        // Set a listener to handle map clicks
        googleMap.setOnMapClickListener { latLng ->
            val clickedVertex = findClosestVertex(latLng)

            if (clickedVertex != null) {
                val startPoint = graph.getVertex("A") // Replace with actual start vertex
                val shortestPath = dijkstraAlgorithm(startPoint, clickedVertex)

                if (shortestPath.isNotEmpty()) {
                    drawPathOnMap(shortestPath)
                    showToast("Shortest path from ${startPoint.name} to ${clickedVertex.name}: $shortestPath")
                } else {
                    showToast("No path found")
                }
            }
        }
    }

    private fun findClosestVertex(latLng: LatLng): Graph.Vertex? {
        var closestVertex: Graph.Vertex? = null
        var shortestDistance = Double.MAX_VALUE

        for (vertex in graph.getVertices()) {
            val vertexLatLng = LatLng(vertex.latitude, vertex.longitude)
            val distance = calculateDistance(latLng, vertexLatLng)

// Iterate through all vertices in the graph to find the closest vertex to the clicked location
 // Update the closest vertex and shortest distance if a closer vertex is found
            if (distance < shortestDistance) {
                shortestDistance = distance
                closestVertex = vertex
            }
        }

        return closestVertex
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val earthRadius = 6371 // Radius of the Earth in kilometers

        val latDistance = Math.toRadians(latLng2.latitude - latLng1.latitude)
        val lngDistance = Math.toRadians(latLng2.longitude - latLng1.longitude)

// Haversine formula to calculate the great-circle distance between two points on the Earth's surface
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)))

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    private fun dijkstraAlgorithm(startVertex: Graph.Vertex, endVertex: Graph.Vertex): List<Graph.Vertex> {
        val distances = mutableMapOf<Graph.Vertex, Double>()
        val previous = mutableMapOf<Graph.Vertex, Graph.Vertex>()
        val unvisited = mutableSetOf<Graph.Vertex>()

        graph.getVertices().forEach { vertex ->
            distances[vertex] = Double.MAX_VALUE
            previous[vertex] = null
            unvisited.add(vertex)
        }

        distances[startVertex] = 0.0

        while (unvisited.isNotEmpty()) {
            val currentVertex = unvisited.minByOrNull { distances[it]!! }

            if (currentVertex == endVertex) {
                break
            }

            unvisited.remove(currentVertex)

            graph.getNeighbors(currentVertex!!).forEach { neighbor ->
                val edgeWeight = graph.getEdgeWeight(currentVertex, neighbor)
                val newDistance = distances[currentVertex]!! + edgeWeight

                if (newDistance < distances[neighbor]!!) {
                    distances[neighbor] = newDistance
                    previous[neighbor] = currentVertex
                }
            }
        }

        val shortestPath = mutableListOf<Graph.Vertex>()
        var currentVertex = endVertex

        while (currentVertex != null) {
            shortestPath.add(0, currentVertex)
            currentVertex = previous[currentVertex]
        }

        return shortestPath
    }

    private fun drawPathOnMap(path: List<Graph.Vertex>) {
        googleMap.clear()

        val latLngList = mutableListOf<LatLng>()

        path.forEach { vertex ->
            val latLng = LatLng(vertex.latitude, vertex.longitude)
            latLngList.add(latLng)

            googleMap.addMarker(MarkerOptions().position(latLng).title(vertex.name))
        }

        googleMap.addPolyline(
            PolylineOptions()
                .addAll(latLngList)
                .width(5f)
                .color(Color.RED)
        )

        val startPoint = latLngList.first()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 12f))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initializeMap()
        } else {
        // Handle the result of the location permission request
            showToast("Location permission denied")
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}