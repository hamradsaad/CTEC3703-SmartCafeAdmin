package com.example.smartcafeadmin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// A simple bar chart drawn with Canvas (no external library)
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var labels: List<String> = emptyList()
    private var values: List<Double> = emptyList()
    private var formatValue: (Double) -> String = { "%.2f".format(it) }

    private val density = resources.displayMetrics.density

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6F4E37")
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 11 * density
        textAlign = Paint.Align.CENTER
    }

    fun setData(labels: List<String>, values: List<Double>, formatValue: (Double) -> String) {
        this.labels = labels
        this.values = values
        this.formatValue = formatValue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (values.isEmpty()) {
            canvas.drawText("No data yet", width / 2f, height / 2f, textPaint)
            return
        }

        val maxValue = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
        val topSpace = 20 * density
        val bottomSpace = 24 * density
        val chartHeight = height - topSpace - bottomSpace
        val baseline = topSpace + chartHeight
        val slotWidth = width.toFloat() / values.size
        val barWidth = slotWidth * 0.6f

        canvas.drawLine(0f, baseline, width.toFloat(), baseline, axisPaint)

        values.forEachIndexed { i, value ->
            val left = i * slotWidth + (slotWidth - barWidth) / 2
            val barHeight = (value / maxValue * chartHeight).toFloat()
            val top = baseline - barHeight
            val centreX = left + barWidth / 2

            canvas.drawRect(left, top, left + barWidth, baseline, barPaint)
            canvas.drawText(formatValue(value), centreX, top - 4 * density, textPaint)
            canvas.drawText(labels.getOrElse(i) { "" }, centreX, height - 6 * density, textPaint)
        }
    }
}

