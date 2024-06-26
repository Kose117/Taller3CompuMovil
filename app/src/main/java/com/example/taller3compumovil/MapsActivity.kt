package com.example.taller3compumovil

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller3compumovil.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import models.availabilityState
import models.availabilityStateRequest
import models.user.defaultResponse
import models.user.locationRequest
import models.user.uploadImageResponse
import network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import util.bitmapToFile


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
    private lateinit var map: GoogleMap
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private var currentLocationMarker: Marker? = null
    private var lastLocation: Location? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isFirstUpdate = true

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private val umbralBajo = 50f

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback { isGranted ->
            if (isGranted) startLocationUpdates()
            else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        isFirstUpdate = true
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token_jwt", null)
        if(token == null){
            val intent = Intent(baseContext, LoginActivity::class.java)
            startActivity(intent)
        }else{
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

            val imagen = intent.getStringExtra("imagen")

            if(imagen != null){
                Log.i("ENTER METHOD", "IMAGE")
                val bitmap = BitmapFactory.decodeFile(imagen)

                val file = bitmapToFile(applicationContext, bitmap, "profile_picture")
                val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                RetrofitClient.create(applicationContext).uploadImage(body).enqueue(object : Callback <uploadImageResponse>{
                    override fun onResponse(
                        call: Call<uploadImageResponse>,
                        response: Response<uploadImageResponse>
                    ) {
                        if(response.isSuccessful){
                            Log.i("UPLOAD IMAGE", "Image uploaded sucesfully")
                        }else{
                            Log.i("UPLOAD IMAGE", "error uploading the picture")
                        }
                    }

                    override fun onFailure(call: Call<uploadImageResponse>, t: Throwable) {
                        Toast.makeText(this@MapsActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
                    }
                })

                Log.i("IMG", imagen)
                Log.i("IMG", "La imagen SI llego")
            } else {
                Log.i("IMG", "La imagen NO llego")
            }

            setupSensor()
            createLocationRequest()
            BotonDisponibles()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun BotonDisponibles() {
        binding.listar.setOnClickListener {
            val intent = Intent(this, DisponiblesActivity::class.java)
            startActivity(intent)
        }

        binding.cambiarEstado.setOnClickListener {
            changeAvailabilityState()
        }

        binding.cerrarSesion.setOnClickListener {
            borrarToken()
        }
    }

    private fun changeAvailabilityState () {
        val state = availabilityStateRequest(available = true)
        RetrofitClient.create(applicationContext).changeAvailability(state).enqueue(object : Callback<availabilityState> {
            override fun onResponse(
                call: Call<availabilityState>,
                response: Response<availabilityState>
            ) {
                if(response.isSuccessful){
                    val state = response.body()?.available
                    Log.i("AVAILABILITY STATE", state.toString())
                    Toast.makeText(this@MapsActivity, "Changed availability to " + state.toString(), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@MapsActivity, "Couldn't change availability, try again later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<availabilityState>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun borrarToken() {
        val sharedPreferences = getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("token_jwt")
            apply()
        }
        val intent = Intent(baseContext, OpcionesActivity::class.java)
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        drawSavedPath()
        startLocationUpdates()
    }


    private fun drawSavedPath() {
        val inputStream = resources.openRawResource(R.raw.locations)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val rootObject = JSONObject(jsonString)  // Crear un JSONObject desde el string
        val locationsArray = rootObject.getJSONArray("locationsArray")  // Acceder al JSONArray correcto

        for (i in 0 until locationsArray.length()) {
            val jsonObject = locationsArray.getJSONObject(i)
            val lat = jsonObject.getDouble("latitude")
            val lng = jsonObject.getDouble("longitude")
            val name = jsonObject.getString("name")  // Suponiendo que también quieras usar el nombre
            val latLng = LatLng(lat, lng)
            map.addMarker(MarkerOptions().position(latLng).title(name))  // Usar el nombre como título
        }
    }



    private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        currentLocationMarker?.remove()
        currentLocationMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
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
                    Log.i("USER LOCATION IN MAPS", "updated sucesfully")
                }else{
                    Log.i("USER LOCATION IN MAPS", "couldn't update location")
                    Toast.makeText(this@MapsActivity, "couldn't update location", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<defaultResponse>, t: Throwable) {
                Log.i("USER LOCATION IN MAPS", "Error en la conexion")
                Toast.makeText(this@MapsActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
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

    // Esto no toca implementarlo, pero tiene que estar si o si
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun setupSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}
