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
package zynkia.webwakedroid.util

import zynkia.webwakedroid.service.LogService
import java.io.BufferedReader
import java.io.InputStreamReader

object Shell {
    fun execute(command: String, directory: String? = null): String {
        val processBuilder = ProcessBuilder(listOf("sh", "-c", command))
        if (directory != null) {
            processBuilder.directory(java.io.File(directory))
        }
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }
        process.waitFor()
        return output.toString()
    }

    fun executeInBackground(
        command: String,
        directory: String? = null,
        logService: LogService? = null,
        logTag: String = "Shell"
    ) {
        Thread {
            try {
                val processBuilder = ProcessBuilder(listOf("sh", "-c", command))
                if (directory != null) {
                    processBuilder.directory(java.io.File(directory))
                }
                val process = processBuilder.start()

                // Consume output and error streams to prevent the process from blocking
                val inputStreamGobbler = StreamGobbler(process.inputStream, logService, logTag)
                val errorStreamGobbler = StreamGobbler(process.errorStream, logService, logTag)
                inputStreamGobbler.start()
                errorStreamGobbler.start()

                process.waitFor()
            } catch (e: Exception) {
                logService?.error(logTag, "Error executing command: $command. Error: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}

private class StreamGobbler(
    private val inputStream: java.io.InputStream,
    private val logService: LogService?,
    private val logTag: String
) : Thread() {
    override fun run() {
        try {
            BufferedReader(InputStreamReader(inputStream)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    logService?.info(logTag, line!!)
                }
            }
        } catch (e: java.io.IOException) {
            logService?.error(logTag, "Error reading stream: ${e.message}")
            e.printStackTrace()
        }
    }
}
