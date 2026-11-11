package com.energyex.app.data.websocket

import android.util.Log
import com.energyex.app.data.model.DashboardSummary
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class EnergyWebSocketClient(private val wsUrl: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _telemetryEvents = MutableSharedFlow<DashboardSummary>(replay = 1)
    val telemetryEvents: SharedFlow<DashboardSummary> = _telemetryEvents

    fun connect() {
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("EnergyWebSocket", "Connected to EnergyEx Live WebSocket: $wsUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val summary = gson.fromJson(text, DashboardSummary::class.java)
                    _telemetryEvents.tryEmit(summary)
                } catch (e: Exception) {
                    Log.d("EnergyWebSocket", "Frame parse: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.d("EnergyWebSocket", "WebSocket connection note: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("EnergyWebSocket", "WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}

