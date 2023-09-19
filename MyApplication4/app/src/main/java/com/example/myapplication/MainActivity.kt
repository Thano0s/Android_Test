package com.example.myapplication

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer.OnCompletionListener
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.views.overlay.Marker

data class GeocodingResult(
    val lat: Double, val lon: Double, val display_name: String
)

interface NominatimService {
    @GET("search")
    fun searchLocation(
        @Query("q") query: String, @Query("format") format: String = "json"
    ): Call<List<GeocodingResult>>
}

open class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val STORAGE_PERMISSION_REQUEST_CODE = 101
    private lateinit var mapController: IMapController;
    private lateinit var mapView: MapView;
    private lateinit var mGpsMyLocationProvider: GpsMyLocationProvider;
    private lateinit var mLocationOverlay: MyLocationNewOverlay;
    private lateinit var icon: Bitmap;
    private lateinit var searchBar: EditText;
    private lateinit var retrofit: Retrofit;
    private lateinit var nominatimService: NominatimService;
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var currentLocation: Button
    private val REQUEST_ENABLE_LOCATION = 123
    private lateinit var searchBtn: Button;
    private lateinit var getAll: Button;
    private var Latitude: Double = 0.0;
    private var Longitude: Double = 0.0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance().load(
            applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView = findViewById(R.id.mapView);
        searchBar = findViewById(R.id.search_bar)
        currentLocation = findViewById(R.id.location)
        searchBtn = findViewById(R.id.searchBtn);
        getAll = findViewById(R.id.all);

        if (!hasLocationPermission()) requestLocationPermission();
        if (!hasStoragePermission()) requestStoragePermission();

        val retrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        nominatimService = retrofit.create(NominatimService::class.java)

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapController = mapView.controller
        mapView.setBuiltInZoomControls(false); //Map ZoomIn/ZoomOut Button Visibility
        mapView.setMultiTouchControls(true);
        mapController.zoomTo(5, 3);
        currentLocation.setOnClickListener {
            getCurrentLocation();
        }
        getAll.setOnClickListener {
            getLocations()
        }
        searchBtn.setOnClickListener {
            searchQuery(searchBar.text.toString());
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ), LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    private fun searchQuery(searchQuery: String) {

        val call = nominatimService.searchLocation(searchQuery)
        call.enqueue(object : Callback<List<GeocodingResult>> {
            override fun onResponse(
                call: Call<List<GeocodingResult>>, response: Response<List<GeocodingResult>>
            ) {
                toast("Yes");
                if (response.isSuccessful) {
                    val results = response.body()
                    if (results != null && results.isNotEmpty()) {
                        // Use the first result (or display multiple results to the user)
                        val result = results[0]
                        addMarker(result.lat, result.lon)
                    } else {
                        // Handle no results found
                        toast("No results found");
                    }
                } else {
                    // Handle HTTP error
                    toast("HTTP error");
                }
            }

            override fun onFailure(call: Call<List<GeocodingResult>>, t: Throwable) {
                // Handle network or other errors
                toast("something went wrong")
            }
        })
    }

    private fun addMarker(latitude: Double, longitude: Double) {

        val geoPoint = GeoPoint(latitude, longitude)
        val mapController = mapView.controller
        mapController.setCenter(geoPoint)
        val overlayItem = OverlayItem("Marker Title", "Marker Snippet", geoPoint)
        // Optionally, you can zoom to a specific zoom level
        val zoomLevel = 9 // Adjust the zoom level as needed
        mapController.zoomTo(zoomLevel)
        val markerOverlay = ItemizedIconOverlay<OverlayItem>(
            applicationContext,
            mutableListOf(overlayItem), // You can add multiple OverlayItems to display multiple markers
            null // Default marker icon (you can customize this)
        )
        mapView.overlays.add(markerOverlay)
        // Invalidate the map view to update the display
        mapView.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 110) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!hasLocationPermission()) requestLocationPermission()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


//
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Create a FusedLocationProviderClient
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)

            // Get the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Handle the last known location here
                    if (location != null) {
                        this.Latitude = location.latitude.toDouble();
                        this.Longitude = location.longitude.toDouble();
                        // Use the latitude and longitude as needed
                        addMarker(Latitude, Longitude)
                    } else toast("location is null")

                }
                .addOnFailureListener { e ->
                    // Handle any errors that occurred while getting the location
                }
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                12
            )
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_ENABLE_LOCATION) {
//            if (resultCode == Activity.RESULT_OK) {
//                // User has enabled location services. You can now proceed with location-related tasks.
//                toast("access grant")
//            } else {
//                // User did not enable location services. Handle this case as needed.
//                toast("access denied")
//            }
//        }
//    }
    private lateinit var db: FirebaseFirestore;
    private var locations: ArrayList<Pair<Double, Double>> = ArrayList<Pair<Double, Double>>();
    private fun getLocations() {
//        println(this.Latitude);
//        println(this.Longitude);
        db = Firebase.firestore;
        db.collection("Locations")
            .get()
            .addOnSuccessListener { result ->
                for(document in result)
                {
                    locations.add(Pair(document.data["latitude"].toString().toDouble(),document.data["longitude"].toString().toDouble()));
                }
            }
            .addOnFailureListener { exception ->
//                    toast(exception.toString());
                Log.d("LocResult Failed",exception.toString());
            }
            .addOnCompleteListener{
                println(locations.size)
                for(coordinates in locations)
                {
                    println(coordinates.first);
                    println(coordinates.second);
                }
            }
//        if(locations.size == 0) return;
    }
    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
