package com.virtualmouse.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TrackpadView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var onDragListener: ((dx: Float, dy: Float) -> Unit)? = null
    private var onSingleTapListener: (() -> Unit)? = null
    private var onDoubleTapListener: (() -> Unit)? = null
    private var onTwoFingerTapListener: (() -> Unit)? = null
    private var onRightClickListener: (() -> Unit)? = null
    private var onTwoFingerDragListener: ((dx: Float, dy: Float) -> Unit)? = null

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#40000000")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val handlePaint = Paint().apply {
        color = Color.parseColor("#60FFFFFF")
        style = Paint.Style.FILL
    }

    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastTapTime: Long = 0
    private var isDragging = false
    private var isDraggingMultiFinger = false
    private var tapCount = 0

    // Default size
    private var defaultWidth: Int = 280
    private var defaultHeight: Int = 180

    private var prefs by lazy {
        context.getSharedPreferences("virtualmouse_prefs", Context.MODE_PRIVATE)
    }

    init {
        isClickable = true
        isLongClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val savedWidth = prefs.getInt("trackpad_width", defaultWidth)
        val savedHeight = prefs.getInt("trackpad_height", defaultHeight)
        
        val width = kotlin.math.min(savedWidth.toFloat(), measuredWidth.toFloat())
        val height = kotlin.math.min(savedHeight.toFloat(), measuredHeight.toFloat())
        
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 16f, 16f, backgroundPaint)
        canvas.drawRoundRect(rect, 16f, 16f, borderPaint)
        
        // Draw handle at top
        val handleRadius = 12f
        canvas.drawCircle(width / 2f, handleRadius, handleRadius, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                lastTapTime = System.currentTimeMillis()
                isDragging = true
                
                if (event.pointerCount == 2) {
                    isDraggingMultiFinger = true
                    // Two-finger tap - right click
                    onTwoFingerTapListener?.invoke()
                }
                
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && event.pointerCount == 1) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    
                    // Apply acceleration curve for natural trackpad feel
                    val magnitude = kotlin.math.sqrt(dx * dx + dy * dy)
                    val sensitivity = prefs.getFloat("pointer_sensitivity", 1.0f)
                    val acceleration = 1 + 0.1f * magnitude / 20f
                    
                    onDragListener?.invoke(dx * sensitivity * acceleration, dy * sensitivity * acceleration)
                    
                    lastX = event.x
                    lastY = event.y
                } else if (isDraggingMultiFinger && event.pointerCount == 2) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    onTwoFingerDragListener?.invoke(dx, dy)
                    lastX = event.x
                    lastY = event.y
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                val elapsed = now - lastTapTime
                
                if (!isDragging && elapsed < 150 && event.pointerCount == 1) {
                    // Tap detection
                    tapCount++
                    if (tapCount >= 2 && elapsed < 300) {
                        onDoubleTapListener?.invoke()
                    } else {
                        // Schedule single tap
                        postDelayed({
                            if (tapCount == 1) {
                                onSingleTapListener?.invoke()
                            }
                        }, 150)
                    }
                }
                
                isDragging = false
                isDraggingMultiFinger = false
                tapCount = 0
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
            
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                isDraggingMultiFinger = false
                tapCount = 0
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }

    fun setOnDragListener(listener: (dx: Float, dy: Float) -> Unit) {
        onDragListener = listener
    }

    fun setOnSingleTapListener(listener: () -> Unit) {
        onSingleTapListener = listener
    }

    fun setOnDoubleTapListener(listener: () -> Unit) {
        onDoubleTapListener = listener
    }

    fun setOnTwoFingerTapListener(listener: () -> Unit) {
        onTwoFingerTapListener = listener
    }

    fun setOnTwoFingerDragListener(listener: (dx: Float, dy: Float) -> Unit) {
        onTwoFingerDragListener = listener
    }
}