package com.na.didi.hangerz.view.fragments

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.na.didi.hangerz.databinding.FragmentCameraBinding
import com.na.didi.hangerz.sep_module.LuminosityAnalyzer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCameraBinding.inflate(inflater, container, false)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for take photo button
        binding.cameraCaptureButton.setOnClickListener { captureImage() }


        viewFinder = binding.viewFinder
        cameraExecutor = Executors.newSingleThreadExecutor()


        return binding.root
    }

    /**
     *
     */
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }


    private fun captureImage() {

        // Get a stable reference of the modifiable image capture use case
        imageCapture?.let { imageCapture ->

            val fileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
            val outputOptions: ImageCapture.OutputFileOptions


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val outputDirectoryName = Environment.DIRECTORY_PICTURES + File.separator + "hangerz"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.RELATIVE_PATH, outputDirectoryName)
                    //java.lang.SecurityException: Permission Denial: writing com.android.providers.media.MediaProvider uri content://media/external/images/media from pid=29517, uid=10149 requires android.permission.WRITE_EXTERNAL_STORAGE, or grantUriPermission()
                    //put(MediaStore.Images.Media.DATA, outputDirectoryName) //pt <29 ?????
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                outputOptions = ImageCapture.OutputFileOptions
                        .Builder(requireContext().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        .build()

            } else {

                //TODO check this - when should we save to the media app?
                //this  is an app-private folder.
                ///storage/emulated/0/Android/media/com.na.didi.hangerz/hangerz/2021-01-04-21-01-06-715.jpg api 23
                //storage/emulated/0/Pictures/hangerz/2021-01-04-21-31-27-071.jpg: open failed: EACCES (Permission denied)

                val outputDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "hangerz").apply {
                    if (!exists())
                        mkdirs()
                }
                val photoFile = File(outputDirectory, fileName + ".jpg")
                outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            }


            imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri //?: Uri.fromFile(photoFile)

                    Log.d(TAG, "Photo capture succeeded: $savedUri")


                    // Implicit broadcasts will be ignored for devices running API level >= 24
                    // so if you only target API level 24+ you can remove this statement
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        requireActivity().sendBroadcast(Intent(Camera.ACTION_NEW_PICTURE, savedUri)
                        )
                    }

                    requireActivity().supportFragmentManager.popBackStack()

                }
            })

        }

    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build()
                    .also {
                        it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                    }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                            //Log.d(TAG, "Average luminosity: $luma")
                        })
                    }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(),
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                //TODO?
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(Manifest.permission.CAMERA)
        else arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        /*
        private fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext

            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, "hangerz").apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }*/
    }


}