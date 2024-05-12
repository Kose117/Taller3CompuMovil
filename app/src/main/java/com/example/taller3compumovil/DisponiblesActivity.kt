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


    private val CHANNEL_ID = "channel_id"
    private val notificacionesid = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)

        binding.listaActivos.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.listaActivos.addItemDecoration(dividerItemDecoration)

        createNotificationChannel()

        adapter = ActivosAdapter(this,this)
        binding.listaActivos.adapter = adapter
        loadUsuarios()


    }

    override fun onButtonClick(user: User) {
        // Maneja el clic del elemento aquí
        intent = Intent(this, MapsPairActivity::class.java)
        intent.putExtra("id", user._id)
        startActivity(intent)
    }

    private fun loadUsuarios() {
        webSocketClient = WebSocketClient("ws://ws0nr9l7-8080.use2.devtunnels.ms/api/user/ws", EchoWebSocketListener2(applicationContext))
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

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Titulo"
            val descripcion = "Notificacion"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val channel =  NotificationChannel(CHANNEL_ID,name,importancia).apply {
                description = descripcion
            }
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(){

        val builder = NotificationCompat.Builder(this , CHANNEL_ID)
            .setSmallIcon(R.drawable.vaultboy)
            .setContentTitle("Ejemplo")
            .setContentText("Example")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)){
            notify(notificacionesid, builder.build())
        }

    }
}
