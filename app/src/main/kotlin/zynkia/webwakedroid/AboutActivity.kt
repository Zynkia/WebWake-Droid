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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import zynkia.webwakedroid.service.UpdateService
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zynkia.webwakedroid.service.LogService
import zynkia.webwakedroid.BuildConfig

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val githubLink = findViewById<TextView>(R.id.github_link)
        githubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/Zynkia/WebWake-Droid")
            startActivity(intent)
        }

        val licenseLink = findViewById<TextView>(R.id.license_link)
        licenseLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/Zynkia/WebWake-Droid/blob/main/LICENSE")
            startActivity(intent)
        }

        val versionText = findViewById<TextView>(R.id.version_text)
        versionText.text = "Version ${BuildConfig.VERSION_NAME}"
        versionText.setOnClickListener {
            checkForUpdates()
        }
    }

    private fun checkForUpdates() {
        val updateService = UpdateService(this)
        CoroutineScope(Dispatchers.IO).launch {
            val latestVersion = updateService.getLatestVersion()
            if (updateService.isUpdateAvailable(latestVersion)) {
                LogService.info("AboutActivity", "New version available: $latestVersion")
                runOnUiThread {
                    AlertDialog.Builder(this@AboutActivity)
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
            } else {
                runOnUiThread {
                    AlertDialog.Builder(this@AboutActivity)
                        .setTitle("No Updates")
                        .setMessage("You are already on the latest version.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
