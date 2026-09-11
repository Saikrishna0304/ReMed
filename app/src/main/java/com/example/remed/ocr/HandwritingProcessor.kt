package com.example.remed.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.remed.ml.HandwritingRecognitionModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HandwritingProcessor(private val context: Context) {

    private val model: HandwritingRecognitionModel by lazy {
        HandwritingRecognitionModel.newInstance(context)
    }

    fun processHandwriting(bitmap: Bitmap): String {
        // 1. Scale bitmap to 28x28 (pure Kotlin, no JNI library needed)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

        // 2. Prepare float tensor buffer (28x28 grayscale inverted)
        val byteBuffer = ByteBuffer.allocateDirect(28 * 28 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(28 * 28)
        scaledBitmap.getPixels(pixels, 0, 28, 0, 0, 28, 28)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // Grayscale value 0..255
            val gray = (0.299f * r + 0.587f * g + 0.114f * b)
            // Normalize & invert: (255 - gray) / 255.0f
            val normalized = (255f - gray) / 255f
            byteBuffer.putFloat(normalized)
        }

        // 3. Run inference
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 28, 28, 1), DataType.FLOAT32)
        inputBuffer.loadBuffer(byteBuffer)

        val outputs = model.process(inputBuffer)
        val outputBuffer = outputs.outputFeature0AsTensorBuffer

        // 4. Post-process (Translate tensor to character)
        return translateBufferToText(outputBuffer)
    }

    private fun translateBufferToText(buffer: TensorBuffer): String {
        val floatArray = buffer.floatArray
        val maxIndex = floatArray.indices.maxByOrNull { floatArray[it] } ?: -1

        return when (maxIndex) {
            in 0..9 -> maxIndex.toString()
            in 10..35 -> ('A'.code + (maxIndex - 10)).toChar().toString()
            in 36..61 -> ('a'.code + (maxIndex - 36)).toChar().toString()
            else -> "?"
        }
    }

    fun close() {
        model.close()
    }
}
