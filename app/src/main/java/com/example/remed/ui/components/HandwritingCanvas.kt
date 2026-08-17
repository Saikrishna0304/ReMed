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
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    
    val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    lines.forEach { line ->
        val path = Path()
        if (line.points.isNotEmpty()) {
            path.moveTo(line.points[0].x, line.points[0].y)
            for (i in 1 until line.points.size) {
                path.lineTo(line.points[i].x, line.points[i].y)
            }
            canvas.drawPath(path, paint)
        }
    }
    return bitmap
}
