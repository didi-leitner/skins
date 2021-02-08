package com.na.didi.skinz.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.Text
import com.na.didi.skinz.R
import com.na.didi.skinz.camera.imageprocessor.TextRecognitionProcessor
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.objectdetection.*
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.fragments.CameraViewFromAnalysisIntentListener
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import java.io.IOException


class ImageProcessor(val graphicOverlay: GraphicOverlay, val cameraIntentListener: CameraViewFromAnalysisIntentListener) : ImageAnalysis.Analyzer {

    private val objectDetector: ObjectDetector

    private val confirmationController: ObjectConfirmationController = ObjectConfirmationController(graphicOverlay)
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)
    private val reticleOuterRingRadius: Int = graphicOverlay
            .resources
            .getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_radius)

    private var needUpdateGraphicOverlayImageSourceInfo = true


    init {
        val options: ObjectDetectorOptionsBase
        val optionsBuilder = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        options = optionsBuilder.build()

        this.objectDetector = ObjectDetection.getClient(options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        try {
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            processImageProxy(imageProxy)
        } catch (e: MlKitException) {
            Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)

        }
    }


    fun stop() {
        try {
            objectDetector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close object detector!", e)
        }
    }

    private fun detectObjectInImage(image: InputImage): Task<List<DetectedObject>> {
        return objectDetector.process(image)
    }

    private fun onObjectDetected(
            originalBitmap: Bitmap?,
            results: List<DetectedObject>
    ) {
        var objects = results
        Log.v(TAG, "onSuccess in ProminentObjDetProcessor, " + " " + results.size)

        val objectIndex = 0
        val hasValidObjects = objects.isNotEmpty()
        if (!hasValidObjects) {
            confirmationController.reset()
            cameraIntentListener(CameraViewIntent.StartDetecting)
        } else {
            val visionObject = objects[objectIndex]
            if (objectBoxOverlapsConfirmationReticle(graphicOverlay, visionObject)) {
                // User is confirming the object selection.
                confirmationController.confirming(visionObject.trackingId)
                Log.v(TAG, "confirming object....." + originalBitmap)

                val selectedObjectIsConfirmed = confirmationController.progress.compareTo(1f) == 0
                if (selectedObjectIsConfirmed) {

                    if (originalBitmap != null) {
                        //viewIntent.onConfirmedDetectedObject.value = DetectedObjectInfo(visionObject, originalBitmap)
                        val detectedObjectInfo = DetectedObjectInfo(visionObject, originalBitmap)
                        val textProcessor = TextRecognitionProcessor()
                        val executor = ContextCompat.getMainExecutor(graphicOverlay.context)

                        textProcessor.detectTextInImage(InputImage.fromBitmap(detectedObjectInfo.getBitmap(), 0))
                                .addOnSuccessListener(executor) { text ->
                                    Log.v(TAG, "detectTextInImage success ")

                                    val products = processTextInProducts(text)
                                    cameraIntentListener(CameraViewIntent.OnTextDetected(SearchedObject(detectedObjectInfo, products)))


                                }
                                .addOnFailureListener(executor) { e: Exception ->

                                    e.printStackTrace()
                                    this.onFailure(e)

                                }
                    }

                    //pricess here with Text
                    //result will be
                    //if(originalBitmap != null)
                    //   viewIntent.onConfirmedDetectedObject.value = DetectedObjectInfo(visionObject, originalBitmap)

                } else {
                    cameraIntentListener(CameraViewIntent.OnPointCameraToDetectedObject)
                }

            } else {
                Log.v(TAG, "obj detected, user moved away")
                confirmationController.reset()
                cameraIntentListener(CameraViewIntent.OnMovedAwayFromDetectedObject)
            }
        }

        graphicOverlay.clear()
        if (!hasValidObjects) {
            graphicOverlay.add(ObjectReticleGraphic(graphicOverlay, cameraReticleAnimator))
            cameraReticleAnimator.start()
        } else {
            if (objectBoxOverlapsConfirmationReticle(graphicOverlay, objects[0])) {
                // User is confirming the object selection.
                cameraReticleAnimator.cancel()
                graphicOverlay.add(
                        ObjectGraphicInProminentMode(
                                graphicOverlay, objects[0], confirmationController
                        )
                )
                if (!confirmationController.isConfirmed) {
                    // Shows a loading indicator to visualize the confirming progress if in auto search mode.
                    graphicOverlay.add(ObjectConfirmationGraphic(graphicOverlay, confirmationController))
                }
            } else {
                // Object is detected but the confirmation reticle is moved off the object box, which
                // indicates user is not trying to pick this object.
                graphicOverlay.add(
                        ObjectGraphicInProminentMode(
                                graphicOverlay, objects[0], confirmationController
                        )
                )
                graphicOverlay.add(ObjectReticleGraphic(graphicOverlay, cameraReticleAnimator))
                cameraReticleAnimator.start()
            }
        }
        graphicOverlay.invalidate()
    }

    // -----------------Code for processing live preview frame from CameraX API-----------------------
    @SuppressLint("UnsafeExperimentalUsageError")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun processImageProxy(imageProxy: ImageProxy) {

        if (needUpdateGraphicOverlayImageSourceInfo) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height)
            } else {
                graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width)
            }
            needUpdateGraphicOverlayImageSourceInfo = false
        }

        val originalCameraImage = BitmapUtils.getBitmap(imageProxy)
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        val executor = ContextCompat.getMainExecutor(graphicOverlay.context)

        detectObjectInImage(inputImage)
                .addOnSuccessListener(executor) { results ->
                    graphicOverlay.clear()
                    Log.v(TAG, "detectInImage success " + results + " " + imageProxy)
                    onObjectDetected(originalCameraImage, results)
                    graphicOverlay.postInvalidate()
                }
                .addOnFailureListener(executor) { e: Exception ->
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    Toast.makeText(
                            graphicOverlay?.context,
                            "Failed to process.\nError: " +
                                    e.localizedMessage +
                                    "\nCause: " +
                                    e.cause,
                            Toast.LENGTH_LONG
                    )
                            .show()
                    e.printStackTrace()
                    this.onFailure(e)

                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
    }

    private fun processTextInProducts(text: Text): List<Product> {
        if (text != null) {
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

        return emptyList<Product>()
    }


    fun onFailure(e: Exception) {
        Log.e(TAG, "Object detection failed!", e)
    }

    private fun objectBoxOverlapsConfirmationReticle(
            graphicOverlay: GraphicOverlay,
            visionObject: DetectedObject
    ): Boolean {
        val boxRect = graphicOverlay.translateRect(visionObject.boundingBox)
        val reticleCenterX = graphicOverlay.width / 2f
        val reticleCenterY = graphicOverlay.height / 2f
        val reticleRect = RectF(
                reticleCenterX - reticleOuterRingRadius,
                reticleCenterY - reticleOuterRingRadius,
                reticleCenterX + reticleOuterRingRadius,
                reticleCenterY + reticleOuterRingRadius
        )
        return reticleRect.intersect(boxRect)
    }




    companion object {
        private const val TAG = "ProminentObjProcessor"
    }

}
