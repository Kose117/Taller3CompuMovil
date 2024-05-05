package com.example.taller3compumovil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3compumovil.adapters.ActivosAdapter
import com.example.taller3compumovil.databinding.ActivityDisponiblesBinding
import models.availabilityResponse
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DisponiblesActivity : AppCompatActivity() {
    private lateinit var adapter: ActivosAdapter
    private lateinit var binding: ActivityDisponiblesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        adapter = ActivosAdapter(this, null, 0)
        binding.listaActivos.adapter = adapter
        loadUsuarios()
    }

    private fun loadUsuarios() {
        RetrofitClient.create(applicationContext).getAvailableUsers().enqueue(object : Callback<availabilityResponse> {
            override fun onResponse(
                call: Call<availabilityResponse>,
                response: Response<availabilityResponse>
            ) {
                if(response.isSuccessful){
                    val response = response.body()
                    Log.i("API RESPONSE", response.toString())
                }else{
                    Toast.makeText(this@DisponiblesActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<availabilityResponse>, t: Throwable) {
                Toast.makeText(this@DisponiblesActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })

        val cursor = null //eto borrenlo cuando pongan lo otro
        //val cursor = contentResolver.query(/* URI de tu base de datos */, null, "active = 1", null, null)
        if (cursor != null) {
            adapter.swapCursor(cursor)
        } else {
            // Manejar error de carga de datos
        }
    }
}
