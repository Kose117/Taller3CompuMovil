package com.example.taller3compumovil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller3compumovil.databinding.ActivityMapsPairBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import models.user.defaultResponse
import models.user.locationRequest
import network.EchoWebSocketListener
import network.RetrofitClient
import network.WebSocketClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapsPairActivity : AppCompatActivity(),  OnMapReadyCallback, SensorEventListener {
    private lateinit var map: GoogleMap
    private lateinit var locationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private var parterLocationMarker: Marker? = null
    private var lastLocation: Location? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isFirstUpdate = true

    private lateinit var webSocketClient: WebSocketClient

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private val umbralBajo = 50f
    private var lat: String? = null
    private var long: String? = null
    private var id: String? = null

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback { isGranted ->
            if (isGranted) startLocationUpdates()
            else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        })


    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsPairBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstUpdate = true
        binding = ActivityMapsPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        setupMap()
        permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        binding.posicionar.setOnClickListener {
            currentLocationMarker?.position?.let { pos -> moveMarkerToLocation(pos) }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    lastLocation = it
                    updateLocationUI(it)
                }
            }
        }

        id = intent.getStringExtra("id")
        Log.i("USER ID", id.toString())
        webSocketClient = WebSocketClient("ws://ws0nr9l7-8080.use2.devtunnels.ms/api/user/ws/${id.toString()}", EchoWebSocketListener(applicationContext))

        setupSensor()
        createLocationRequest()
    }

    override fun onResume() {
        super.onResume()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    lastLocation = it
                    updateLocationUI(it)
                }
            }
        }
        id = intent.getStringExtra("id")
        webSocketClient = WebSocketClient("ws://ws0nr9l7-8080.use2.devtunnels.ms/api/user/ws/${id.toString()}", EchoWebSocketListener(applicationContext))
        setupSensor()
        createLocationRequest()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
        val sharedPref = applicationContext.getSharedPreferences("miPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear() // Borrar todas las preferencias compartidas
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
        val sharedPref = applicationContext.getSharedPreferences("miPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear() // Borrar todas las preferencias compartidas
        editor.apply()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startLocationUpdates()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        currentLocationMarker?.remove()
        currentLocationMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        if (isFirstUpdate) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            isFirstUpdate = false
        }

        val location = locationRequest(location.latitude.toString(), location.longitude.toString())

        RetrofitClient.create(applicationContext).updateUserLocation(location).enqueue(object :
            Callback<defaultResponse> {
            override fun onResponse(call: Call<defaultResponse>, response: Response<defaultResponse>){
                if(response.isSuccessful){
                    Log.i("USER LOCATION PAIRS", "updated sucesfully")
                    val sharedPref = applicationContext.getSharedPreferences("miPref", Context.MODE_PRIVATE)
                    lat = sharedPref.getString("latitude", null)
                    long = sharedPref.getString("longitude", null)
                    if (lat != null && long != null) {
                        Log.i("POSICION MAPS", "$lat + $long")

                        val posicion = LatLng(lat!!.toDouble(), long!!.toDouble())

                        parterLocationMarker?.remove()
                        parterLocationMarker = map.addMarker(
                            MarkerOptions()
                                .position(posicion)
                                .title("Partner Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )

                        calcularDistancia(location.latitude.toString(),location.longitude.toString(),lat!!,long!!)
                    } else {
                        Log.e("POSICION MAPS", "No se encontraron datos de latitud y longitud en SharedPreferences")
                    }

                }else{
                    Toast.makeText(this@MapsPairActivity, "couldn't update location", Toast.LENGTH_SHORT).show()
                    Log.i("USER LOCATION PAIRS", "couldn't update location")
                }
            }

            override fun onFailure(call: Call<defaultResponse>, t: Throwable) {
                Log.i("USER LOCATION PAIRS", "Error en la conexión")
                Toast.makeText(this@MapsPairActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun moveMarkerToLocation(latLng: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (::map.isInitialized && event.sensor.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            if (lightValue < umbralBajo) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_night))
            } else {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_day))
            }
        }
    }

    private fun setupSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    private fun calcularDistancia(lat1:String, long1:String, lat2:String, long2:String){
        val radioTierra = 6371 // Radio de la Tierra en kilómetros
        val dLat = Math.toRadians(lat2.toDouble() - lat1.toDouble())
        val dLon = Math.toRadians(long2.toDouble() - long1.toDouble())
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1.toDouble())) * cos(Math.toRadians(lat2.toDouble())) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = radioTierra * c

        val texto = String.format("%.${1}f", distancia)

        binding.distancia.text = texto + " Kilometros"
    }

}



