package com.example.taller3compumovil

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taller3compumovil.adapters.ActivosAdapter
import com.example.taller3compumovil.databinding.ActivityDisponiblesBinding
import models.User
import models.availabilityResponse
import network.EchoWebSocketListener
import network.EchoWebSocketListener2
import network.RetrofitClient
import network.WebSocketClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class DisponiblesActivity : AppCompatActivity(), ActivosAdapter.OnButtonClickListener{
    private lateinit var adapter: ActivosAdapter
    private lateinit var binding: ActivityDisponiblesBinding
    private var lista: List<User>? = null
    private lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val layoutManager = LinearLayoutManager(this)

        binding.listaActivos.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.listaActivos.addItemDecoration(dividerItemDecoration)


        adapter = ActivosAdapter(this,this)
        binding.listaActivos.adapter = adapter

        webSocketClient = WebSocketClient("ws://ws0nr9l7-8080.use2.devtunnels.ms/api/user/ws", EchoWebSocketListener2(applicationContext))


        loadUsuarios()


    }

    override fun onResume() {
        super.onResume()
        webSocketClient = WebSocketClient("ws://ws0nr9l7-8080.use2.devtunnels.ms/api/user/ws", EchoWebSocketListener2(applicationContext))
        loadUsuarios()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }



    override fun onButtonClick(user: User) {
        // Maneja el clic del elemento aquí
        intent = Intent(this, MapsPairActivity::class.java)
        intent.putExtra("id", user._id)
        startActivity(intent)
    }

    private fun loadUsuarios() {
        RetrofitClient.create(applicationContext).getAvailableUsers().enqueue(object : Callback<availabilityResponse> {
            override fun onResponse(
                call: Call<availabilityResponse>,
                response: Response<availabilityResponse>
            ) {
                if(response.isSuccessful){
                    val respuesta = response.body()
                    lista = respuesta?.users

                    Log.i("API RESPONSE", respuesta.toString())
                    Log.i("LISTA", lista.toString())
                    adapter.setUsers(lista)


                }else{
                    Toast.makeText(this@DisponiblesActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<availabilityResponse>, t: Throwable) {
                Toast.makeText(this@DisponiblesActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })

    }

}
