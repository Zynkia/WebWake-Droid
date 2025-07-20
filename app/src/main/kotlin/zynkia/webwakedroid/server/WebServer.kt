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
package zynkia.webwakedroid.server

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import zynkia.webwakedroid.service.StorageService
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level
import zynkia.webwakedroid.service.LogService
import zynkia.webwakedroid.service.UpdateService

data class UserSession(val name: String) : Principal

fun Application.mainModule(storageService: StorageService, updateService: UpdateService, onSettingsChanged: () -> Unit) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
        }
        logger = object : org.slf4j.Logger {
            override fun getName(): String = "Ktor"
            override fun isTraceEnabled(): Boolean = true
            override fun isTraceEnabled(marker: org.slf4j.Marker?): Boolean = true
            override fun isDebugEnabled(): Boolean = true
            override fun isDebugEnabled(marker: org.slf4j.Marker?): Boolean = true
            override fun isInfoEnabled(): Boolean = true
            override fun isInfoEnabled(marker: org.slf4j.Marker?): Boolean = true
            override fun isWarnEnabled(): Boolean = true
            override fun isWarnEnabled(marker: org.slf4j.Marker?): Boolean = true
            override fun isErrorEnabled(): Boolean = true
            override fun isErrorEnabled(marker: org.slf4j.Marker?): Boolean = true
            override fun trace(msg: String?) {
                LogService.debug("Ktor", msg ?: "")
            }
            override fun trace(format: String?, arg: Any?) {
                LogService.debug("Ktor", format?.format(arg) ?: "")
            }
            override fun trace(format: String?, arg1: Any?, arg2: Any?) {
                LogService.debug("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun trace(format: String?, vararg arguments: Any?) {
                LogService.debug("Ktor", format?.format(*arguments) ?: "")
            }
            override fun trace(msg: String?, t: Throwable?) {
                LogService.debug("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun trace(marker: org.slf4j.Marker?, msg: String?) {
                LogService.debug("Ktor", msg ?: "")
            }
            override fun trace(marker: org.slf4j.Marker?, format: String?, arg: Any?) {
                LogService.debug("Ktor", format?.format(arg) ?: "")
            }
            override fun trace(marker: org.slf4j.Marker?, format: String?, arg1: Any?, arg2: Any?) {
                LogService.debug("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun trace(marker: org.slf4j.Marker?, format: String?, vararg arguments: Any?) {
                LogService.debug("Ktor", format?.format(*arguments) ?: "")
            }
            override fun trace(marker: org.slf4j.Marker?, msg: String?, t: Throwable?) {
                LogService.debug("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun debug(msg: String?) {
                LogService.debug("Ktor", msg ?: "")
            }
            override fun debug(format: String?, arg: Any?) {
                LogService.debug("Ktor", format?.format(arg) ?: "")
            }
            override fun debug(format: String?, arg1: Any?, arg2: Any?) {
                LogService.debug("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun debug(format: String?, vararg arguments: Any?) {
                LogService.debug("Ktor", format?.format(*arguments) ?: "")
            }
            override fun debug(msg: String?, t: Throwable?) {
                LogService.debug("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun debug(marker: org.slf4j.Marker?, msg: String?) {
                LogService.debug("Ktor", msg ?: "")
            }
            override fun debug(marker: org.slf4j.Marker?, format: String?, arg: Any?) {
                LogService.debug("Ktor", format?.format(arg) ?: "")
            }
            override fun debug(marker: org.slf4j.Marker?, format: String?, arg1: Any?, arg2: Any?) {
                LogService.debug("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun debug(marker: org.slf4j.Marker?, format: String?, vararg arguments: Any?) {
                LogService.debug("Ktor", format?.format(*arguments) ?: "")
            }
            override fun debug(marker: org.slf4j.Marker?, msg: String?, t: Throwable?) {
                LogService.debug("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun info(msg: String?) {
                LogService.info("Ktor", msg ?: "")
            }
            override fun info(format: String?, arg: Any?) {
                LogService.info("Ktor", format?.format(arg) ?: "")
            }
            override fun info(format: String?, arg1: Any?, arg2: Any?) {
                LogService.info("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun info(format: String?, vararg arguments: Any?) {
                LogService.info("Ktor", format?.format(*arguments) ?: "")
            }
            override fun info(msg: String?, t: Throwable?) {
                LogService.info("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun info(marker: org.slf4j.Marker?, msg: String?) {
                LogService.info("Ktor", msg ?: "")
            }
            override fun info(marker: org.slf4j.Marker?, format: String?, arg: Any?) {
                LogService.info("Ktor", format?.format(arg) ?: "")
            }
            override fun info(marker: org.slf4j.Marker?, format: String?, arg1: Any?, arg2: Any?) {
                LogService.info("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun info(marker: org.slf4j.Marker?, format: String?, vararg arguments: Any?) {
                LogService.info("Ktor", format?.format(*arguments) ?: "")
            }
            override fun info(marker: org.slf4j.Marker?, msg: String?, t: Throwable?) {
                LogService.info("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun warn(msg: String?) {
                LogService.warn("Ktor", msg ?: "")
            }
            override fun warn(format: String?, arg: Any?) {
                LogService.warn("Ktor", format?.format(arg) ?: "")
            }
            override fun warn(format: String?, vararg arguments: Any?) {
                LogService.warn("Ktor", format?.format(*arguments) ?: "")
            }
            override fun warn(format: String?, arg1: Any?, arg2: Any?) {
                LogService.warn("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun warn(msg: String?, t: Throwable?) {
                LogService.warn("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun warn(marker: org.slf4j.Marker?, msg: String?) {
                LogService.warn("Ktor", msg ?: "")
            }
            override fun warn(marker: org.slf4j.Marker?, format: String?, arg: Any?) {
                LogService.warn("Ktor", format?.format(arg) ?: "")
            }
            override fun warn(marker: org.slf4j.Marker?, format: String?, arg1: Any?, arg2: Any?) {
                LogService.warn("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun warn(marker: org.slf4j.Marker?, format: String?, vararg arguments: Any?) {
                LogService.warn("Ktor", format?.format(*arguments) ?: "")
            }
            override fun warn(marker: org.slf4j.Marker?, msg: String?, t: Throwable?) {
                LogService.warn("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun error(msg: String?) {
                LogService.error("Ktor", msg ?: "")
            }
            override fun error(format: String?, arg: Any?) {
                LogService.error("Ktor", format?.format(arg) ?: "")
            }
            override fun error(format: String?, arg1: Any?, arg2: Any?) {
                LogService.error("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun error(format: String?, vararg arguments: Any?) {
                LogService.error("Ktor", format?.format(*arguments) ?: "")
            }
            override fun error(msg: String?, t: Throwable?) {
                LogService.error("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
            override fun error(marker: org.slf4j.Marker?, msg: String?) {
                LogService.error("Ktor", msg ?: "")
            }
            override fun error(marker: org.slf4j.Marker?, format: String?, arg: Any?) {
                LogService.error("Ktor", format?.format(arg) ?: "")
            }
            override fun error(marker: org.slf4j.Marker?, format: String?, arg1: Any?, arg2: Any?) {
                LogService.error("Ktor", format?.format(arg1, arg2) ?: "")
            }
            override fun error(marker: org.slf4j.Marker?, format: String?, vararg arguments: Any?) {
                LogService.error("Ktor", format?.format(*arguments) ?: "")
            }
            override fun error(marker: org.slf4j.Marker?, msg: String?, t: Throwable?) {
                LogService.error("Ktor", "$msg\n${t?.stackTraceToString() ?: ""}")
            }
        }
    }

    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                val user = storageService.getUser()
                if (user != null && user.username == credentials.name && BCrypt.checkpw(credentials.password, user.passwordHash)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login?error=invalid_credentials")
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                session
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }

    routing {
        get("/login") {
            val resourcePath = "templates/login.html"
            val resourceStream = call.application.javaClass.classLoader.getResourceAsStream(resourcePath)
            if (resourceStream != null) {
                call.respondText(resourceStream.readBytes().toString(Charsets.UTF_8), ContentType.Text.Html)
            } else {
                call.respond(HttpStatusCode.NotFound, "Template not found: $resourcePath")
            }
        }

        authenticate("auth-form") {
            post("/login") {
                val principal = call.principal<UserIdPrincipal>()
                if (principal != null) {
                    call.sessions.set(UserSession(name = principal.name))
                    call.respondRedirect("/dashboard")
                }
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/login")
        }

        authenticate("auth-session") {
            get("/") {
                call.respondRedirect("/dashboard")
            }

            get("/dashboard") {
                val resourcePath = "templates/dashboard.html"
                val resourceStream = call.application.javaClass.classLoader.getResourceAsStream(resourcePath)
                if (resourceStream != null) {
                    var content = resourceStream.readBytes().toString(Charsets.UTF_8)
                    val latestVersion = updateService.getLatestVersion()
                    if (updateService.isUpdateAvailable(latestVersion)) {
                        content = content.replace("<!-- UPDATE_NOTIFICATION -->", """<div class="update-notification">New version available: $latestVersion. <a href="https://github.com/Zynkia/WebWake-Droid/releases/latest" target="_blank">Download</a></div>""")
                    }
                    call.respondText(content, ContentType.Text.Html)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Template not found: $resourcePath")
                }
            }

            get("/settings") {
                val resourcePath = "templates/settings.html"
                val resourceStream = call.application.javaClass.classLoader.getResourceAsStream(resourcePath)
                if (resourceStream != null) {
                    var content = resourceStream.readBytes().toString(Charsets.UTF_8)
                    val latestVersion = updateService.getLatestVersion()
                    if (updateService.isUpdateAvailable(latestVersion)) {
                        content = content.replace("<!-- UPDATE_NOTIFICATION -->", """<div class="update-notification">New version available: $latestVersion. <a href="https://github.com/Zynkia/WebWake-Droid/releases/latest" target="_blank">Download</a></div>""")
                    }
                    call.respondText(content, ContentType.Text.Html)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Template not found: $resourcePath")
                }
            }

            route("/api") {
                post("/wol/{macAddress}") {
                    val macAddress = call.parameters["macAddress"]
                    if (macAddress != null) {
                        zynkia.webwakedroid.util.Wol.wake(macAddress)
                        val resourcePath = "templates/wol_success.html"
                        val resourceStream = call.application.javaClass.classLoader.getResourceAsStream(resourcePath)
                        if (resourceStream != null) {
                            call.respondText(resourceStream.readBytes().toString(Charsets.UTF_8), ContentType.Text.Html)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Template not found: $resourcePath")
                        }
                    } else {
                        call.respond(mapOf("status" to "error", "message" to "MAC address not provided"))
                    }
                }

                route("/devices") {
                    get {
                        call.respond(storageService.getDevices())
                    }

                    post {
                        val device = call.receive<zynkia.webwakedroid.model.Device>()
                        val devices = storageService.getDevices().toMutableList()
                        devices.add(device)
                        storageService.saveDevices(devices)
                        call.respond(mapOf("status" to "ok"))
                    }

                    put {
                        val updatedDevices = call.receive<List<zynkia.webwakedroid.model.Device>>()
                        storageService.saveDevices(updatedDevices)
                        call.respond(mapOf("status" to "ok"))
                    }

                    delete("/{macAddress}") {
                        val macAddress = call.parameters["macAddress"]
                        if (macAddress != null) {
                            val devices = storageService.getDevices().toMutableList()
                            devices.removeAll { it.macAddress == macAddress }
                            storageService.saveDevices(devices)
                            call.respond(mapOf("status" to "ok"))
                        } else {
                            call.respond(mapOf("status" to "error", "message" to "MAC address not provided"))
                        }
                    }
                }

                route("/port") {
                    get {
                        val port = storageService.getString("http_port") ?: "8080"
                        call.respond(mapOf("port" to port))
                    }

                    post("/update") {
                        val params = call.receiveParameters()
                        val port = params["port"]
                        if (!port.isNullOrBlank()) {
                            storageService.saveString("http_port", port)
                            call.respond(mapOf("status" to "ok", "message" to "Port updated. Please restart the service."))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Port not provided"))
                        }
                    }
                }

                route("/user") {
                    get("/username") {
                        val user = storageService.getUser()
                        if (user != null) {
                            call.respond(mapOf("username" to user.username))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "User not found"))
                        }
                    }

                    put {
                        val updatedUser = call.receive<zynkia.webwakedroid.model.User>()
                        if (updatedUser.username.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Username cannot be empty"))
                            return@put
                        }

                        val currentUser = storageService.getUser()
                        if (currentUser != null) {
                            val newPasswordHash = if (updatedUser.passwordHash.isNotEmpty()) {
                                BCrypt.hashpw(updatedUser.passwordHash, BCrypt.gensalt())
                            } else {
                                currentUser.passwordHash
                            }
                            val userToSave = currentUser.copy(
                                username = updatedUser.username,
                                passwordHash = newPasswordHash
                            )
                            storageService.saveUser(userToSave)
                            call.respond(mapOf("status" to "ok"))
                        } else {
                            call.respond(mapOf("status" to "error", "message" to "User not found"))
                        }
                    }
                }

                route("/ngrok") {
                    get("/status") {
                        val enabled = storageService.getString("ngrok_enabled") == "true"
                        val authToken = storageService.getString("ngrok_auth_token") ?: ""
                        val domain = storageService.getString("ngrok_domain") ?: ""
                        val jsonResponse = buildJsonObject {
                            put("enabled", enabled)
                            put("authToken", authToken)
                            put("domain", domain)
                        }
                        call.respond(jsonResponse)
                    }

                    post("/update") {
                        val params = call.receiveParameters()
                        val enabled = params["enabled"] == "true"
                        val authToken = params["authToken"]
                        val domain = params["domain"]

                        LogService.info("WebServer", "Received ngrok update: enabled=$enabled")

                        if (enabled && authToken.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Auth Token cannot be empty when Ngrok is enabled."))
                            return@post
                        }

                        storageService.saveString("ngrok_enabled", enabled.toString())
                        storageService.saveString("ngrok_auth_token", authToken ?: "")
                        storageService.saveString("ngrok_domain", domain ?: "")
                        LogService.info("WebServer", "Saved ngrok settings. Triggering restart.")
                        onSettingsChanged()
                        call.respond(mapOf("status" to "ok", "message" to "Ngrok settings saved."))
                    }
                }

                route("/cloudflare") {
                    get("/status") {
                        val enabled = storageService.getString("cloudflare_enabled") == "true"
                        val token = storageService.getString("cloudflare_token") ?: ""
                        val jsonResponse = buildJsonObject {
                            put("enabled", enabled)
                            put("token", token)
                        }
                        call.respond(jsonResponse)
                    }

                    post("/update") {
                        val params = call.receiveParameters()
                        val enabled = params["enabled"] == "true"
                        val token = params["token"]

                        LogService.info("WebServer", "Received cloudflare update: enabled=$enabled")

                        if (enabled && token.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Token cannot be empty when Cloudflare is enabled."))
                            return@post
                        }

                        storageService.saveString("cloudflare_enabled", enabled.toString())
                        storageService.saveString("cloudflare_token", token ?: "")
                        LogService.info("WebServer", "Saved cloudflare settings. Triggering restart.")
                        onSettingsChanged()
                        call.respond(mapOf("status" to "ok", "message" to "Cloudflare settings saved."))
                    }
                }

                route("/ssl") {
                    get("/status") {
                        val enabled = storageService.getString("https_enabled") == "true"
                        val cert = storageService.getString("ssl_cert") ?: ""
                        val key = storageService.getString("ssl_key") ?: ""
                        call.respond(mapOf("enabled" to enabled, "cert" to cert, "key" to key))
                    }

                    post("/update") {
                        val params = call.receiveParameters()
                        val httpsEnabled = params["httpsEnabled"] == "true"
                        val cert = params["cert"]
                        val key = params["key"]

                        if (httpsEnabled && (cert.isNullOrBlank() || key.isNullOrBlank())) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Certificate and Key cannot be empty when HTTPS is enabled."))
                            return@post
                        }

                        storageService.saveString("https_enabled", httpsEnabled.toString())
                        if (cert != null) {
                            storageService.saveString("ssl_cert", cert)
                        }
                        if (key != null) {
                            storageService.saveString("ssl_key", key)
                        }
                        
                        call.respond(mapOf("status" to "ok", "message" to "SSL settings updated. Please restart the service."))
                    }
                }

                route("/service") {
                    post("/restart") {
                        call.respond(mapOf("status" to "ok", "message" to "Service is restarting..."))
                        kotlin.system.exitProcess(0)
                    }
                }

                route("/logging") {
                    get("/status") {
                        val enabled = storageService.getString("log_to_file", "false") == "true"
                        call.respond(mapOf("enabled" to enabled))
                    }

                    post("/update") {
                        val params = call.receiveParameters()
                        val enabled = params["enabled"] == "true"
                        storageService.saveString("log_to_file", enabled.toString())
                        call.respond(mapOf("status" to "ok", "message" to "Logging settings updated."))
                    }
                }
            }
        }
    }
}
