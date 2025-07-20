/*
 * WebWake Droid
 *
 * Copyright (C) 2025 Zynkia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package zynkia.webwakedroid

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zynkia.webwakedroid.service.KtorService
import zynkia.webwakedroid.service.LogService
import zynkia.webwakedroid.service.StorageService
import zynkia.webwakedroid.service.UpdateService
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var updateService: UpdateService

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(KtorService.EXTRA_STATUS)
            updateStatus(status)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        updateService = UpdateService(applicationContext)
        checkForUpdates()

        // Start the Ktor service
        Intent(this, KtorService::class.java).also { intent ->
            startService(intent)
        }

        val storageService = StorageService(applicationContext)
        val port = storageService.getString("http_port")?.toIntOrNull() ?: 8080

        statusText = findViewById(R.id.status_text)
        val openBrowserButton: Button = findViewById(R.id.open_browser_button)
        openBrowserButton.setOnClickListener {
            val url = "http://127.0.0.1:$port"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        val ipv4AddressText: TextView = findViewById(R.id.ipv4_address_text)
        val ipv4Address = getIPv4Address()
        if (ipv4Address != null) {
            ipv4AddressText.text = "http://$ipv4Address:$port"
        } else {
            ipv4AddressText.text = getString(R.string.cannot_get_ipv4)
        }

        val ipv6AddressText: TextView = findViewById(R.id.ipv6_address_text)
        val ipv6Address = getIPv6Address()
        if (ipv6Address != null) {
            ipv6AddressText.text = "http://[$ipv6Address]:$port"
        } else {
            ipv6AddressText.text = getString(R.string.cannot_get_ipv6)
        }
        statusText.text = getString(R.string.status_unknown)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(KtorService.ACTION_STATUS_UPDATE)
        registerReceiver(statusReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(statusReceiver)
    }

    private fun updateStatus(status: String?) {
        statusText.text = when (status) {
            KtorService.STATUS_RUNNING -> getString(R.string.service_running)
            KtorService.STATUS_STOPPED -> getString(R.string.service_stopped)
            KtorService.STATUS_ERROR -> getString(R.string.service_error)
            else -> getString(R.string.status_unknown)
        }
    }

    private fun getIPv4Address(): String? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is java.net.Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getIPv6Address(): String? {
        try {
            var linkLocalAddress: String? = null
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is java.net.Inet6Address) {
                        val hostAddress = inetAddress.hostAddress
                        val addressWithoutScope = hostAddress.let {
                            val scopeIndex = it.indexOf('%')
                            if (scopeIndex > 0) it.substring(0, scopeIndex) else it
                        }

                        if (!inetAddress.isLinkLocalAddress) {
                            // Prefer global address
                            return addressWithoutScope
                        } else if (linkLocalAddress == null) {
                            // Store the first link-local address found
                            linkLocalAddress = addressWithoutScope
                        }
                    }
                }
            }
            // Return the link-local address if no global address was found
            return linkLocalAddress
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            val latestVersion = updateService.getLatestVersion()
            if (updateService.isUpdateAvailable(latestVersion)) {
                LogService.info("MainActivity", "New version available: $latestVersion")
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Update Available")
                        .setMessage("A new version of WebWake Droid is available: $latestVersion")
                        .setPositiveButton("Download") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://github.com/Zynkia/WebWake-Droid/releases/latest")
                            startActivity(intent)
                        }
                        .setNegativeButton("Later", null)
                        .show()
                }
            }
        }
    }
}
