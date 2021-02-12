package com.na.didi.skinz.view.camera_graphics


import android.os.CountDownTimer


class ObjectConfirmationController
{

    private val countDownTimer: CountDownTimer

    private var objectId: Int? = null

    private lateinit var onTickListener: (Float) -> Unit

    var progress = 0f
        private set

    val isConfirmed: Boolean
        get() = progress.compareTo(1f) == 0

    init {
        val confirmationTimeMs = 1500L
        countDownTimer = object : CountDownTimer(confirmationTimeMs, /* countDownInterval= */ 20) {
            override fun onTick(millisUntilFinished: Long) {
                progress = (confirmationTimeMs - millisUntilFinished).toFloat() / confirmationTimeMs
                //graphicOverlay.invalidate()
                onTickListener(progress)

            }

            override fun onFinish() {
                progress = 1f
            }
        }
    }

    fun confirming(objectId: Int?, onTickListener: (Float)-> Unit) {
        if (objectId == this.objectId) {
            // Do nothing if it's already in confirming.
            return
        }

        reset()
        this.objectId = objectId
        this.onTickListener = onTickListener
        countDownTimer.start()
    }

    fun reset() {
        countDownTimer.cancel()
        objectId = null
        progress = 0f
    }

    fun isConfirming(): Boolean {
        return this.objectId != null
    }
}

