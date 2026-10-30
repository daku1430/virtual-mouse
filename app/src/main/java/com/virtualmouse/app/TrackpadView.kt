package com.virtualmouse.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TrackpadView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var onDragListener: ((dx: Float, dy: Float) -> Unit)? = null
    private var onSingleTapListener: (() -> Unit)? = null
    private var onDoubleTapListener: (() -> Unit)? = null
    private var onTwoFingerTapListener: (() -> Unit)? = null

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
    private var downTime: Long = 0
    private var lastTapTime: Long = 0
    private var isDragging = false
    private val handler = Handler(Looper.getMainLooper())

    private val pendingSingleTap = Runnable {
        onSingleTapListener?.invoke()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 280
        val desiredHeight = 180
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
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
                downTime = event.eventTime
                lastX = event.x
                lastY = event.y
                isDragging = true
                handler.removeCallbacks(pendingSingleTap)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    onTwoFingerTapListener?.invoke()
                    isDragging = false
                }
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && event.pointerCount == 1) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    onDragListener?.invoke(dx, dy)
                    lastX = event.x
                    lastY = event.y
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                val now = event.eventTime
                val elapsed = now - downTime
                
                if (!isDragging || event.pointerCount > 1) {
                    handler.removeCallbacks(pendingSingleTap)
                    parent?.requestDisallowInterceptTouchEvent(false)
                    isDragging = false
                    return true
                }
                
                // Check for double tap
                if (now - lastTapTime < 300) {
                    // Double tap detected
                    handler.removeCallbacks(pendingSingleTap)
                    onDoubleTapListener?.invoke()
                } else {
                    // Post single tap with delay to detect double tap
                    handler.postDelayed(pendingSingleTap, 180)
                }
                
                lastTapTime = now
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
            
            MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacks(pendingSingleTap)
                parent?.requestDisallowInterceptTouchEvent(false)
                isDragging = false
                return true
            }
            
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(pendingSingleTap)
                isDragging = false
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
}