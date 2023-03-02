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
        setupUIComponents()
        setupCustomMarkers()
        setupMapOverlays()
        setupErrorHandling()
        setupAdditionalDataStructures()
    }
}

private fun addVerticesAndEdges() {
    // Add vertices
    val vertexA = graph.addVertex("A", 37.7749, -122.4194) //  vertex with name "A" and coordinates (37.7749, -122.4194)
    val vertexB = graph.addVertex("B", 37.3352, -121.8811) // vertex with name "B" and coordinates (37.3352, -121.8811)

    // Add edges
    graph.addEdge(vertexA, vertexB, 10) // edge from vertex A to vertex B with weight 10
    
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
//Ui search places
    private fun setupSearchView() {
        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                performSearch(newText)
                return true
            }
        })
    }

    private fun setupUIComponents() {
        // Set up additional UI components and layouts

        // Add a button to the layout and set an onClickListener
        val button = findViewById<Button>(R.id.my_button)
        button.setOnClickListener {
            
        }
    }

    private fun setupCustomMarkers() {
        // Set up custom marker icons and styles
        // Customize the markers on the map with your own icons, colors, and labels

        //Create a custom marker icon
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.my_marker_icon)

        // Add a marker to the map with a custom icon
        val markerOptions = MarkerOptions()
            .position(LatLng(37.7749, -122.4194))
            .title("Custom Marker")
            .icon(markerIcon)

        googleMap.addMarker(markerOptions)
    }

    private fun setupMapOverlays() {
        // Set up custom map overlays or layers
       
        // polygon overlay in map
        val polygonOptions = PolygonOptions()
            .add(LatLng(37.7749, -122.4194))
            .add(LatLng(37.3352, -121.8811))
            .add(LatLng(37.422, -122.084))
            .strokeColor(Color.RED)
            .fillColor(Color.argb(100, 255, 0, 0))

        googleMap.addPolygon(polygonOptions)
    }

    private fun setupErrorHandling() {
          // Set up error handling and exception handling logic
    
          // Implement an error handler for network requests
          val errorHandler = ErrorHandler()
          errorHandler.setErrorListener { error ->
        // Handle the error
        // Display an error message to the user or perform appropriate actions
        Toast.makeText(this, "An error occurred: $error", Toast.LENGTH_SHORT).show()
    }
    
    //Use the error handler for network requests
    val apiClient = ApiClient()
    apiClient.setErrorHandler(errorHandler)
    apiClient.makeRequest()
     
    }

    private fun setupAdditionalDataStructures() {
    
        //use a data structure for storing map markers
        val markersList = mutableListOf<Marker>()

        //Add a marker to the list
        val marker = googleMap.addMarker(MarkerOptions().position(LatLng(37.7749, -122.4194)))
        markersList.add(marker)

        //Perform operations on the markers list
        markersList.forEach { marker ->
            // Access marker properties
            val position = marker.position
            val title = marker.title
            val snippet = marker.snippet

    // show an info window for a specific marker
    if (title == "Custom Marker") {
        marker.showInfoWindow()
        }
    }

//perform the actual search operation based on the query, using Google Places API.
    private fun searchPlaces(query: String) {
    val placesClient = Places.createClient(this)
    
    // Create a FindAutocompletePredictionsRequest with the query
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()
    
    // Call the findAutocompletePredictions method to get place predictions
    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            // Handle the successful response
            val predictions = response.autocompletePredictions
            
            // Process the predictions, e.g., display them on the map or in a list view
            
            for (prediction in predictions) {
                val placeId = prediction.placeId
                
                // Get place details for each prediction
                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
                
                placesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { fetchResponse ->
                        // Handle the successful fetch response
                        val place = fetchResponse.place
                        
                        val name = place.name
                        val latLng = place.latLng
                        
                        // Display the place details on the map or in a list view
                        //create a marker at the place's location
                        if (latLng != null) {
                            val markerOptions = MarkerOptions()
                                .position(latLng)
                                .title(name)
                                
                            googleMap.addMarker(markerOptions)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the fetch place failure
                        Log.e(TAG, "Failed to fetch place: ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            // Handle the findAutocompletePredictions failure
            Log.e(TAG, "Failed to search for places: ${exception.message}")
        }
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