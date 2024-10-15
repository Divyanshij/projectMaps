package com.example.collegeproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.collegeproject.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import okhttp3.*
import okio.IOException
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val API_KEY = "AIzaSyD-VYTMsJjvxeFw2B0m83aMZaz6fX_NuIk"
    private lateinit var petrolPumpNameTextView: TextView
    private lateinit var petrolPumpLocationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        petrolPumpNameTextView = findViewById(R.id.petrolname)
        petrolPumpLocationTextView = findViewById(R.id.locaton)
       setContentView(R.layout.activity_maps)
        val deliveryButton = findViewById<Button>(R.id.delivery)
        deliveryButton.setOnClickListener(){
            val deliveryIntent = Intent(this,deliveryActivity::class.java)
            startActivity(deliveryIntent)
            finish()
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        deliveryButton.setOnClickListener {
            // Collect the petrol pump name and location
            val stationName = petrolPumpNameTextView.text.toString() // Get the name from the TextView
            val stationLocation = petrolPumpLocationTextView.text.toString() // Get the location from the TextView

            // Create an intent to pass the data to DeliveryActivity.kt
            val deliveryIntent = Intent(this, deliveryActivity::class.java).apply {
                putExtra("station_name", stationName)
                putExtra("station_location", stationLocation)
            }

            startActivity(deliveryIntent)
            finish() // Optional: If you want to close MapsActivity
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val json = applicationContext.resources.openRawResource(R.raw.map_style)
                .bufferedReader().use { it.readText() }
            mMap.setMapStyle(MapStyleOptions(json))
        } catch (e: Resources.NotFoundException) {
            Log.e("MapActivity", "Can't find style. Error: ", e)
        } catch (e: Exception) {
            Log.e("MapActivity", "Failed to set map style. Error: ", e)
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true // Enable the blue dot (current location indicator)
            getCurrentLocation() // Get the current location and move the camera
        } else {
            // Request location permission if not already granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Method to get the user's current location
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Get the latitude and longitude
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Move the map's camera to the user's current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Optionally, add a marker for the current location
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                    getNearbyPetrolPumps(location)
                }
            }
    }

    private fun getNearbyPetrolPumps(location: Location) {
        val placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${location.latitude},${location.longitude}" +
                "&radius=5000&type=gas_station&key=AIzaSyD-VYTMsJjvxeFw2B0m83aMZaz6fX_NuIk"

        val client = OkHttpClient()
        val request = Request.Builder().url(placesUrl).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        // Parse the JSON response and add markers for each petrol station
                        val jsonResponse = JSONObject(responseData)
                        val results = jsonResponse.getJSONArray("results")
                        runOnUiThread {
                            petrolPumpNameTextView.text = ""
                            petrolPumpLocationTextView.text = ""
                        }
                        for (i in 0 until results.length()) {
                            val place = results.getJSONObject(i)
                            val lat = place.getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lat")
                            val lng = place.getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lng")
                            val name = place.getString("name")

                            runOnUiThread {
                                // Add a marker for each petrol station
                                val petrolStationLatLng = LatLng(lat, lng)
                                mMap.addMarker(
                                    MarkerOptions().position(petrolStationLatLng).title(name).icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    )
                                )
                                runOnUiThread {
                                    // Add a marker for each petrol station
                                    val petrolStationLatLng = LatLng(lat, lng)
                                    mMap.addMarker(
                                        MarkerOptions().position(petrolStationLatLng).title(name)
                                    )

                                    // Update the TextViews with the name and location
                                    petrolPumpNameTextView.append("$name\n")
                                    petrolPumpLocationTextView.append("Location: $lat, $lng\n\n")
                                }
                            }
                        }
                    }
                }
            }
        })

    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    getCurrentLocation() // Get the location if permission is granted
                }
            }
        }
    }
}