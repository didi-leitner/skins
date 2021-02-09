package com.na.didi.skinz.view.camera_graphics

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.na.didi.skinz.R
import com.na.didi.skinz.view.custom.GraphicOverlay

class CameraOverlayController(val graphicOverlay: GraphicOverlay){

    private val cameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    private val reticleOuterRingRadius: Int = graphicOverlay
            .resources
            .getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_radius)

    private var needUpdateGraphicOverlayImageSourceInfo = true

    fun renderOnMovedAwayFromDetectedObjectOverlay(visionObjectBoundingBox: Rect) {
        graphicOverlay.clear()
        // Object is detected but the confirmation reticle is moved off the object box, which
        // indicates user is not trying to pick this object.
        graphicOverlay.add(
                ObjectGraphicInProminentMode(graphicOverlay.context,
                        graphicOverlay, visionObjectBoundingBox, false
                )
        )
        graphicOverlay.add(ObjectReticleGraphic(graphicOverlay.context, cameraReticleAnimator))
        cameraReticleAnimator.start()

        graphicOverlay.invalidate()
    }

    fun renderOnConfirmedObjectOverlay(visionObjectBoundingBox: Rect) {
        graphicOverlay.clear()

        // User is confirming the object selection.
        cameraReticleAnimator.cancel()
        graphicOverlay.add(
                ObjectGraphicInProminentMode(graphicOverlay.context,
                        graphicOverlay, visionObjectBoundingBox, true
                )
        )

        graphicOverlay.invalidate()
    }

    fun renderConfirmingObjectOverlay(visionObjectBoundingBox: Rect, progress: Float) {
        graphicOverlay.clear()

        // User is confirming the object selection.
        cameraReticleAnimator.cancel()
        graphicOverlay.add(
                ObjectGraphicInProminentMode(graphicOverlay.context,
                        graphicOverlay, visionObjectBoundingBox, false
                )
        )

        // Shows a loading indicator to visualize the confirming progress if in auto search mode.
        graphicOverlay.add(ObjectConfirmationGraphic(graphicOverlay.context, progress))

        graphicOverlay.invalidate()

    }

    fun renderStartDetectingOverlay() {
        graphicOverlay.add(ObjectReticleGraphic(graphicOverlay.context, cameraReticleAnimator))
        cameraReticleAnimator.start()
    }

    fun setImageInfo(imageProxy: ImageProxy) {
        if (needUpdateGraphicOverlayImageSourceInfo) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height)
            } else {
                graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width)
            }
            needUpdateGraphicOverlayImageSourceInfo = false
        }
    }

    fun objectBoxOverlapsConfirmationReticle(visionObjectBoundingBox: Rect):Boolean {

        val boxRect = graphicOverlay.translateRect(visionObjectBoundingBox)
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



}