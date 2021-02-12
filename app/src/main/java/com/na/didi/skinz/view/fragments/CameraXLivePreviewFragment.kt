package com.na.didi.skinz.view.fragments


import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.annotation.KeepName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.na.didi.skinz.R
import com.na.didi.skinz.camera.FrameAnalysisResult
import com.na.didi.skinz.camera.ImageAnalyzer
import com.na.didi.skinz.databinding.FragmentCameraBinding
import com.na.didi.skinz.utils.Utils
import com.na.didi.skinz.view.adapters.BottomSheetProductAdapter
import com.na.didi.skinz.view.adapters.ProductPreviewClickListener
import com.na.didi.skinz.view.camera_graphics.CameraOverlayController
import com.na.didi.skinz.view.custom.BottomSheetScrimView
import com.na.didi.skinz.view.custom.GraphicOverlay
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import com.na.didi.skinz.view.viewstate.CameraState
import com.na.didi.skinz.view.viewstate.CameraViewEffect
import com.na.didi.skinz.view.viewstate.ProductsResult
import com.na.didi.skinz.view.viewstate.ViewState
import com.na.didi.skinz.viewmodel.CameraXViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    BaseFragmentMVI<ViewState, CameraViewEffect, CameraViewIntent>(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    View.OnClickListener {

    override val viewModel: CameraXViewModel by viewModels()

    private lateinit var previewUseCase: Preview
    private lateinit var analysisUseCase: ImageAnalysis

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
            (AnimatorInflater.loadAnimator(
                requireContext(),
                R.animator.bottom_prompt_chip_enter
            ) as AnimatorSet).apply {
                setTarget(promptChip)
            }
        searchProgressBar = binding.cameraPreviewOverlay.searchProgressBar

        setUpBottomSheet(binding)



        binding.topActionBar.topBarClickListener = this@CameraXLivePreviewFragment

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

        if (!allPermissionsGranted()) {
            runtimePermissions
        } else {
            setupCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageAnalyzer.stop()
        cameraExecutor.shutdown()
        //displayManager.unregisterDisplayListener(displayListener)

    }

    //preview and analysis usecases
    private fun setupCameraUseCases() {

        //val rotation = viewFinder.display.rotation

        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        previewUseCase = Preview.Builder()
            //.setTargetRotation(rotation)
            .setTargetAspectRatio(screenAspectRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        cameraOverlayController = CameraOverlayController(graphicOverlay)

        //TODO interface for these 3 methods
        imageAnalyzer = ImageAnalyzer(
            graphicOverlay::invalidate,
            cameraOverlayController::setImageInfo,
            cameraOverlayController::objectBoxOverlapsConfirmationReticle,
            ContextCompat.getMainExecutor(requireContext())
        )
        {
            onFrameAnalysisResult(it)
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            //.setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, imageAnalyzer)

            }

    }

    private fun onFrameAnalysisResult(frameResult: FrameAnalysisResult) {
        Log.v(TAG, "FrameResult " + frameResult)

        //think about - does it make sense here that the viewModel has full control
        //maybe with a more reduced latency, i can go back to channel.send(Effect)
        //for smooth progress - CONFLATED view-intent-channel may bot be ok

        when (frameResult) {

            FrameAnalysisResult.OnNothingDetected ->
                renderEffect(CameraViewEffect.OnNothingDetected)
            is FrameAnalysisResult.OnConfirmingDetectedObject -> {
                renderEffect(
                    CameraViewEffect.OnConfirmingDetectedObject(
                        frameResult.boundingBox,
                        frameResult.progress
                    )
                )
            }
            is FrameAnalysisResult.OnObjectPicked -> {
                renderEffect(
                    CameraViewEffect.OnObjectPicked(
                        frameResult.boundingBox
                    )
                )
                stopCameraPreview()
            }

            is FrameAnalysisResult.OnTextDetected -> {
                lifecycleScope.launch {
                    delay(1000) //time to see detecting text
                    if (frameResult.text.textBlocks.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            val products = imageAnalyzer.processTextInProducts(frameResult.text)
                            //simulate network and db search
                            delay(2000)

                            viewIntentChannel.send(
                                CameraViewIntent.OnProductsFound(
                                    ProductsResult(
                                        products,
                                        frameResult.detectedObjectBitmap,
                                        frameResult.boundingBox
                                    )
                                )
                            )
                        }

                    } else {
                        promptChip?.setText(R.string.prompt_no_text)
                        lifecycleScope.launch {
                            delay(4000) //time  to read new prompt
                            viewIntentChannel.send(CameraViewIntent.CameraReady)
                        }

                    }
                }
            }
        }
    }

    private fun startCameraPreview() {

        Log.v(TAG, "startCameraPreview")

        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        //think about -> this will cause an ugly flicker  (unbind + bind),
        //there's no simple way to just 'pause' the preview
        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, analysisUseCase)

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }


    }

    private fun stopCameraPreview() {
        Log.v(TAG, "stopCameraPreview")
        cameraProvider?.unbindAll()
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
            setupCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.bottom_sheet_scrim_view ->
                bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
            R.id.close_button -> {
                //TODO
                Log.v("TAGGG", "TODO close")
            }
            R.id.flash_button -> {
                //TODO
            }
            R.id.settings_button -> {
                //TODO
            }
        }
    }


    private fun setUpBottomSheet(binding: FragmentCameraBinding) {

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
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
                                Log.v(TAG, "send StartDetecting from BotomSheet hidden")
                                viewIntentChannel.send(CameraViewIntent.OnBottomSheetHidden)
                            }
                        }
                        BottomSheetBehavior.STATE_COLLAPSED,
                        BottomSheetBehavior.STATE_EXPANDED,
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> slidingSheetUpFromHiddenState =
                            false
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    if (java.lang.Float.isNaN(slideOffset)) {
                        return
                    }


                    val bottomSheetBehavior = bottomSheetBehavior ?: return
                    val collapsedStateHeight =
                        bottomSheetBehavior.peekHeight.coerceAtMost(bottomSheet.height)

                    if (slidingSheetUpFromHiddenState) {

                        bottomSheetScrimView?.translateAndScaleThumbnail(
                            collapsedStateHeight,
                            slideOffset
                        )
                    } else {
                        bottomSheetScrimView?.updateWithThumbnailTranslate(
                            collapsedStateHeight, slideOffset, bottomSheet
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

        //if(cameraProvider != null) {
        //    bindCameraUseCases()
        //} else{


        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            Runnable
            {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                cameraProvider = cameraProviderFuture.get()

                if (allPermissionsGranted()) {
                    setupCameraUseCases()

                    lifecycleScope.launch {
                        Log.v(TAG, "send StartDetecting from bindCameraUseCases")
                        viewIntentChannel.send(CameraViewIntent.CameraReady)//CameraReady)
                    }
                }

            }, ContextCompat.getMainExecutor(requireContext())
        )
        //}

    }


    override fun renderState(state: ViewState) {

        val wasPromptChipGone = promptChip!!.visibility == View.GONE

        Log.v(TAG, "renderState " + state)
        searchProgressBar?.visibility = View.GONE

        if (state.productResult.products.isNotEmpty()) {

            stopCameraPreview()

            val objectThumbnailCornerRadius: Int =
                resources.getDimensionPixelOffset(R.dimen.bounding_box_corner_radius)

            if (state.productResult.bitmap != null && state.productResult.boundingBox != null) {
                val thumb = Utils.getCornerRoundedBitmap(
                    state.productResult.bitmap,
                    objectThumbnailCornerRadius
                )
                bottomSheetScrimView?.updateThumbnail(thumb)
                bottomSheetScrimView?.updateSrcThumb(graphicOverlay.translateRect(state.productResult.boundingBox))
            }



            promptChip?.visibility = View.GONE

            val productList = state.productResult.products

            bottomSheetTitleView?.text = resources
                .getQuantityString(
                    R.plurals.bottom_sheet_title, productList.size, productList.size
                )
            val clickListener = ProductPreviewClickListener {
                lifecycleScope.launch {
                    //maybe show loading in bottomsheet here
                    viewIntentChannel.send(
                        CameraViewIntent.OnProductClickedInBottomSheet(
                            requireContext(),
                            bottomSheetScrimView?.getThumbnailBitmap(), it
                        )
                    )
                }
            }
            productRecyclerView?.adapter = BottomSheetProductAdapter(productList, clickListener)

            slidingSheetUpFromHiddenState = true
            bottomSheetBehavior?.peekHeight =
                previewView.height.div(2)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        } else {

            when (state.cameraState) {

                CameraState.Idle -> {
                    stopCameraPreview()
                }
                is CameraState.Detecting -> {

                    cameraProvider?.let {

                        startCameraPreview()

                        settingsButton?.isEnabled = true
                        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

                        promptChip?.visibility = View.VISIBLE
                        promptChip?.setText(R.string.prompt_point_at_an_object)

                        val h: Handler = Handler()
                        h.postDelayed({
                            cameraOverlayController.renderDetectingOverlay()
                        }, 500)
                    }
                }
            }
        }


        val shouldPlayPromptChipEnteringAnimation =
            wasPromptChipGone && promptChip?.visibility == View.VISIBLE
        if (shouldPlayPromptChipEnteringAnimation && promptChipAnimator?.isRunning == false) {
            promptChipAnimator?.start()
        }
    }

    override fun renderEffect(effect: CameraViewEffect) {

        Log.v(TAG, "renderEffect " + effect)

        when (effect) {

            is CameraViewEffect.OnNothingDetected -> {
                cameraOverlayController.renderDetectingOverlay()

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_point_at_an_object)
            }

            is CameraViewEffect.OnConfirmingDetectedObject -> {
                cameraOverlayController
                    .renderConfirmingObjectOverlay(effect.boundingBox, effect.progress)

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_hold_camera_steady)

            }
            is CameraViewEffect.OnObjectPicked -> {

                cameraOverlayController.renderOnConfirmedObjectOverlay(effect.boundingBox)

                searchProgressBar?.visibility = View.VISIBLE
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_detecting_text)

            }

            is CameraViewEffect.OnProductAdded -> {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                val action =
                    CameraXLivePreviewFragmentDirections.actionCameraFragmentToHomeViewPager("products")

                findNavController().navigate(action)
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



