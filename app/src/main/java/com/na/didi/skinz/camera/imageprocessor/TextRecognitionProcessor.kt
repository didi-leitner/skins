package com.na.didi.skinz.camera.imageprocessor

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.io.IOException

class TextRecognitionProcessor() : FrameProcessorBase<Text>() {

    private val textRecognizer: TextRecognizer
    init {
        textRecognizer = TextRecognition.getClient()
    }

    fun detectTextInImage(image: InputImage): Task<Text> {
        Log.v("UUUUU","detectTextInImage")
        return textRecognizer.process(image)

    }

    override fun stop() {
        super.stop()
        try {
            textRecognizer.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close object detector!", e)
        }
    }



    override fun onFailure(e: Exception) {
        Log.w(TAG, "Text detection failed.$e")
    }

    companion object {
        private const val TAG = "TextRecProcessor"

    }
}

