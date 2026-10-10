package com.example.network

import android.util.Base64
import android.util.Log
import com.example.model.SamsungKey
import com.example.model.SamsungTVDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.TimeUnit

sealed class TvConnectionEvent {
    object Connecting : TvConnectionEvent()
    data class Connected(val token: String?) : TvConnectionEvent()
    data class Disconnected(val reason: String) : TvConnectionEvent()
    data class PairingRequired(val message: String) : TvConnectionEvent()
    data class Error(val message: String) : TvConnectionEvent()
    data class KeySent(val key: SamsungKey) : TvConnectionEvent()
}

class SamsungTvClient {

    private val tag = "SamsungTvClient"
    private var webSocket: WebSocket? = null
    private var activeDevice: SamsungTVDevice? = null

    private val _events = MutableSharedFlow<TvConnectionEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<TvConnectionEvent> = _events

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun connect(device: SamsungTVDevice) = withContext(Dispatchers.IO) {
        disconnect()
        activeDevice = device

        if (device.isVirtual) {
            _events.emit(TvConnectionEvent.Connected(null))
            return@withContext
        }

        _events.emit(TvConnectionEvent.Connecting)

        val appName = "SamRemote"
        val base64AppName = Base64.encodeToString(appName.toByteArray(), Base64.NO_WRAP)
        val tokenParam = if (!device.token.isNullOrEmpty()) "&token=${device.token}" else ""
        val protocol = if (device.port == 8002) "wss" else "ws"
        val wsUrl = "$protocol://${device.ipAddress}:${device.port}/api/v2/channels/samsung.remote.control?name=$base64AppName$tokenParam"

        try {
            val request = Request.Builder().url(wsUrl).build()
            webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(tag, "WebSocket Opened to ${device.ipAddress}")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(tag, "WebSocket Message received: $text")
                    try {
                        val json = JSONObject(text)
                        val event = json.optString("event")
                        if (event == "ms.channel.connect") {
                            val data = json.optJSONObject("data")
                            val token = data?.optString("token")
                            _events.tryEmit(TvConnectionEvent.Connected(token))
                        } else if (event == "ms.channel.unauthorized") {
                            _events.tryEmit(TvConnectionEvent.PairingRequired("Please accept connection prompt on your Samsung TV screen"))
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing message", e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(tag, "WebSocket Closing: $code $reason")
                    _events.tryEmit(TvConnectionEvent.Disconnected(reason))
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(tag, "WebSocket Failure: ${t.message}")
                    _events.tryEmit(TvConnectionEvent.Error(t.message ?: "Connection failed"))
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Failed to connect", e)
            _events.emit(TvConnectionEvent.Error(e.message ?: "Could not connect to TV"))
        }
    }

    suspend fun sendKey(key: SamsungKey): Boolean = withContext(Dispatchers.IO) {
        val current = activeDevice ?: return@withContext false

        if (current.isVirtual) {
            _events.emit(TvConnectionEvent.KeySent(key))
            return@withContext true
        }

        val ws = webSocket
        if (ws == null) {
            Log.w(tag, "Cannot send key: WebSocket is not open")
            return@withContext false
        }

        val json = JSONObject().apply {
            put("method", "ms.remote.control")
            put("params", JSONObject().apply {
                put("Cmd", "Click")
                put("DataOfCmd", key.keyCode)
                put("Option", "false")
                put("TypeOfRemote", "SendRemoteKey")
            })
        }

        val sent = ws.send(json.toString())
        if (sent) {
            _events.emit(TvConnectionEvent.KeySent(key))
        }
        sent
    }

    suspend fun sendWakeOnLan(macAddress: String): Boolean = withContext(Dispatchers.IO) {
        if (macAddress.isBlank()) return@withContext false
        try {
            val cleanMac = macAddress.replace(":", "").replace("-", "")
            if (cleanMac.length != 12) return@withContext false

            val macBytes = ByteArray(6)
            for (i in 0 until 6) {
                macBytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }

            // 6 bytes of 0xFF followed by 16 repetitions of MAC
            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0 until 6) {
                bytes[i] = 0xFF.toByte()
            }
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val address = InetAddress.getByName("255.255.255.255")
            val packet = DatagramPacket(bytes, bytes.size, address, 9)
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.send(packet)
            socket.close()
            Log.d(tag, "Wake-on-LAN magic packet sent to $macAddress")
            true
        } catch (e: Exception) {
            Log.e(tag, "Wake-on-LAN failed", e)
            false
        }
    }

    suspend fun checkTvInfo(ipAddress: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://$ipAddress:8001/api/v2/")
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun launchApp(appId: String, appName: String): Boolean = withContext(Dispatchers.IO) {
        val current = activeDevice ?: return@withContext false
        if (current.isVirtual) {
            return@withContext true
        }

        // Try REST application launch endpoint first
        try {
            val emptyBody = okhttp3.RequestBody.create(null, ByteArray(0))
            val request = Request.Builder()
                .url("http://${current.ipAddress}:${current.port}/api/v2/applications/$appId")
                .post(emptyBody)
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(tag, "REST launched app: $appName ($appId)")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.d(tag, "REST app launch fallback: ${e.message}")
        }

        // Fallback to WebSocket app launch event
        val ws = webSocket
        if (ws != null) {
            val json = JSONObject().apply {
                put("method", "ms.channel.emit")
                put("params", JSONObject().apply {
                    put("event", "ed.apps.launch")
                    put("to", "host")
                    put("data", JSONObject().apply {
                        put("action_type", "DEEP_LINK")
                        put("appId", appId)
                    })
                })
            }
            return@withContext ws.send(json.toString())
        }
        false
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
            webSocket = null
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting", e)
        }
    }
}
