package com.example.query

import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Location
import com.google.android.gms.location.LocationRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import android.Manifest
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.QuerySnapshot
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

//import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var btn: Button;
    private var currentLatitude: Double = 0.0;
    private var currentLongitude: Double = 0.0;
    private lateinit var textview: TextView;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById(R.id.getData)
        textview = findViewById(R.id.editText);

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationsRef = db.collection("Locations")

//        textview.text = currentLatitude.toString() + "\n" + currentLongitude.toString()
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.invalidate()

        btn.setOnClickListener {
            //Fix this
            getCurrentLocation()
            getNearByLocations()
//            openGoogleMapsWithDirections(0.0, 0.0)
//            val latitudeQuery =
//                locationsRef.whereGreaterThanOrEqualTo("latitude", currentLatitude - 0.006)
//                    .whereLessThanOrEqualTo("latitude", currentLatitude + 0.006)
//
//            val longitudeQuery =
//                locationsRef.whereGreaterThanOrEqualTo("longitude", currentLongitude - 0.006)
//                    .whereLessThanOrEqualTo("longitude", currentLongitude + 0.006)
//
//            // Create lists to store the results
//            val matchingLatitudeDocuments = mutableListOf<DocumentSnapshot>()
//            val matchingLongitudeDocuments = mutableListOf<DocumentSnapshot>()
//
//            latitudeQuery.get().addOnSuccessListener { latitudeQuerySnapshot ->
//                matchingLatitudeDocuments.addAll(latitudeQuerySnapshot.documents)
//
//                longitudeQuery.get().addOnSuccessListener { longitudeQuerySnapshot ->
//                    matchingLongitudeDocuments.addAll(longitudeQuerySnapshot.documents)
//
//                    // Find the intersection of documents that match both latitude and longitude conditions
//                    val matchingDocuments =
//                        matchingLatitudeDocuments.filter { latitudeDocument ->
//                            matchingLongitudeDocuments.any { longitudeDocument ->
//                                latitudeDocument.id == longitudeDocument.id
//                            }
//                        }
//
//                    // Process the matching documents as needed
//                    if (matchingDocuments.isEmpty())
//                        toast("No mess found")
//                    for (document in matchingDocuments) {
//                        val latitude = document.getDouble("latitude")!!
//                        val longitude = document.getDouble("longitude")!!
//                        addMarker(latitude, longitude)
//                    }
//
//                }
//            }
//        }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        requestLocationPermission()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnCompleteListener { task ->
            try {
                val response: LocationSettingsResponse = task.getResult(ApiException::class.java)
                // All location settings are satisfied. You can request location updates here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        // Location settings are not satisfied. Show a dialog to the user to enable location services.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            exception.status.startResolutionForResult(this, 101)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Location settings are not satisfied, and there's no way to change them. Handle this case as needed.
                    }
                }
            }
        }
        fusedLocationClient.lastLocation
            .addOnCompleteListener{
                if(it != null)
                {
                    currentLatitude = it.result.latitude.toString().toDouble();
                    currentLongitude = it.result.longitude.toString().toDouble();
                }
            }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }


    private fun addMarker(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        val mapController = mapView.controller
        mapController.setCenter(geoPoint)
        val overlayItem = OverlayItem("Marker Title", "Marker Snippet", geoPoint)
        // Optionally, you can zoom to a specific zoom level
        val zoomLevel = 9 // Adjust the zoom level as needed
        mapController.zoomTo(zoomLevel)
        val markerOverlay = ItemizedIconOverlay(
            applicationContext,
            mutableListOf(overlayItem), // You can add multiple OverlayItems to display multiple markers
            null // Default marker icon (you can customize this)
        )
        mapView.overlays.add(markerOverlay)
        // Invalidate the map view to update the display
        mapView.invalidate()
    }

    private fun openGoogleMapsWithDirections(destinationLat: Double, destinationLng: Double) {
//        val origin = "${location.latitude},${location.longitude}" // You can specify your current location or start location here
//        val destination = "$destinationLat,$destinationLng"

        // Create a URI with the destination coordinates
        val uri =
            Uri.parse("https://www.google.com/maps/place/18.48727427973568,73.79879735921273/")

        // Create an Intent to open Google Maps with the specified URI
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps") // Specify Google Maps package

        // Check if Google Maps is installed on the device
        if (mapIntent.resolveActivity(packageManager) != null) {
            // Start Google Maps with directions
            startActivity(mapIntent)
        } else {
            // Handle the case where Google Maps is not installed
            // You can prompt the user to install it or provide an alternative action
            toast("error")
        }
    }


    private fun getNearByLocations() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val placesRef = firebaseDatabase.getReference("Locations")

//        val currentLatitude = 12.3456 // Replace with the user's current latitude
//        val currentLongitude = 45.6789 // Replace with the user's current longitude
        val radiusInKm = 1.0 // One kilometer radius

        // Calculate latitude and longitude bounds for the query
        val latDelta = 0.009
        val lonDelta = 0.009 / cos(Math.toRadians(currentLatitude))
        val minLat = currentLatitude - latDelta * radiusInKm
        val maxLat = currentLatitude + latDelta * radiusInKm
        val minLon = currentLongitude - lonDelta * radiusInKm
        val maxLon = currentLongitude + lonDelta * radiusInKm
        println("minLat: " + minLat)
        println("maxLat: " + maxLat)
        // Query the database for places within the specified bounds
        placesRef.orderByChild("latitude")
//            .startAt(0.0)
//            .endAt(10.0)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
        println("I'm here");
                    // Iterate through the dataSnapshot to retrieve nearby places
                    for (placeSnapshot in dataSnapshot.children) {
                        val placeLat: Double = placeSnapshot.child("latitude").toString().toDouble()
                        val placeLon: Double =
                            placeSnapshot.child("longitude").toString().toDouble()

                        // Check if the place is within the radius
                        val distance = calculateDistance(
                            currentLatitude, currentLongitude,
                            placeLat ?: 0.0, placeLon ?: 0.0
                        )
                        if (distance <= radiusInKm) {
                            // This place is within the radius, you can use it
                            // Add it to a list or display it as needed
                            addMarker(placeLat, placeLon)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database errors

                }
            })
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val radiusOfEarth = 6371 // Earth's radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radiusOfEarth * c
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}