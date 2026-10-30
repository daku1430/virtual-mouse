package com.virtualmouse.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

class CursorView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var cursorType: CursorType = CursorType.ARROW_WHITE
    private var cursorPaint: Paint? = null
    private val arrowPaths = mutableMapOf<CursorType, Pair<Int, Int>>()

    init {
        setZ(1000f) // Ensure cursor is above other views
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCursorPaint()
        x = (w / 2).toFloat()
        y = (h / 2).toFloat()
    }

    private fun updateCursorPaint() {
        cursorPaint = Paint().apply {
            color = when (cursorType) {
                CursorType.ARROW_WHITE -> Color.WHITE
                CursorType.ARROW_BLACK -> Color.BLACK
                CursorType.POINTER -> Color.BLACK
            }
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        cursorPaint?.let { paint ->
            when (cursorType) {
                CursorType.ARROW_WHITE -> {
                    drawArrowWhite(canvas, paint)
                }
                CursorType.ARROW_BLACK -> {
                    drawArrowBlack(canvas, paint)
                }
                CursorType.POINTER -> {
                    drawPointer(canvas, paint)
                }
            }
        }
    }

    private fun drawArrowWhite(canvas: Canvas, paint: Paint) {
        // White arrow with black outline
        val path = android.graphics.Path()
        val w = width
        val h = height
        
        // Arrow body
        path.moveTo(w / 2f, 0f)
        path.lineTo(w / 4f, h / 3f)
        path.lineTo(w / 8f, h / 3f)
        path.lineTo(w / 2f, h * 0.7f)
        path.lineTo(w * 0.75f, h / 3f)
        path.lineTo(w * 0.875f, h / 3f)
        path.close()
        
        // Black outline
        val outlinePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawPath(path, paint)
        canvas.drawPath(path, outlinePaint)
    }

    private fun drawArrowBlack(canvas: Canvas, paint: Paint) {
        // Black arrow with white outline
        val path = android.graphics.Path()
        val w = width
        val h = height
        
        path.moveTo(w / 2f, 0f)
        path.lineTo(w / 4f, h / 3f)
        path.lineTo(w / 8f, h / 3f)
        path.lineTo(w / 2f, h * 0.7f)
        path.lineTo(w * 0.75f, h / 3f)
        path.lineTo(w * 0.875f, h / 3f)
        path.close()
        
        // White outline
        val outlinePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawPath(path, paint)
        canvas.drawPath(path, outlinePaint)
    }

    private fun drawPointer(canvas: Canvas, paint: Paint) {
        // Hand pointer cursor
        val path = android.graphics.Path()
        val w = width
        val h = height
        
        // Pointer shape
        path.moveTo(w * 0.3f, 0f)
        path.lineTo(w * 0.3f, h * 0.4f)
        path.lineTo(0f, h * 0.5f)
        path.lineTo(w * 0.1f, h * 0.5f)
        path.lineTo(w * 0.4f, h)
        path.lineTo(w * 0.6f, h * 0.6f)
        path.lineTo(w, h * 0.5f)
        path.lineTo(w * 0.9f, h * 0.5f)
        path.close()
        
        canvas.drawPath(path, paint)
    }

    fun setCursorType(type: CursorType) {
        cursorType = type
        updateCursorPaint()
        invalidate()
    }

    fun getCursorType(): CursorType = cursorType
}

enum class CursorType {
    ARROW_WHITE, ARROW_BLACK, POINTER
}