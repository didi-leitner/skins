package com.na.didi.skinz.camera

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import androidx.annotation.experimental.UseExperimental
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.camera_graphics.ObjectConfirmationController
import java.io.IOException
import java.util.concurrent.Executor

//TODO interface for these methods
class ImageAnalyzer(
    val invalidateOverlay: () -> Unit,
    val setImageInfo: (ImageProxy) -> Unit,
    val objectBoxOverlapsConfirmationReticle: (Rect) -> Boolean,
    val executor: Executor,
    val frameAnalysisResultListener: (intent: FrameAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val objectDetector: ObjectDetector
    private val textRecognizer: TextRecognizer

    private val confirmationController: ObjectConfirmationController

    private var processingFrame: Boolean = false

    init {
        val options: ObjectDetectorOptionsBase
        val optionsBuilder = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        options = optionsBuilder.build()

        this.objectDetector = ObjectDetection.getClient(options)
        this.textRecognizer = TextRecognition.getClient()

        this.confirmationController = ObjectConfirmationController()
    }

    override fun analyze(imageProxy: ImageProxy) {
        try {
            if (!processingFrame) {
                setImageInfo(imageProxy)
                processImageProxyForObjDetection(imageProxy)
            }

        } catch (e: MlKitException) {
            Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)

        }
    }

    @UseExperimental(markerClass = androidx.camera.core.ExperimentalGetImage::class)
    fun processImageProxyForObjDetection(imageProxy: ImageProxy) {

        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        val startMs = SystemClock.elapsedRealtime()
        detectObjectInImage(inputImage)
            .addOnSuccessListener(executor) { objects ->
                //TODO improve latency
                //Log.d(TAG, "Latency is: ${SystemClock.elapsedRealtime() - startMs}")
                this.onImageProxyProcessed(objects, imageProxy)
            }
            .addOnFailureListener(executor) { e: Exception ->
                this.onFailure(e)
            }
            .addOnCompleteListener {
                imageProxy.close()
                processingFrame = false
            }
    }


    fun processBitmapForText(detectedObjectBitmap: Bitmap, boundingBox: Rect) {

        val inputImapge = InputImage.fromBitmap(detectedObjectBitmap, 0)
        detectTextInImage(inputImapge)
            .addOnSuccessListener(executor) { text ->
                frameAnalysisResultListener(FrameAnalysisResult.OnTextDetected(text, detectedObjectBitmap, boundingBox))
            }
            .addOnFailureListener(executor) { e: Exception ->
                this.onFailure(e)
            }
    }


    fun stop() {
        try {
            objectDetector.close()
            textRecognizer.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close object detector!", e)
        }
    }

    private fun detectObjectInImage(image: InputImage): Task<List<DetectedObject>> {
        return objectDetector.process(image)
    }

    private fun detectTextInImage(image: InputImage): Task<Text> {
        return textRecognizer.process(image)
    }

    @ExperimentalGetImage
    private fun onImageProxyProcessed(objects: List<DetectedObject>, imageProxy: ImageProxy) {

        val hasValidObjects = objects.isNotEmpty() //&& TODO (hasValidText())

        if (hasValidObjects) {
            val objectIndex = 0
            val visionObject = objects[objectIndex]

            if (objectBoxOverlapsConfirmationReticle(visionObject.boundingBox)) {
                // User is confirming the object selection.
                confirmationController.confirming(visionObject.trackingId, {
                    invalidateOverlay()
                })
                Log.v(
                    TAG,
                    "confirming object....." + visionObject.trackingId + " " + confirmationController.progress
                )

                if (confirmationController.progress.compareTo(1f) == 0) {
                    frameAnalysisResultListener(
                        FrameAnalysisResult.OnObjectPicked(
                            visionObject.boundingBox
                        )
                    )

                    BitmapUtils.getBitmap(imageProxy)?.let {
                        processBitmapForText(it, visionObject.boundingBox)

                    }


                } else {
                    frameAnalysisResultListener(
                        FrameAnalysisResult.OnConfirmingDetectedObject(
                            visionObject.boundingBox,
                            confirmationController.progress
                        )
                    )
                }

            } else {
                confirmationController.reset()
                Log.v(TAG, "on Moved away")
                frameAnalysisResultListener(FrameAnalysisResult.OnNothingDetected)
            }


        } else {
            if (confirmationController.isConfirming()) {
                confirmationController.reset()
                frameAnalysisResultListener(FrameAnalysisResult.OnNothingDetected)
            }
        }
    }


    fun processTextInProducts(text: Text): List<Product> {

        Log.v(TAG, "Detected text has : " + text.textBlocks.size + " blocks")
        var titleCandidate = ""
        var subtitleCandidate = ""

        for (i in text.textBlocks.indices) {
            val lines = text.textBlocks[i].lines
            Log.v(TAG, String.format("Detected text block %d has %d lines", i, lines.size))

            for (j in lines.indices) {
                val elements = lines[j].elements
                Log.v(TAG, String.format("Detected text line %d has %d elements", j, elements.size))
                for (k in elements.indices) {
                    val element = elements[k]
                    Log.v(TAG, String.format("Detected text element %d says: %s", k, element.text))

                    if (i == 0)
                        titleCandidate = titleCandidate.plus(" ").plus(element.text)
                    else if (i == 1)
                        subtitleCandidate = subtitleCandidate.plus(" ").plus(element.text)

                    if (i >= 1)
                        break

                }
            }

            Log.v(
                TAG,
                "__________________" + titleCandidate + " : " + subtitleCandidate + "_________________________________"
            )

        }


        return listOf(
            Product(
                0,
                titleCandidate,
                subtitleCandidate,
                null,
                System.currentTimeMillis()
            )
        )

    }


    private fun onFailure(e: Exception) {
        Log.e(TAG, "Object detection failed!", e)
        //TODO
        /*graphicOverlay.clear()
        graphicOverlay.postInvalidate()
        Toast.makeText(
                graphicOverlay?.context,
                "Failed to process.\nError: " +
                        e.localizedMessage +
                        "\nCause: " +
                        e.cause,
                Toast.LENGTH_LONG
        )
                .show()*/
        e.printStackTrace()
    }


    companion object {
        private const val TAG = "ImageAnalyzer"
    }

}
