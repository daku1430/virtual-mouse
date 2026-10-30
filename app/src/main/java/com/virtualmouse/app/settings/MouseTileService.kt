package com.virtualmouse.app.settings

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.virtualmouse.app.MouseAccessibilityService

class MouseTileService : TileService() {

    companion object {
        private var tileInstance: Tile? = null
        
        fun getTile(): Tile? = tileInstance
    }

    override fun onClick() {
        super.onClick()
        val service = MouseAccessibilityService.getInstance()
        
        if (service == null || !MouseAccessibilityService.isRunning()) {
            // Try to open accessibility settings
            val intent = Intent(this, com.virtualmouse.app.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivityAndCollapse(intent)
            return
        }
        
        service.toggleService()
    }

    override fun onStartListening() {
        super.onStartListening()
        tileInstance = qsTile
        
        val service = MouseAccessibilityService.getInstance()
        val isEnabled = service != null && MouseAccessibilityService.isRunning()
        
        qsTile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = if (isEnabled) "On" else "Off"
            updateTile()
        }
    }
}