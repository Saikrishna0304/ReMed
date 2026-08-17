package com.example.remed.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.remed.ml.HandwritingRecognitionModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class HandwritingProcessor(private val context: Context) {

    private val model: HandwritingRecognitionModel by lazy {
        HandwritingRecognitionModel.newInstance(context)
    }

    fun processHandwriting(bitmap: Bitmap): String {
        // 1. Preprocess the image
        // Assuming the model expects 28x28 grayscale image (common for MNIST-like handwriting models)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28, 28, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1]
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Run inference
        val outputs = model.process(tensorImage.tensorBuffer)
        val outputBuffer = outputs.outputFeature0AsTensorBuffer

        // 3. Post-process (Translate tensor to character)
        // This depends on the specific model's output (e.g., Softmax indices to A-Z, 0-9)
        return translateBufferToText(outputBuffer)
    }

    private fun translateBufferToText(buffer: TensorBuffer): String {
        val floatArray = buffer.floatArray
        val maxIndex = floatArray.indices.maxByOrNull { floatArray[it] } ?: -1
        
        // Example mapping for a numeric model (0-9)
        return if (maxIndex in 0..9) maxIndex.toString() else "?"
    }

    fun close() {
        model.close()
    }
}
