package com.virtualmouse.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CursorView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    enum class CursorType { ARROW_WHITE, ARROW_BLACK, POINTER }
    
    private var cursorType: CursorType = CursorType.ARROW_WHITE
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val outlinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    init {
        setZ(1000f) // Ensure cursor is above other views
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        x = (w / 2).toFloat()
        y = (h / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        when (cursorType) {
            CursorType.ARROW_WHITE -> drawArrowWhite(canvas)
            CursorType.ARROW_BLACK -> drawArrowBlack(canvas)
            CursorType.POINTER -> drawPointer(canvas)
        }
    }

    private fun drawArrowWhite(canvas: Canvas) {
        val w = width
        val h = height
        val path = buildArrowPath(w, h)
        
        fillPaint.color = Color.WHITE
        outlinePaint.color = Color.BLACK
        
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, outlinePaint)
    }

    private fun drawArrowBlack(canvas: Canvas) {
        val w = width
        val h = height
        val path = buildArrowPath(w, h)
        
        fillPaint.color = Color.BLACK
        outlinePaint.color = Color.WHITE
        
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, outlinePaint)
    }

    private fun drawPointer(canvas: Canvas) {
        val w = width
        val h = height
        val path = buildPointerPath(w, h)
        
        fillPaint.color = Color.BLACK
        
        canvas.drawPath(path, fillPaint)
    }

    private fun buildArrowPath(w: Int, h: Int): Path {
        return Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w / 4f, h / 3f)
            lineTo(w / 8f, h / 3f)
            lineTo(w / 2f, h * 2f / 3f)
            lineTo(w * 3f / 4f, h / 3f)
            lineTo(w * 7f / 8f, h / 3f)
            close()
        }
    }

    private fun buildPointerPath(w: Int, h: Int): Path {
        return Path().apply {
            moveTo(w * 0.3f, 0f)
            lineTo(w * 0.3f, h * 0.4f)
            lineTo(0f, h * 0.5f)
            lineTo(w * 0.1f, h * 0.5f)
            lineTo(w * 0.4f, h.toFloat())
            lineTo(w * 0.6f, h * 0.6f)
            lineTo(w.toFloat(), h * 0.5f)
            lineTo(w * 0.9f, h * 0.5f)
            close()
        }
    }

    fun setCursorType(type: CursorType) {
        cursorType = type
        invalidate()
    }

    fun getCursorType(): CursorType = cursorType
}