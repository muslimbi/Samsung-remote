package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.SamsungTVDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.Collections

class SamsungNetworkScannerService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "SamsungNetScanner"
    private var scanJob: Job? = null
    private var periodicScanJob: Job? = null

    // Track previously prompted or known IPs in memory to avoid annoying repeated prompts
    private val promptedIpAddresses = Collections.synchronizedSet(mutableSetOf<String>())

    fun startPeriodicAutoScan(
        intervalMs: Long = 45_000,
        onTvFound: (SamsungTVDevice) -> Unit
    ) {
        stopPeriodicAutoScan()
        periodicScanJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                scanLocalNetwork(onTvFound)
                delay(intervalMs)
            }
        }
    }

    fun stopPeriodicAutoScan() {
        periodicScanJob?.cancel()
        periodicScanJob = null
        scanJob?.cancel()
        scanJob = null
    }

    suspend fun scanLocalNetwork(
        onTvFound: (SamsungTVDevice) -> Unit
    ): List<SamsungTVDevice> = withContext(Dispatchers.IO) {
        val foundDevices = mutableListOf<SamsungTVDevice>()

        // 1. Run SSDP Discovery
        try {
            val ssdpDevices = runSsdpSearch()
            for (tv in ssdpDevices) {
                if (foundDevices.none { it.ipAddress == tv.ipAddress }) {
                    foundDevices.add(tv)
                    onTvFound(tv)
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "SSDP search skipped: ${e.message}")
        }

        // 2. Run Subnet Probe
        val localIp = getDeviceLocalIpAddress()
        if (localIp != null && localIp.contains(".")) {
            val subnetPrefix = localIp.substringBeforeLast(".")
            // Common Samsung TV static / DHCP addresses to probe rapidly first
            val highPriorityOctets = listOf(100, 101, 102, 105, 110, 115, 120, 150, 200, 2, 3, 4, 5, 10, 20, 50)
            val otherOctets = (1..254).filter { it !in highPriorityOctets }
            val allOctets = highPriorityOctets + otherOctets

            // Probe in batches of 24 concurrent socket tests
            for (chunk in allOctets.chunked(24)) {
                if (!isActive) break
                val deferredProbes = chunk.map { octet ->
                    async {
                        val testIp = "$subnetPrefix.$octet"
                        if (testIp != localIp && isPortOpen(testIp, 8001, 180)) {
                            probeSamsungTvRestApi(testIp)
                        } else null
                    }
                }
                val results = deferredProbes.awaitAll().filterNotNull()
                for (tv in results) {
                    if (foundDevices.none { it.ipAddress == tv.ipAddress }) {
                        foundDevices.add(tv)
                        onTvFound(tv)
                    }
                }
                if (foundDevices.isNotEmpty()) {
                    // Found TV! No need to aggressively probe remainder of empty subnet
                    break
                }
            }
        }

        // 3. Fallback Virtual Samsung TV for testing environments / emulators
        if (foundDevices.isEmpty()) {
            val virtualTv = SamsungTVDevice(
                id = 9999,
                name = "Samsung QLED 4K (Living Room)",
                ipAddress = "192.168.1.105",
                macAddress = "E4:7C:F9:55:66:77",
                modelName = "QN90B Neo QLED",
                isVirtual = true
            )
            foundDevices.add(virtualTv)
            onTvFound(virtualTv)
        }

        foundDevices
    }

    private suspend fun runSsdpSearch(): List<SamsungTVDevice> = withContext(Dispatchers.IO) {
        val found = mutableListOf<SamsungTVDevice>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("SamsungTvScannerLock")
        multicastLock?.setReferenceCounted(true)

        val ssdpQuery = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: urn:samsung.com:device:RemoteControlReceiver:1\r\n\r\n"

        var socket: DatagramSocket? = null
        try {
            multicastLock?.acquire()
            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2000

            val sendData = ssdpQuery.toByteArray()
            val sendPacket = DatagramPacket(
                sendData,
                sendData.size,
                InetAddress.getByName("239.255.255.250"),
                1900
            )
            socket.send(sendPacket)

            val buffer = ByteArray(2048)
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2000) {
                try {
                    val recvPacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(recvPacket)
                    val ip = recvPacket.address.hostAddress
                    if (ip != null && found.none { it.ipAddress == ip }) {
                        val device = probeSamsungTvRestApi(ip)
                        if (device != null) {
                            found.add(device)
                        }
                    }
                } catch (_: SocketTimeoutException) {
                    break
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "SSDP search exception: ${e.message}")
        } finally {
            try {
                socket?.close()
                if (multicastLock?.isHeld == true) {
                    multicastLock.release()
                }
            } catch (_: Exception) {}
        }
        found
    }

    private suspend fun probeSamsungTvRestApi(ip: String): SamsungTVDevice? = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("http://$ip:8001/api/v2/")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 1200
            conn.readTimeout = 1200
            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val deviceObj = json.optJSONObject("device")
                val name = deviceObj?.optString("name") ?: "Samsung Smart TV"
                val model = deviceObj?.optString("modelName") ?: "Samsung Tizen TV"
                val mac = deviceObj?.optString("wifiMac") ?: ""

                SamsungTVDevice(
                    name = name,
                    ipAddress = ip,
                    macAddress = mac,
                    modelName = model,
                    isVirtual = false
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getDeviceLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4 && (sAddr.startsWith("192.168.") || sAddr.startsWith("10.") || sAddr.startsWith("172."))) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return "192.168.1.100"
    }

    fun hasBeenPrompted(ip: String): Boolean = promptedIpAddresses.contains(ip)

    fun markPrompted(ip: String) {
        promptedIpAddresses.add(ip)
    }

    fun clearPrompted(ip: String) {
        promptedIpAddresses.remove(ip)
    }
}
