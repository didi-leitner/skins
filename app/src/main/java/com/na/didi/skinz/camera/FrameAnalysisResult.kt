package com.na.didi.skinz.camera

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.text.Text


sealed class FrameAnalysisResult {

    object OnNothingDetected: FrameAnalysisResult()
    data class OnConfirmingDetectedObject(val boundingBox: Rect, val progress: Float) :
        FrameAnalysisResult()

    data class OnObjectPicked(val boundingBox: Rect) :
        FrameAnalysisResult()

    data class OnTextDetected(val text: Text, val detectedObjectBitmap: Bitmap, val boundingBox: Rect) : FrameAnalysisResult()


}

