package com.virtualmouse.app.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.virtualmouse.app.MouseAccessibilityService

class MouseTileService : TileService() {

    companion object {
        private var tileInstance: Tile? = null
        
        fun getTile(): Tile? = tileInstance
    }

    override fun onClick() {
        super.onClick()
        val service = MouseAccessibilityService.getInstance()
        
        if (service == null) {
            // Service not running, try to enable it
            val intent = Intent(this, MouseAccessibilityService::class.java)
            intent.action = android.accessibilityservice.AccessibilityService.ACTION_CLICK_TO_TOUCH
            startActivityAndCollapse(intent)
            return
        }
        
        service.toggleService()
    }

    override fun onStartListening() {
        super.onStartListening()
        tileInstance = qsTile
        
        val service = MouseAccessibilityService.getInstance()
        val isEnabled = service != null && service.isServiceRunning()
        
        qsTile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = if (isEnabled) "On" else "Off"
            updateTile()
        }
    }
}