package com.virtualmouse.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs

class MouseAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: MouseAccessibilityService? = null
        private const val DOUBLE_TAP_TIMEOUT_MS = 300L
        
        fun getInstance(): MouseAccessibilityService? = instance
        
        fun isRunning(): Boolean = instance?.serviceRunning() ?: false
    }

    private lateinit var windowManager: WindowManager
    private var trackpadView: TrackpadView? = null
    private var cursorView: CursorView? = null
    private var lastTapTime: Long = 0
    private var serviceRunning: Boolean = false
    private var pendingLongPress: Runnable? = null

    private val screenWidth get() = resources.displayMetrics.widthPixels
    private val screenHeight get() = resources.displayMetrics.heightPixels

    fun serviceRunning(): Boolean = serviceRunning

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceRunning = true
        createOverlays()
        updateTileState()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No events to process
    }

    override fun onInterrupt() {
        // Nothing to interrupt
    }

    private fun createOverlays() {
        val params = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        // Create trackpad overlay
        trackpadView = TrackpadView(this)
        val trackpadParams = params.clone()
        trackpadParams.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
        trackpadView?.let {
            windowManager.addView(it, trackpadParams)
            setupTrackpadGestures()
        }

        // Create cursor overlay
        cursorView = CursorView(this)
        val cursorParams = params.clone()
        cursorParams.gravity = android.view.Gravity.START or android.view.Gravity.TOP
        cursorParams.flags = params.flags or android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        cursorView?.let {
            windowManager.addView(it, cursorParams)
        }
    }

    private fun removeOverlays() {
        try {
            trackpadView?.let { windowManager.removeView(it) }
        } catch (e: Exception) { /* already removed */ }
        try {
            cursorView?.let { windowManager.removeView(it) }
        } catch (e: Exception) { /* already removed */ }
        trackpadView = null
        cursorView = null
    }

    private fun setupTrackpadGestures() {
        trackpadView?.setOnDragListener { dx, dy ->
            moveCursor(dx, dy)
        }
        
        trackpadView?.setOnSingleTapListener {
            performLeftClick()
        }
        
        trackpadView?.setOnDoubleTapListener {
            performDoubleClick()
        }
        
        trackpadView?.setOnTwoFingerTapListener {
            performLongClick()
        }
    }

    fun toggleService() {
        if (serviceRunning) {
            removeOverlays()
            serviceRunning = false
        } else {
            createOverlays()
            serviceRunning = true
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = com.virtualmouse.app.settings.MouseTileService.getTile()
        tile?.let {
            it.state = if (serviceRunning) android.service.quicksettings.Tile.STATE_ACTIVE else android.service.quicksettings.Tile.STATE_INACTIVE
            it.subtitle = if (serviceRunning) "On" else "Off"
            it.updateTile()
        }
    }

    fun getCursorX(): Float = cursorView?.x ?: (screenWidth / 2).toFloat()
    fun getCursorY(): Float = cursorView?.y ?: (screenHeight / 2).toFloat()

    fun moveCursor(dx: Float, dy: Float) {
        cursorView?.let {
            val newX = (it.x + dx).coerceIn(0f, (screenWidth - it.width).toFloat())
            val newY = (it.y + dy).coerceIn(0f, (screenHeight - it.height).toFloat())
            it.x = newX
            it.y = newY
        }
    }

    fun performLeftClick() {
        val cursorX = getCursorX()
        val cursorY = getCursorY()
        
        val path = Path().apply {
            moveTo(cursorX, cursorY)
            lineTo(cursorX, cursorY)
        }
        
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        
        dispatchGesture(gesture, null, null)
    }

    fun performDoubleClick() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < DOUBLE_TAP_TIMEOUT_MS) {
            performLeftClick()
            performLeftClick()
            lastTapTime = 0
        } else {
            lastTapTime = now
        }
    }

    fun performLongClick() {
        val cursorX = getCursorX()
        val cursorY = getCursorY()
        
        val path = Path().apply {
            moveTo(cursorX, cursorY)
            lineTo(cursorX, cursorY)
        }
        
        val stroke = GestureDescription.StrokeDescription(path, 0, 500)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        
        dispatchGesture(gesture, null, null)
    }

    fun performScroll(dx: Float, dy: Float) {
        val startX = getCursorX()
        val startY = getCursorY()
        val endX = startX - dx
        val endY = startY - dy
        
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        
        val stroke = GestureDescription.StrokeDescription(path, 0, 200)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        
        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
        removeOverlays()
        instance = null
    }
}