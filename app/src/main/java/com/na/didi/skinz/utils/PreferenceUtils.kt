package com.na.didi.skinz.utils


import android.content.Context
import android.os.Build.VERSION_CODES
import android.preference.PreferenceManager
import android.util.Size
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.na.didi.skinz.R


/** Utility class to retrieve shared preferences.  */
object PreferenceUtils {



    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Nullable
    fun getCameraXTargetResolution(context: Context): Size? {
        val prefKey = context.getString(R.string.pref_key_camerax_target_resolution)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            Size.parseSize(sharedPreferences.getString(prefKey, null))
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun getIntPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyId)
        return sharedPreferences.getInt(prefKey, defaultValue)
    }



    /*fun getCameraPreviewSizePair(context: Context, cameraId: Int): SizePair? {
        com.google.common.base.Preconditions.checkArgument(cameraId == CameraSource.CAMERA_FACING_BACK
                || cameraId == CameraSource.CAMERA_FACING_FRONT)
        val previewSizePrefKey: String
        val pictureSizePrefKey: String
        if (cameraId == CameraSource.CAMERA_FACING_BACK) {
            previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size)
        } else {
            previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size)
        }
        return try {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            SizePair(
                    com.google.android.gms.common.images.Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
                    com.google.android.gms.common.images.Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)))
        } catch (e: Exception) {
            null
        }
    }*/



    /*fun getFaceDetectorOptionsForLivePreview(context: Context): FaceDetectorOptions {
        val landmarkMode = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_landmark_mode,
                FaceDetectorOptions.LANDMARK_MODE_NONE)
        val contourMode = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_contour_mode,
                FaceDetectorOptions.CONTOUR_MODE_ALL)
        val classificationMode = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_classification_mode,
                FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        val performanceMode = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_performance_mode,
                FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val enableFaceTracking = sharedPreferences.getBoolean(
                context.getString(R.string.pref_key_live_preview_face_detection_face_tracking), false)
        val minFaceSize =
                sharedPreferences.getString(
                        context.getString(R.string.pref_key_live_preview_face_detection_min_face_size),
                        "0.1")!!.toFloat()
        val optionsBuilder: FaceDetectorOptions.Builder = Builder()
                .setLandmarkMode(landmarkMode)
                .setContourMode(contourMode)
                .setClassificationMode(classificationMode)
                .setPerformanceMode(performanceMode)
                .setMinFaceSize(minFaceSize)
        if (enableFaceTracking) {
            optionsBuilder.enableTracking()
        }
        return optionsBuilder.build()
    }*/

}

