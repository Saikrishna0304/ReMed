package com.example.remed.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class Line(
    val points: List<Offset>,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    val strokeWidth: Float = 10f
)

@Composable
fun HandwritingCanvas(
    modifier: Modifier = Modifier,
    lines: MutableList<Line>
) {
    Box(modifier = modifier.background(androidx.compose.ui.graphics.Color.White)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            lines.add(Line(points = listOf(offset)))
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val lastLine = lines.last()
                            lines[lines.lastIndex] = lastLine.copy(
                                points = lastLine.points + change.position
                            )
                        }
                    )
                }
        ) {
            lines.forEach { line ->
                for (i in 0 until line.points.size - 1) {
                    drawLine(
                        color = line.color,
                        start = line.points[i],
                        end = line.points[i + 1],
                        strokeWidth = line.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

fun captureCanvasToBitmap(lines: List<Line>, width: Int, height: Int): Bitmap {
    if (lines.isEmpty()) return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Find bounding box of the handwriting to crop it for better recognition
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE

    lines.forEach { line ->
        line.points.forEach { point ->
            minX = minOf(minX, point.x)
            minY = minOf(minY, point.y)
            maxX = maxOf(maxX, point.x)
            maxY = maxOf(maxY, point.y)
        }
    }

    // Add some padding and ensure we don't go out of bounds
    val padding = 40f
    minX = (minX - padding).coerceAtLeast(0f)
    minY = (minY - padding).coerceAtLeast(0f)
    maxX = (maxX + padding).coerceAtMost(width.toFloat())
    maxY = (maxY + padding).coerceAtMost(height.toFloat())

    val contentWidth = (maxX - minX).toInt()
    val contentHeight = (maxY - minY).toInt()

    if (contentWidth <= 0 || contentHeight <= 0) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
    }

    // Create a bitmap that fits the content
    val bitmap = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    
    val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 12f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    lines.forEach { line ->
        val path = Path()
        if (line.points.isNotEmpty()) {
            path.moveTo(line.points[0].x - minX, line.points[0].y - minY)
            for (i in 1 until line.points.size) {
                path.lineTo(line.points[i].x - minX, line.points[i].y - minY)
            }
            canvas.drawPath(path, paint)
        }
    }
    return bitmap
}
