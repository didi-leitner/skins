package com.na.didi.skinz.camera.imageprocessor

import androidx.annotation.GuardedBy
import com.na.didi.skinz.camera.FrameMetadata
import java.nio.ByteBuffer

/** Abstract base class of [FrameProcessor].  */
abstract class FrameProcessorBase<T> : FrameProcessor {

    // To keep the frame and metadata in process.
    @GuardedBy("this")
    private var processingFrame: ByteBuffer? = null

    @GuardedBy("this")
    private var processingFrameMetaData: FrameMetadata? = null

    // Whether this processor is already shut down
    private var isShutdown = false



    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null
    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null
    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null
    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null


    // -----------------Code for processing single still image----------------------------------------
    /*override fun processBitmap(detectedObjectInfo: DetectedObjectInfo, graphicOverlay: GraphicOverlay) {

        Log.v(TAG,"processBitmap method " + isShutdown)
        if (isShutdown) {
            return
        }

        val inputImage: InputImage = InputImage.fromBitmap(detectedObjectInfo.getBitmap(),0)
        requestDetectInImage(
                inputImage,
                graphicOverlay,
                null,
                ContextCompat.getMainExecutor(graphicOverlay.context))

    }*/

    // -----------------Code for processing live preview frame from Camera1 API-----------------------
    /*@Synchronized
    override fun processByteBuffer(
            data: ByteBuffer?,
            frameMetadata: FrameMetadata?,
            graphicOverlay: GraphicOverlay
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay)
        }
    }*/


    /*private fun requestDetectInImage(
            image: InputImage,
            graphicOverlay: GraphicOverlay,
            detectedObjectInfo: DetectedObjectInfo,
            executor: Executor
    ): Task<T> {

        return detectInImage(image).addOnSuccessListener(executor) { results: T ->

            graphicOverlay.clear()


            Log.v(TAG,"detectInImage success " + results + " " + image)
            this@FrameProcessorBase.onSuccess(originalCameraImage, results, graphicOverlay)


            graphicOverlay?.postInvalidate()
        }
                .addOnFailureListener(executor) { e: Exception ->
                    graphicOverlay?.clear()
                    graphicOverlay?.postInvalidate()
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
                    this@FrameProcessorBase.onFailure(e)
                }
    }*/

    override fun stop() {
        isShutdown = true

    }


    protected abstract fun onFailure(e: Exception)




    companion object {
        private const val TAG = "FrameProcessorBase"
    }
}
