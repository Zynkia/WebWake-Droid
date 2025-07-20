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
package zynkia.webwakedroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import zynkia.webwakedroid.R
import zynkia.webwakedroid.model.User
import zynkia.webwakedroid.server.mainModule
import zynkia.webwakedroid.util.Shell
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory

class KtorService : Service() {

    companion object {
        const val ACTION_STATUS_UPDATE = "zynkia.webwakedroid.service.STATUS_UPDATE"
        const val EXTRA_STATUS = "zynkia.webwakedroid.service.EXTRA_STATUS"
        const val STATUS_RUNNING = "RUNNING"
        const val STATUS_STOPPED = "STOPPED"
        const val STATUS_ERROR = "ERROR"
    }

    private lateinit var storageService: StorageService
    private lateinit var updateService: UpdateService

    private val server by lazy {
        val httpsEnabled = storageService.getString("https_enabled") == "true"
        val sslCert = storageService.getString("ssl_cert")
        val sslKey = storageService.getString("ssl_key")
        val port = storageService.getString("http_port")?.toIntOrNull() ?: 8080

        val module: Application.() -> Unit = {
            mainModule(storageService, updateService, ::restartTunnels)
            environment.monitor.subscribe(ApplicationStarted) {
                sendUpdateBroadcast(STATUS_RUNNING)
            }
            environment.monitor.subscribe(ApplicationStopped) {
                sendUpdateBroadcast(STATUS_STOPPED)
            }
        }

        if (httpsEnabled && !sslCert.isNullOrBlank() && !sslKey.isNullOrBlank()) {
            val keyStoreFile = File(filesDir, "keystore.jks")
            val keyStorePassword = "changeit".toCharArray()
            val keyAlias = "webwakedroid"

            // Create KeyStore
            val keyStore = KeyStore.getInstance("JKS")
            keyStore.load(null, null)
            // This is a simplified example. In a real app, you'd parse the cert and key
            // and add them to the keystore. For now, we assume they are in a format
            // that can be directly used. This part would need a more robust implementation.

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, keyStorePassword)

            val environment = applicationEngineEnvironment {
                sslConnector(
                    keyStore = keyStore,
                    keyAlias = keyAlias,
                    keyStorePassword = { keyStorePassword },
                    privateKeyPassword = { keyStorePassword }) {
                    this.port = 443
                    keyStorePath = keyStoreFile
                }
                connector {
                    this.port = port
                }
                module(module)
            }
            embeddedServer(CIO, environment = environment)
        } else {
            embeddedServer(CIO, port = port, module = module)
        }
    }

    override fun onCreate() {
        super.onCreate()
        storageService = StorageService(applicationContext)
        updateService = UpdateService(applicationContext)
        LogService.info("KtorService", "Service creating.")
        initDefaultUser()
        extractCloudflared()
        extractNgrok()
    }

    private fun extractNgrok() {
        val ngrokFile = File(filesDir, "ngrok")
        if (!ngrokFile.exists()) {
            LogService.info("KtorService", "Ngrok binary not found, extracting from assets.")
            try {
                assets.open("ngrok").use { input ->
                    ngrokFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                ngrokFile.setExecutable(true)
                LogService.info("KtorService", "Ngrok binary extracted successfully.")
            } catch (e: java.io.IOException) {
                LogService.error("KtorService", "Failed to extract Ngrok binary: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun extractCloudflared() {
        val cloudflaredFile = java.io.File(filesDir, "cloudflared")
        if (!cloudflaredFile.exists()) {
            try {
                assets.open("cloudflared").use { input ->
                    cloudflaredFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                cloudflaredFile.setExecutable(true)
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initDefaultUser() {
        if (storageService.getUser() == null) {
            val hashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt())
            val defaultUser = User("admin", hashedPassword)
            storageService.saveUser(defaultUser)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogService.info("KtorService", "Service starting.")
        startForeground(1, createNotification())
        Thread {
            try {
                LogService.info("KtorService", "Starting Ktor server.")
                server.start(wait = true)
            } catch (e: Exception) {
                LogService.error("KtorService", "Ktor server start failed: ${e.message}")
                e.printStackTrace()
                sendUpdateBroadcast(STATUS_ERROR)
            }
        }.start()
        restartTunnels()
        return START_STICKY
    }

    fun restartTunnels() {
        Thread {
            stopTunnels()
            startNgrok()
            startCloudflared()
        }.start()
    }

    private fun stopTunnels() {
        LogService.info("KtorService", "Stopping all tunnels.")
        try {
            val appDir = storageService.getAppDirectory()
            Shell.execute("pkill -f ngrok", appDir)
            Shell.execute("pkill -f cloudflared", appDir)
            LogService.info("KtorService", "Tunnels stopped.")
        } catch (e: Exception) {
            LogService.error("KtorService", "Error stopping tunnels: ${e.message}")
        }
    }

    private fun startNgrok() {
        LogService.info("KtorService", "Checking Ngrok status.")
        val enabled = storageService.getString("ngrok_enabled") == "true"
        if (enabled) {
            LogService.info("KtorService", "Ngrok is enabled.")
            val authToken = storageService.getString("ngrok_auth_token")
            val domain = storageService.getString("ngrok_domain")
            val port = storageService.getString("http_port") ?: "8080"

            if (!authToken.isNullOrBlank()) {
                LogService.info("KtorService", "Ngrok auth token found.")
                try {
                    val appDir = storageService.getAppDirectory()
                    LogService.info("KtorService", "Configuring ngrok auth token.")
                    val configCommand = "./ngrok config add-authtoken $authToken"
                    val configOutput = Shell.execute(configCommand, appDir)
                    LogService.debug("KtorService", "Ngrok config output: $configOutput")

                    var command = "./ngrok http $port"
                    if (!domain.isNullOrBlank()) {
                        command += " --domain $domain"
                    }
                    LogService.info("KtorService", "Starting ngrok.")
                    Shell.executeInBackground(command, appDir, LogService, "Ngrok")
                } catch (e: Exception) {
                    LogService.error("KtorService", "Failed to start Ngrok: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                LogService.warn("KtorService", "Ngrok auth token is missing.")
            }
        } else {
            LogService.info("KtorService", "Ngrok is disabled.")
        }
    }

    private fun startCloudflared() {
        LogService.info("KtorService", "Checking Cloudflared status.")
        val enabled = storageService.getString("cloudflare_enabled") == "true"
        if (enabled) {
            val token = storageService.getString("cloudflare_token")
            if (!token.isNullOrBlank()) {
                LogService.info("KtorService", "Cloudflared token found, starting tunnel.")
                try {
                    val appDir = storageService.getAppDirectory()
                    val command = "./cloudflared tunnel --no-autoupdate run --token $token"
                    LogService.info("KtorService", "Starting Cloudflared.")
                    Shell.executeInBackground(command, appDir, LogService, "Cloudflared")
                } catch (e: Exception) {
                    LogService.error("KtorService", "Failed to start Cloudflared: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                LogService.warn("KtorService", "Cloudflared is enabled but token is missing.")
            }
        } else {
            LogService.info("KtorService", "Cloudflared is disabled.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogService.info("KtorService", "Service destroying.")
        stopTunnels()
        server.stop(1000, 5000)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendUpdateBroadcast(status: String) {
        val intent = Intent(ACTION_STATUS_UPDATE).apply {
            putExtra(EXTRA_STATUS, status)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification {
        val channelId = "KtorServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Ktor Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("WebWake Droid Server")
            .setContentText("HTTP server is running.")
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app's icon
            .build()
    }
}
