package com.na.didi.skinz.view.fragments


import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.annotation.KeepName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.na.didi.skinz.R
import com.na.didi.skinz.camera.ImageAnalyzer
import com.na.didi.skinz.databinding.FragmentCameraBinding
import com.na.didi.skinz.utils.Utils
import com.na.didi.skinz.view.adapters.BottomSheetProductAdapter
import com.na.didi.skinz.view.adapters.ProductPreviewClickListener
import com.na.didi.skinz.view.custom.BottomSheetScrimView
import com.na.didi.skinz.view.custom.GraphicOverlay
import com.na.didi.skinz.view.camera_graphics.CameraOverlayController
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewEffect
import com.na.didi.skinz.view.viewstate.CameraViewState
import com.na.didi.skinz.viewmodel.CameraXViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class CameraXLivePreviewFragment :
        BaseFragmentMVI<CameraViewState, CameraViewEffect, CameraViewIntent>(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener {

    override val viewModel: CameraXViewModel by viewModels()

    private lateinit var imageAnalyzer: ImageAnalyzer
    private lateinit var cameraOverlayController: CameraOverlayController

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var displayId: Int = -1
    //private val displayManager by lazy {
    //    requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    //}
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView

    private var settingsButton: View? = null
    private var flashButton: View? = null

    private var promptChip: Chip? = null
    private var promptChipAnimator: AnimatorSet? = null
    private var searchProgressBar: ProgressBar? = null

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var bottomSheetScrimView: BottomSheetScrimView? = null
    private var productRecyclerView: RecyclerView? = null
    private var bottomSheetTitleView: TextView? = null
    private var slidingSheetUpFromHiddenState: Boolean = false

    /**
     * need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraXLivePreviewFragment.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")

/*
                val isImageFlipped =
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = view.display.rotation
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                    )
                } else {
                    graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                    )
                }


                imageAnalyzer?.targetRotation = view.display.rotation*/
            }
        } ?: Unit
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        if (savedInstanceState != null) {
            lensFacing =
                    savedInstanceState.getInt(
                            STATE_LENS_FACING,
                            CameraSelector.LENS_FACING_FRONT
                    )
        }
        val binding = FragmentCameraBinding.inflate(inflater, container, false)

        previewView = binding.cameraPreview
        //displayId = previewView.display.displayId

        graphicOverlay = binding.cameraPreviewOverlay.cameraPreviewGraphicOverlay

        promptChip = binding.cameraPreviewOverlay.bottomPromptChip
        promptChipAnimator =
                (AnimatorInflater.loadAnimator(requireContext(), R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                    setTarget(promptChip)
                }
        searchProgressBar = binding.cameraPreviewOverlay.searchProgressBar

        setUpBottomSheet(binding)



        binding.topActionBar.topBarClickListener = this@CameraXLivePreviewFragment

        if (!allPermissionsGranted()) {
            runtimePermissions
        }

        return binding.root
    }


    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Every time the orientation of device changes, update rotation for use cases
        //displayManager.registerDisplayListener(displayListener, null)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupCamera()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        imageAnalyzer.stop()
        cameraExecutor.shutdown()
        //displayManager.unregisterDisplayListener(displayListener)

    }

    //preview and analysis usecases
    private fun bindCameraUseCases() {

        //val rotation = viewFinder.display.rotation
        val cameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)


        val preview = Preview.Builder()
                //.setTargetRotation(rotation)
                .setTargetAspectRatio(screenAspectRatio)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }


        cameraOverlayController = CameraOverlayController(graphicOverlay)

        imageAnalyzer = ImageAnalyzer(cameraOverlayController::setImageInfo,
                cameraOverlayController::objectBoxOverlapsConfirmationReticle,
                ContextCompat.getMainExecutor(requireContext()))
        {
            lifecycleScope.launch {
                viewIntentChannel.send(it)
            }
        }

        val imageAnalysisUseCase = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                //.setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, imageAnalyzer)

                }

        //must unbind use-cases before rebinding them
        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysisUseCase)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    private val requiredPermissions: Array<String?>
        get() = try {
            val info = requireContext().packageManager
                    .getPackageInfo(requireContext().packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(requireContext(), permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(requireContext(), permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        allNeededPermissions.toTypedArray(),
                        PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            lifecycleScope.launch {
                viewIntentChannel.send(CameraViewIntent.StartDetecting)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.bottom_sheet_scrim_view -> bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
            R.id.close_button -> {
                //TODO
                Log.v("TAGGG", "TODO close")
            }
            R.id.flash_button -> {
                if (flashButton?.isSelected == true) {
                    flashButton?.isSelected = false
                } else {
                    flashButton?.isSelected = true
                }
            }
            R.id.settings_button -> {
                settingsButton?.isEnabled = false
                //startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }


    private fun stopCameraPreview() {
        cameraProvider?.unbindAll()
    }

    private fun setUpBottomSheet(binding: FragmentCameraBinding) {

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        Log.v(TAG, "setupBS " + bottomSheetBehavior)
        bottomSheetBehavior?.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.d(TAG, "Bottom sheet new state: $newState")
                        bottomSheetScrimView?.visibility =
                                if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
                        graphicOverlay?.clear()

                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> {
                                lifecycleScope.launch {
                                    viewIntentChannel.send(CameraViewIntent.StartDetecting)
                                }
                            }
                            BottomSheetBehavior.STATE_COLLAPSED,
                            BottomSheetBehavior.STATE_EXPANDED,
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> slidingSheetUpFromHiddenState = false
                            BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                        Log.v(TAG, "onSliiide")
                        if (java.lang.Float.isNaN(slideOffset)) {
                            return
                        }


                        val bottomSheetBehavior = bottomSheetBehavior ?: return
                        val collapsedStateHeight = bottomSheetBehavior.peekHeight.coerceAtMost(bottomSheet.height)

                        if (slidingSheetUpFromHiddenState) {

                            bottomSheetScrimView?.translateAndScaleThumbnail(
                                    collapsedStateHeight,
                                    slideOffset
                            )
                        } else {
                            bottomSheetScrimView?.updateWithThumbnailTranslate(collapsedStateHeight, slideOffset, bottomSheet
                            )
                        }
                    }
                })

        bottomSheetScrimView = binding.bottomSheetScrimView.apply {
            setOnClickListener(this@CameraXLivePreviewFragment)
        }

        productRecyclerView = binding.bottomSheetIncluded.productRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable
        {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            if (allPermissionsGranted()) {
                bindCameraUseCases()

            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun renderState(state: CameraViewState) {

        val wasPromptChipGone = promptChip!!.visibility == View.GONE

        Log.v(TAG, "renderState " + state)
        searchProgressBar?.visibility = View.GONE
        when (state) {
            is CameraViewState.Idle -> {
                stopCameraPreview()
            }
            is CameraViewState.Detecting -> {

                //setupCamera()
                //startCameraPreview()

                settingsButton?.isEnabled = true
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_point_at_an_object)

                cameraOverlayController.renderStartDetectingOverlay()

            }


            is CameraViewState.OnProductsFound -> {

                stopCameraPreview()
                promptChip?.visibility = View.GONE

                val productList = state.products

                bottomSheetTitleView?.text = resources
                        .getQuantityString(
                                R.plurals.bottom_sheet_title, productList.size, productList.size
                        )
                val clickListener = ProductPreviewClickListener {
                    lifecycleScope.launch {
                        //maybe show loading in bottomsheet here
                        viewIntentChannel.send(CameraViewIntent.OnProductClickedInBottomSheet(requireContext(),
                                bottomSheetScrimView?.getThumbnailBitmap(), it))
                    }
                }
                productRecyclerView?.adapter = BottomSheetProductAdapter(productList, clickListener)

                slidingSheetUpFromHiddenState = true
                bottomSheetBehavior?.peekHeight = previewView.height?.div(2) ?: BottomSheetBehavior.PEEK_HEIGHT_AUTO
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            //may be effect?
            is CameraViewState.ProductAdded -> {
                //TODO
                //finish() or restart preview
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }

        }

        val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
        if (shouldPlayPromptChipEnteringAnimation && promptChipAnimator?.isRunning == false) {
            promptChipAnimator?.start()
        }
    }

    override fun renderEffect(effect: CameraViewEffect) {

        Log.v(TAG, "renderEffect" + effect)

        when (effect) {

            is CameraViewEffect.OnMovedAwayFromDetectedObject -> {

                cameraOverlayController.renderOnMovedAwayFromDetectedObjectOverlay(effect.boundingBox)

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_point_at_an_object)
                //startCameraPreview()
            }

            is CameraViewEffect.OnConfirmingDetectedObject -> {
                cameraOverlayController
                        .renderConfirmingObjectOverlay(effect.boundingBox, effect.progress)

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_hold_camera_steady)

                //startCameraPreview()
            }
            is CameraViewEffect.OnConfirmedDetectedObject -> {

                cameraOverlayController.renderOnConfirmedObjectOverlay(effect.boundingBox)

                //do only once for thumb and img proc
                if(bottomSheetScrimView?.getThumbnailBitmap() == null){

                    searchProgressBar?.visibility = View.VISIBLE
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_searching)
                    stopCameraPreview()

                    val objectThumbnailCornerRadius: Int = resources.getDimensionPixelOffset(R.dimen.bounding_box_corner_radius)


                    /*val detbitmap

                    val createdBitmap = Bitmap.createBitmap(
                            effect.detectedObjectBitmap,
                            effect.boundingBox.left,
                            effect.boundingBox.top,
                            effect.boundingBox.width(),
                            effect.boundingBox.height()
                    )
                    if (createdBitmap.width > DetectedObjectInfo.MAX_IMAGE_WIDTH) {
                        val dstHeight = (DetectedObjectInfo.MAX_IMAGE_WIDTH.toFloat() / createdBitmap.width * createdBitmap.height).toInt()
                        detbitmap = Bitmap.createScaledBitmap(createdBitmap, DetectedObjectInfo.MAX_IMAGE_WIDTH, dstHeight, /* filter= */ false)

                    }*/

                    val thumb = Utils.getCornerRoundedBitmap(effect.detectedObjectBitmap, objectThumbnailCornerRadius)
                    bottomSheetScrimView?.updateThumbnail(thumb)


                    bottomSheetScrimView?.updateSrcThumb(graphicOverlay.translateRect(effect.boundingBox))


                    //start image processor here
                    imageAnalyzer.processBitmap(effect.detectedObjectBitmap)
                }


            }

        }
    }

    companion object {
        private const val TAG = "CameraXLivePreview"
        private const val PERMISSION_REQUESTS = 1

        private const val STATE_LENS_FACING = "lens_facing"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private fun isPermissionGranted(
                context: Context,
                permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }

        private fun aspectRatio(width: Int, height: Int): Int {
            val previewRatio = max(width, height).toDouble() / min(width, height)
            if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
                return AspectRatio.RATIO_4_3
            }
            return AspectRatio.RATIO_16_9
        }

    }

}



