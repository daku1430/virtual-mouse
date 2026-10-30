package com.virtualmouse.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.graphics.Point

class MouseAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: MouseAccessibilityService? = null
        private const val DOUBLE_TAP_TIMEOUT_MS = 300L
        
        fun getInstance(): MouseAccessibilityService? = instance
        
        fun isRunning(): Boolean = instance?.isServiceRunning() ?: false
    }

    private lateinit var windowManager: WindowManager
    private var trackpadView: TrackpadView? = null
    private var cursorView: CursorView? = null
    private var lastTapTime: Long = 0
    private var serviceRunning: Boolean = false

    private val screenWidth get() = resources.displayMatrix.width()
    private val screenHeight get() = resources.displayMatrix.height()

    fun isServiceRunning(): Boolean = serviceRunning

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

    fun getCursorX(): Float = cursorView?.x ?: screenWidth / 2f
    fun getCursorY(): Float = cursorView?.y ?: screenHeight / 2f

    fun moveCursor(dx: Float, dy: Float) {
        cursorView?.let {
            val newX = (it.x + dx).coerceIn(0f, screenWidth - it.width)
            val newY = (it.y + dy).coerceIn(0f, screenHeight - it.height)
            it.x = newX
            it.y = newY
        }
    }

    fun performLeftClick() {
        val path = GestureDescription.Builder()
            .addStep(GestureDescription.StepBuilder(
                GestureDescription.PathBuilder()
                    .moveTo(getCursorX(), getCursorY())
                    .press()
                    .release()
            ).build())
            .build()
        
        dispatchGesture(path, null, null)
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

    fun performRightClick() {
        val path = GestureDescription.Builder()
            .addStep(GestureDescription.StepBuilder(
                GestureDescription.PathBuilder()
                    .moveTo(getCursorX(), getCursorY())
                    .press()
                    .release()
            ).build())
            .build()
        
        dispatchGesture(path, null, null)
    }

    fun performLongClick() {
        val path = GestureDescription.Builder()
            .addStep(
                GestureDescription.StepBuilder(
                    GestureDescription.PathBuilder()
                        .moveTo(getCursorX(), getCursorY())
                        .press(500)
                ).setInterval(0)
            )
            .build()
        
        dispatchGesture(path, null, null)
    }

    fun performDrag(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 200L) {
        val path = GestureDescription.Builder()
            .addStep(GestureDescription.StepBuilder(
                GestureDescription.PathBuilder()
                    .moveTo(startX, startY)
                    .lineTo(endX, endY)
                    .up()
            ).setDuration(duration))
            .build()
        
        dispatchGesture(path, null, null)
    }

    fun performScroll(dx: Float, dy: Float) {
        val path = GestureDescription.Builder()
            .addStep(GestureDescription.StepBuilder(
                GestureDescription.PathBuilder()
                    .swipe(getCursorX(), getCursorY(), getCursorX() - dx, getCursorY() - dy, 200)
            ).build())
            .build()
        
        dispatchGesture(path, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
        removeOverlays()
        instance = null
    }
}