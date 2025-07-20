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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import zynkia.webwakedroid.service.LogService

class UpdateService(private val context: Context) {

    @Serializable
    private data class GitHubRelease(val tagName: String)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getLatestVersion(): String {
        return try {
            val release = client.get("https://api.github.com/repos/Zynkia/WebWake-Droid/releases/latest").body<GitHubRelease>()
            release.tagName
        } catch (e: Exception) {
            LogService.error("UpdateService", "Failed to get latest version: ${e.message}")
            "0.0.0"
        }
    }

    fun isUpdateAvailable(latestVersion: String): Boolean {
        val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        return latestVersion.compareTo(currentVersion) > 0
    }
}
