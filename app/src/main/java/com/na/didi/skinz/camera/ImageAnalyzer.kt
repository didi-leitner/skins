package com.na.didi.skinz.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
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
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import java.io.IOException
import java.util.concurrent.Executor


class ImageAnalyzer(val setImageInfo: (ImageProxy) -> Unit,
                    val objectBoxOverlapsConfirmationReticle: (Rect) -> Boolean,
                    val executor: Executor,
                    val imageAnalysisIntentListener: (intent: CameraViewIntent) -> Unit) : ImageAnalysis.Analyzer {

    private val objectDetector: ObjectDetector
    private val textRecognizer: TextRecognizer
    private val confirmationController: ObjectConfirmationController

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
            setImageInfo(imageProxy)

            processImageProxy(imageProxy)
        } catch (e: MlKitException) {
            Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)

        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun processImageProxy(imageProxy: ImageProxy) {

        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        detectObjectInImage(inputImage)
                .addOnSuccessListener(executor) { objects ->

                    if (objects.isNotEmpty()) {
                        val objectIndex = 0
                        val originalCameraImage = BitmapUtils.getBitmap(imageProxy)
                        val visionObject = objects[objectIndex]
                        onObjectDetected(visionObject, originalCameraImage!!)


                    } else {
                        confirmationController.reset()
                        imageAnalysisIntentListener(CameraViewIntent.StartDetecting)
                    }
                }
                .addOnFailureListener(executor) { e: Exception ->

                    this.onFailure(e)

                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
    }



    fun processBitmap(detectedObjectBitmap: Bitmap) {

        val inputImapge = InputImage.fromBitmap(detectedObjectBitmap, 0)

        detectTextInImage(inputImapge)
                .addOnSuccessListener(executor) { text ->

                    //TODO move off mmain
                    val products = processTextInProducts(text)
                    imageAnalysisIntentListener(CameraViewIntent.OnProductsFound(products))
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

    private fun onObjectDetected(visionObject: DetectedObject, originalBitmap: Bitmap) {

        if (objectBoxOverlapsConfirmationReticle(visionObject.boundingBox)) {
            // User is confirming the object selection.
            confirmationController.confirming(visionObject.trackingId)
            Log.v(TAG, "confirming object....." + visionObject.trackingId +  " " + confirmationController.progress)

            //val selectedObjectIsConfirmed = confirmationController.progress.compareTo(1f) == 0
            if (confirmationController.progress.compareTo(1f) == 0) {
                imageAnalysisIntentListener(CameraViewIntent.OnConfirmedDetectedObject(originalBitmap, visionObject.boundingBox))
            } else {
                imageAnalysisIntentListener(CameraViewIntent.OnConfirmingDetectedObject(visionObject.boundingBox, confirmationController.progress))
            }

        } else {
            confirmationController.reset()
            imageAnalysisIntentListener(CameraViewIntent.OnMovedAwayFromDetectedObject(visionObject.boundingBox))
        }

    }



    private fun processTextInProducts(text: Text): List<Product> {
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
                    /*Log.v(
                            TAG,
                            String.format(
                                    "Detected text element %d has a bounding box: %s",
                                    k, element.boundingBox!!.flattenToString()
                            )
                    )
                    Log.v(
                            TAG,
                            String.format(
                                    "Expected corner point size is 4, get %d", element.cornerPoints!!.size
                            )
                    )
                    for (point in element.cornerPoints!!) {
                        Log.v(
                                TAG,
                                String.format(
                                        "Corner point for element %d is located at: x - %d, y = %d",
                                        k, point.x, point.y
                                )
                        )
                    }*/
                }
            }

            Log.v(TAG, "__________________" + titleCandidate + " : " + subtitleCandidate + "_________________________________")

        }


        return listOf(Product(0, titleCandidate, subtitleCandidate, null, System.currentTimeMillis()))

    }


    private fun onFailure(e: Exception) {
        Log.e(TAG, "Object detection failed!", e)
        //TODO ?
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
        private const val TAG = "ProminentObjProcessor"
    }

}
