package com.na.didi.skinz.sep_module

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

/**
 * Our custom image analysis class.
 *
 * <p>All we need to do is override the function `analyze` with our desired operations. Here,
 * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
 */
class MyImageAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    // Or, to set the minimum confidence required:
    // val options = ImageLabelerOptions.Builder()
    //     .setConfidenceThreshold(0.7f)
    //     .build()
    // val labeler = ImageLabeling.getClient(options)

    private var lastAnalyzedTimestamp = 0L
    var framesPerSecond: Double = -1.0
        private set

    /**
     * Used to add listeners that will be called with each luma computed
     */
    fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) {
            imageProxy.close()
            return
        }


        val mediaImage = imageProxy.image

        if(mediaImage == null) {
            imageProxy.close()
        }

        // Keep track of frames analyzed
        /*val currentTime = System.currentTimeMillis()
        frameTimestamps.push(currentTime)*/

        // Compute the FPS using a moving average
        //while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        //val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
        //val timestampLast = frameTimestamps.peekLast() ?: currentTime
        //framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
        //        frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

        // Analysis could take an arbitrarily long amount of time
        // Since we are running in a different thread, it won't stall other use cases

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        // Pass image to an ML Kit Vision API
        // ...

        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    val index = label.index

                    Log.v("TAG","Label: " + text + " " + confidence)
                }

                Log.v("TAG","------------------------")


            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }.addOnCompleteListener {
                imageProxy.close()

            }


        // Call all listeners with new value
        //listeners.forEach { it(luma) }

    }
}
