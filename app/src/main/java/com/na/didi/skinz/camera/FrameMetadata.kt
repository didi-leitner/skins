package com.na.didi.skinz.camera

class FrameMetadata private constructor(val width: Int, val height: Int, val rotation: Int) {

    /** Builder of [FrameMetadata].  */
    class Builder {
        private var width = 0
        private var height = 0
        private var rotation = 0
        fun setWidth(width: Int): FrameMetadata.Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): FrameMetadata.Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): FrameMetadata.Builder {
            this.rotation = rotation
            return this
        }

        fun build(): FrameMetadata {
            return FrameMetadata(width, height, rotation)
        }
    }
}