package network

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

class EchoWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i("WEB SOCKET CONNECTION", "FUCKKK")
        webSocket.send("Hello, it's me. I was wondering if after all these years you'd like to meet.")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Receiving : $text")
        Log.i("WEB SOCKET RECEIVING", text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Error : " + t.message)
    }
}
