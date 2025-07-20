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

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

enum class LogLevel {
    INFO, WARN, ERROR, DEBUG
}

object LogService {

    private lateinit var storageService: StorageService
    private lateinit var logFile: File
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val appContext = context.applicationContext
        storageService = StorageService(appContext)

        val externalFilesDir = appContext.getExternalFilesDir(null)
        val logDirectory = if (externalFilesDir != null) {
            File(externalFilesDir, "logs")
        } else {
            // Fallback to internal storage if external is not available
            File(appContext.filesDir, "logs")
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }

        val logFileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(Date()) + ".log"
        logFile = File(logDirectory, logFileName)
        isInitialized = true
        info("LogService", "Log service initialized. Log file: ${logFile.absolutePath}")
    }

    private fun log(level: LogLevel, tag: String, message: String) {
        if (!isInitialized) return

        val logToFile = storageService.getString("log_to_file", "false").toBoolean()
        if (!logToFile) {
            return
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp [${level.name}] [$tag] $message\n"

        try {
            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun info(tag: String, message: String) {
        log(LogLevel.INFO, tag, message)
    }

    fun warn(tag: String, message: String) {
        log(LogLevel.WARN, tag, message)
    }

    fun error(tag: String, message: String) {
        log(LogLevel.ERROR, tag, message)
    }

    fun debug(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }
}
