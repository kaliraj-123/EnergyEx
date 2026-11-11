package com.energyex.app.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class EnergyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#00897B")
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.parseColor("#E2E8F0")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#718096")
        textSize = 28f
    }

    private val path = Path()
    private val fillPath = Path()

    private var dataPoints: List<Float> = listOf(15f, 28f, 42f, 35f, 60f, 85f, 50f, 70f, 65f, 95f)
    private var labels: List<String> = listOf("1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h")

    fun setData(points: List<Float>, xLabels: List<String>? = null) {
        if (points.isNotEmpty()) {
            this.dataPoints = points
            if (xLabels != null && xLabels.size == points.size) {
                this.labels = xLabels
            } else {
                this.labels = points.indices.map { "#${it + 1}" }
            }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val paddingLeft = 60f
        val paddingRight = 30f
        val paddingTop = 30f
        val paddingBottom = 60f

        val width = width.toFloat() - paddingLeft - paddingRight
        val height = height.toFloat() - paddingTop - paddingBottom

        if (width <= 0 || height <= 0) return

        val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(10f) * 1.15f
        val minVal = 0f

        // Draw horizontal grid lines & Y labels
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = paddingTop + height - (i * (height / gridCount))
            canvas.drawLine(paddingLeft, y, paddingLeft + width, y, gridPaint)
            val labelVal = (maxVal * (i.toFloat() / gridCount)).toInt()
            canvas.drawText("$labelVal", 10f, y + 10f, textPaint)
        }

        path.reset()
        fillPath.reset()

        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

        fillPath.moveTo(paddingLeft, paddingTop + height)

        dataPoints.forEachIndexed { index, value ->
            val x = paddingLeft + (index * stepX)
            val normalizedY = (value - minVal) / (maxVal - minVal)
            val y = paddingTop + height - (normalizedY * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            // Draw X label
            if (index < labels.size && (index % 2 == 0 || index == dataPoints.size - 1)) {
                canvas.drawText(labels[index], x - 15f, paddingTop + height + 40f, textPaint)
            }
        }

        fillPath.lineTo(paddingLeft + width, paddingTop + height)
        fillPath.close()

        // Gradient area under curve
        fillPaint.shader = LinearGradient(
            0f, paddingTop,
            0f, paddingTop + height,
            Color.parseColor("#4D00897B"),
            Color.parseColor("#0500897B"),
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)
    }
}

