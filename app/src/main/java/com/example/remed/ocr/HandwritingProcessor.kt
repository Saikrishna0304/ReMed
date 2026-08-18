package com.example.remed.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.remed.ml.HandwritingRecognitionModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class HandwritingProcessor(private val context: Context) {

    private val model: HandwritingRecognitionModel by lazy {
        HandwritingRecognitionModel.newInstance(context)
    }

    fun processHandwriting(bitmap: Bitmap): String {
        // 1. Preprocess the image
        // Most handwriting models expect 28x28 grayscale, inverted (white ink on black background)
        val imageProcessor = ImageProcessor.Builder()
            .add(TransformToGrayscaleOp())
            .add(ResizeOp(28, 28, ResizeOp.ResizeMethod.BILINEAR))
            // Invert colors while normalizing: Our canvas is black ink (0) on white (255).
            // (255 - x) / 255 maps white to 0.0 (background) and black to 1.0 (ink).
            // NormalizeOp(mean, stddev) -> result = (val - mean) / stddev
            // To get (255 - x) / 255.0:
            // x' = (x - 255) / -255.0 = (255 - x) / 255.0
            .add(NormalizeOp(255f, -255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Run inference
        val outputs = model.process(tensorImage.tensorBuffer)
        val outputBuffer = outputs.outputFeature0AsTensorBuffer

        // 3. Post-process (Translate tensor to character)
        return translateBufferToText(outputBuffer)
    }

    private fun translateBufferToText(buffer: TensorBuffer): String {
        val floatArray = buffer.floatArray
        val maxIndex = floatArray.indices.maxByOrNull { floatArray[it] } ?: -1
        
        // Comprehensive alphanumeric mapping (EMNIST-like)
        // 0-9: '0'-'9'
        // 10-35: 'A'-'Z'
        // 36-61: 'a'-'z'
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
