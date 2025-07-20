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
import android.content.SharedPreferences
import zynkia.webwakedroid.model.Device
import zynkia.webwakedroid.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StorageService(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("webwakedroid_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveUser(user: User) {
        val userJson = json.encodeToString(user)
        prefs.edit().putString("user", userJson).commit()
    }

    fun getUser(): User? {
        val userJson = prefs.getString("user", null)
        return if (userJson != null) {
            try {
                json.decodeFromString<User>(userJson)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun saveDevices(devices: List<Device>) {
        val devicesJson = json.encodeToString(devices)
        prefs.edit().putString("devices", devicesJson).commit()
    }

    fun getDevices(): List<Device> {
        val devicesJson = prefs.getString("devices", null)
        return if (devicesJson != null) {
            try {
                json.decodeFromString<List<Device>>(devicesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return prefs.getString(key, defaultValue)
    }

    fun getAppDirectory(): String {
        return context.filesDir.absolutePath
    }
}
