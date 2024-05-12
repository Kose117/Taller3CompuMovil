package network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taller3compumovil.MapsPairActivity
import com.example.taller3compumovil.R
import models.User
import okhttp3.*
import org.json.JSONObject

class EchoWebSocketListener2(context: Context) : WebSocketListener() {

    private val contexto = context

    private val CHANNEL_ID = "channel_id"
    private val notificacionesid = 101


    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i("WEB SOCKET CONNECTION", "FUCKKK")
        webSocket.send("Hello, it's me. I was wondering if after all these years you'd like to meet.")
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i("WEB SOCKET RECEIVING", text)

        val jsonObject = JSONObject(text)

        val nombre = jsonObject.getString("name")
        val apellido = jsonObject.getString("lastname")
        val id = jsonObject.getString("_id")
        val envio = "$nombre $apellido"

        createNotificationChannel()
        sendNotification(envio,id)

    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Error : " + t.message)
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Titulo"
            val descripcion = "Notificacion"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val channel =  NotificationChannel(CHANNEL_ID,name,importancia).apply {
                description = descripcion
            }
            val notificationManager : NotificationManager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(data: String, id :String){

        val intent = Intent(contexto,MapsPairActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("id",id)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(contexto,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE )

        val builder = NotificationCompat.Builder(contexto , CHANNEL_ID)
            .setSmallIcon(R.drawable.vaultboy)
            .setContentTitle("Se acaba de conectar")
            .setContentText(data)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(contexto)){
            notify(notificacionesid, builder.build())
        }

        Log.i("NOTIFICACION", "Notificacion desplegada")

    }

}
