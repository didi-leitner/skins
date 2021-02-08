package com.na.didi.skinz.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.*


class GraphicOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lock = Any()

    private var previewWidth: Int = 0
    private var widthScaleFactor = 1.0f
    private var previewHeight: Int = 0
    private var heightScaleFactor = 1.0f
    private val graphics = ArrayList<Graphic>()

    abstract class Graphic protected constructor(protected val overlay: GraphicOverlay) {
        protected val context: Context = overlay.context

        abstract fun draw(canvas: Canvas)
    }


    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }


    fun setImageSourceInfo(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
    }

    fun translateX(x: Float): Float = x * widthScaleFactor
    fun translateY(y: Float): Float = y * heightScaleFactor

    /**
     * Adjusts the `rect`'s coordinate from the preview's coordinate system to the view
     * coordinate system.
     */
    fun translateRect(rect: Rect) = RectF(
            translateX(rect.left.toFloat()),
            translateY(rect.top.toFloat()),
            translateX(rect.right.toFloat()),
            translateY(rect.bottom.toFloat())
    )


    /** Draws the overlay with its associated graphic objects.  */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (previewWidth > 0 && previewHeight > 0) {
            widthScaleFactor = width.toFloat() / previewWidth
            heightScaleFactor = height.toFloat() / previewHeight
        }

        //SHOWCASE - DEFAULT prevSize 1280 x 720
        //            Urm: 720  1080 1.5     1280 1920 1.5
        //La mine   ->Urm: 1280  1080 0.84375     720 2151 2.9875
        //2021-01-31 1Urm: 720  1080 1.5     1280 2151 1.6804688


        //cu previewSize de la user 1920 x 960
        //ML Vision -> Urm: 960  1080 1.125     1920 2159 1.1244792



        //2021-01-31 11:26:16.368 Screen metrics: 1080 x 2340
        synchronized(lock) {
            graphics.forEach { it.draw(canvas) }
        }
    }
}

