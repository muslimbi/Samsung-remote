package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.SamsungTVDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class SamsungTvDiscovery(private val context: Context) {

    private val tag = "SamsungTvDiscovery"

    fun discoverDevices(): Flow<List<SamsungTVDevice>> = flow {
        val discoveredList = mutableListOf<SamsungTVDevice>()

        // Always include virtual simulation device for instantaneous testing
        val virtualDevice = SamsungTVDevice(
            id = 9999,
            name = "Samsung Neo QLED 65\" (Virtual)",
            ipAddress = "192.168.1.105",
            macAddress = "E4:7C:F9:55:66:77",
            modelName = "QN95B 4K Smart TV",
            isLastConnected = true,
            isVirtual = true
        )
        discoveredList.add(virtualDevice)
        emit(discoveredList.toList())

        // Acquire MulticastLock for SSDP
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("SamRemoteSSDP")
        multicastLock?.setReferenceCounted(true)

        try {
            multicastLock?.acquire()
            val ssdpResults = runSsdpDiscovery()
            for (dev in ssdpResults) {
                if (discoveredList.none { it.ipAddress == dev.ipAddress }) {
                    discoveredList.add(dev)
                    emit(discoveredList.toList())
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "SSDP Discovery error", e)
        } finally {
            if (multicastLock?.isHeld == true) {
                multicastLock.release()
            }
        }

        // Subnet ping for open port 8001 / 8002
        try {
            val localIp = getLocalIpAddress(wifiManager)
            if (localIp != null) {
                val subnetPrefix = localIp.substringBeforeLast(".")
                val fastProbeIps = listOf(
                    "$subnetPrefix.100",
                    "$subnetPrefix.101",
                    "$subnetPrefix.102",
                    "$subnetPrefix.105",
                    "$subnetPrefix.110",
                    "$subnetPrefix.120",
                    "$subnetPrefix.150"
                )
                for (ip in fastProbeIps) {
                    if (isPortOpen(ip, 8001, 150)) {
                        val device = probeDeviceInfo(ip)
                        if (discoveredList.none { it.ipAddress == device.ipAddress }) {
                            discoveredList.add(device)
                            emit(discoveredList.toList())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Subnet scan error", e)
        }

        delay(500)
        emit(discoveredList.toList())
    }.flowOn(Dispatchers.IO)

    private suspend fun runSsdpDiscovery(): List<SamsungTVDevice> = withContext(Dispatchers.IO) {
        val found = mutableListOf<SamsungTVDevice>()
        val ssdpMessage = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: urn:samsung.com:device:RemoteControlReceiver:1\r\n\r\n"

        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2500

            val sendData = ssdpMessage.toByteArray()
            val sendPacket = DatagramPacket(
                sendData,
                sendData.size,
                InetAddress.getByName("239.255.255.250"),
                1900
            )
            socket.send(sendPacket)

            val recvBuffer = ByteArray(2048)
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2500) {
                try {
                    val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                    socket.receive(recvPacket)
                    val response = String(recvPacket.data, 0, recvPacket.length)
                    val ip = recvPacket.address.hostAddress
                    if (ip != null && found.none { it.ipAddress == ip }) {
                        val tv = probeDeviceInfo(ip)
                        found.add(tv)
                    }
                } catch (timeout: SocketTimeoutException) {
                    break
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "SSDP socket exception: ${e.message}")
        } finally {
            socket?.close()
        }
        found
    }

    private suspend fun probeDeviceInfo(ip: String): SamsungTVDevice = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("http://$ip:8001/api/v2/")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 1500
            connection.readTimeout = 1500
            val stream = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(stream)
            val device = json.optJSONObject("device")
            val name = device?.optString("name") ?: "Samsung Smart TV"
            val model = device?.optString("modelName") ?: "Tizen TV"
            val mac = device?.optString("wifiMac") ?: ""

            SamsungTVDevice(
                name = name,
                ipAddress = ip,
                macAddress = mac,
                modelName = model,
                isVirtual = false
            )
        } catch (e: Exception) {
            SamsungTVDevice(
                name = "Samsung Smart TV ($ip)",
                ipAddress = ip,
                modelName = "Samsung Tizen TV",
                isVirtual = false
            )
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocalIpAddress(wifiManager: WifiManager?): String? {
        val ipInt = wifiManager?.connectionInfo?.ipAddress ?: return null
        if (ipInt == 0) return null
        return String.format(
            "%d.%d.%d.%d",
            ipInt and 0xff,
            ipInt shr 8 and 0xff,
            ipInt shr 16 and 0xff,
            ipInt shr 24 and 0xff
        )
    }
}
