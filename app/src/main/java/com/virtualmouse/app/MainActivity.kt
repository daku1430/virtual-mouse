package com.virtualmouse.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvServiceStatus: TextView
    private lateinit var btnOpenSettings: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvServiceStatus = findViewById(R.id.tvServiceStatus)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        btnSettings = findViewById(R.id.btnSettings)

        val serviceConnected = isAccessibilityServiceEnabled()
        updateServiceStatus(serviceConnected)

        btnOpenSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            // Settings are accessible via Quick Settings tile
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceComponent = ComponentName(this, MouseAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            "enabled_accessibility_service_list"
        )
        return enabledServices?.contains(serviceComponent.flattenToShortString()) ?: false
    }

    private fun updateServiceStatus(enabled: Boolean) {
        tvServiceStatus.text = if (enabled) {
            "Service Status: Enabled"
        } else {
            "Service Status: Not enabled"
        }
    }

    override fun onResume() {
        super.onResume()
        val serviceConnected = isAccessibilityServiceEnabled()
        updateServiceStatus(serviceConnected)
    }
}