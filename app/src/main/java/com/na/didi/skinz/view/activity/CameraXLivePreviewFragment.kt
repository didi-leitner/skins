package com.na.didi.skinz.view.activity


import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.annotation.KeepName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.common.collect.ImmutableList
import com.google.mlkit.common.MlKitException
import com.na.didi.skinz.R
import com.na.didi.skinz.camera.GraphicOverlay
import com.na.didi.skinz.camera.imageprocessor.ProminentObjectDetectorProcessor
import com.na.didi.skinz.databinding.FragmentCameraBinding
import com.na.didi.skinz.view.adapters.BottomSheetProductAdapter
import com.na.didi.skinz.view.adapters.ProductPreviewClickListener
import com.na.didi.skinz.view.custom.BottomSheetScrimView
import com.na.didi.skinz.view.viewcontract.CameraXPreviewViewContract
import com.na.didi.skinz.view.viewintent.CameraXViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewState
import com.na.didi.skinz.viewmodel.CameraXViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class CameraXLivePreviewFragment :
        Fragment(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener,
        CameraXPreviewViewContract {

    private val cameraXViewModel: CameraXViewModel by viewModels()
    private val viewIntentChannel = Channel<CameraXViewIntent>(Channel.CONFLATED)

    private var cameraProvider: ProcessCameraProvider? = null

    private var analysisUseCase: ImageAnalysis? = null
    private var previewUseCase: Preview? = null

    private var prominentObjectImageProcessor: ProminentObjectDetectorProcessor? = null

    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var cameraSelector: CameraSelector
    private lateinit var previewView: PreviewView

    private var needUpdateGraphicOverlayImageSourceInfo = false

    private var selectedModel = OBJECT_DETECTION
    private var lensFacing = CameraSelector.LENS_FACING_BACK


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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        if (savedInstanceState != null) {
            selectedModel =
                    savedInstanceState.getString(
                            STATE_SELECTED_MODEL,
                            OBJECT_DETECTION
                    )
            lensFacing =
                    savedInstanceState.getInt(
                            STATE_LENS_FACING,
                            CameraSelector.LENS_FACING_FRONT
                    )
        }
        val binding = FragmentCameraBinding.inflate(inflater, container, false)


        previewView = binding.cameraPreview
        graphicOverlay = binding.cameraPreviewOverlay.cameraPreviewGraphicOverlay.apply {
            setOnClickListener(this@CameraXLivePreviewFragment)
        }

        promptChip = binding.cameraPreviewOverlay.bottomPromptChip
        promptChipAnimator =
                (AnimatorInflater.loadAnimator(requireContext(), R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                    setTarget(promptChip)
                }
        searchProgressBar = binding.cameraPreviewOverlay.searchProgressBar

        setUpBottomSheet(binding)

        binding.topActionBar.closeButton.setOnClickListener(this)

        flashButton = binding.topActionBar.flashButton.apply {
            setOnClickListener(this@CameraXLivePreviewFragment)
        }
        settingsButton = binding.topActionBar.settingsButton.apply {
            setOnClickListener(this@CameraXLivePreviewFragment)
        }


        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        setupCameraProvider()
        cameraXViewModel.bindViewIntents(this@CameraXLivePreviewFragment)


        if (!allPermissionsGranted()) {
            runtimePermissions
        }

        return binding.root


    }


    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }


    override fun onResume() {
        super.onResume()

        cameraXViewModel.markCameraFrozen()
        bindAllCameraUseCases()
        cameraXViewModel.markCameraLive()

    }

    override fun onPause() {
        super.onPause()

        prominentObjectImageProcessor?.run {
            this.stop()
        }

        cameraXViewModel.markCameraFrozen()

    }

    override fun onDestroy() {
        super.onDestroy()

        prominentObjectImageProcessor?.run {
            this.stop()
        }

    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider != null) {

            if(analysisUseCase != null)
                cameraProvider!!.unbind(analysisUseCase)

            if (prominentObjectImageProcessor != null) {
                prominentObjectImageProcessor!!.stop()
            }


            prominentObjectImageProcessor = ProminentObjectDetectorProcessor({
                lifecycleScope.launch{
                    viewIntentChannel.send(it)
                }
            } , graphicOverlay)


            needUpdateGraphicOverlayImageSourceInfo = true

            val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

            val builder = ImageAnalysis.Builder()
            builder.setTargetAspectRatio(screenAspectRatio)
            analysisUseCase = builder.build()
            analysisUseCase?.setAnalyzer(
                    ContextCompat.getMainExecutor(requireContext()),
                    { imageProxy: ImageProxy ->

                        if (needUpdateGraphicOverlayImageSourceInfo) {
                            val isImageFlipped =
                                    lensFacing == CameraSelector.LENS_FACING_FRONT
                            val rotationDegrees =
                                    imageProxy.imageInfo.rotationDegrees
                            if (rotationDegrees == 0 || rotationDegrees == 180) {
                                graphicOverlay.setImageSourceInfo(
                                        imageProxy.width, imageProxy.height, isImageFlipped
                                )
                            } else {
                                graphicOverlay.setImageSourceInfo(
                                        imageProxy.height, imageProxy.width, isImageFlipped
                                )
                            }
                            needUpdateGraphicOverlayImageSourceInfo = false
                        }

                        try {
                            Log.v(TAG,"will call processImageProxy")
                            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                            prominentObjectImageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                        } catch (e: MlKitException) {
                            Log.e(
                                    TAG,
                                    "Failed to process image. Error: " + e.localizedMessage
                            )
                            Toast.makeText(
                                    requireContext(),
                                    e.localizedMessage,
                                    Toast.LENGTH_SHORT
                            )
                                    .show()
                        }

                        needUpdateGraphicOverlayImageSourceInfo = true

                    })

            Log.v(TAG, "calling bindToLifecycle " + cameraSelector + " " + analysisUseCase)

            cameraProvider!!.bindToLifecycle(this, cameraSelector!!, analysisUseCase)
        }
    }

    private fun bindPreviewUseCase() {

        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)


        val builder = Preview.Builder()
        builder.setTargetAspectRatio(screenAspectRatio)
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView.getSurfaceProvider())

        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */this, cameraSelector!!, previewUseCase)

        previewUseCase!!.setSurfaceProvider(previewView.getSurfaceProvider())

    }


    private fun bindAllCameraUseCases() {
        bindPreviewUseCase()
        bindAnalysisUseCase()
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
            startCameraPreview()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.bottom_sheet_scrim_view -> bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
            R.id.close_button -> TODO()
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

    private fun startCameraPreview() {
        if (!cameraXViewModel.isCameraLive) {
            try {
                bindAllCameraUseCases()
                cameraXViewModel.markCameraLive()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start camera preview!", e)
            }
        }
    }

    private fun stopCameraPreview() {
        //TODO
        if (cameraXViewModel?.isCameraLive == true) {
            cameraXViewModel!!.markCameraFrozen()
            flashButton?.isSelected = false
            cameraProvider?.unbindAll()
        }
    }

    private fun setUpBottomSheet(binding: FragmentCameraBinding) {

        //binding.bottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
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
                                    viewIntentChannel.send(CameraXViewIntent.OnBottomSheetHidden)
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


        bottomSheetTitleView = binding.bottomSheet.bottomSheetTitle
        productRecyclerView = binding.bottomSheet.productRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())

            val clickListener = ProductPreviewClickListener{
                lifecycleScope.launch {
                    viewIntentChannel.send(CameraXViewIntent.OnProductClicked(it))
                }
            }
            adapter = BottomSheetProductAdapter(ImmutableList.of(), clickListener)
        }
    }

    private fun setupCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable
        {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            if (allPermissionsGranted()) {
                Log.v(TAG, "Bind all camera usecases from camPRvoderFuture")
                bindAllCameraUseCases()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    companion object {
        private const val TAG = "CameraXLivePreview"
        private const val PERMISSION_REQUESTS = 1

        private const val OBJECT_DETECTION = "Object Detection"
        private const val FACE_DETECTION = "Face Detection"
        private const val TEXT_RECOGNITION = "Text Recognition"

        private const val STATE_SELECTED_MODEL = "selected_model"
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


    override fun viewIntentFlow() = viewIntentChannel.receiveAsFlow().filterNotNull()

    override fun render(cameraViewState: CameraViewState) {

        val wasPromptChipGone = promptChip!!.visibility == View.GONE

        searchProgressBar?.visibility = View.GONE
        when(cameraViewState) {
            is CameraViewState.Idle -> {

            }
            is CameraViewState.Detecting -> {

                settingsButton?.isEnabled = true
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_point_at_an_object)
                startCameraPreview()
            }
            is CameraViewState.Detected -> {
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_point_at_an_object)
                //startCameraPreview()
            }
            is CameraViewState.Confirming -> {
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_hold_camera_steady)
                //startCameraPreview()
            }
            is CameraViewState.Confirmed -> {
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_searching)
                stopCameraPreview()
            }
            is CameraViewState.Searching -> {
                searchProgressBar?.visibility = View.VISIBLE
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_searching)
                stopCameraPreview()
            }
            is CameraViewState.Searched -> {
                promptChip?.visibility = View.GONE
                stopCameraPreview()

                val searchedObject = cameraViewState.searchedObject

                val productList = searchedObject.productList
                bottomSheetScrimView?.updateThumbnail(searchedObject.getObjectThumbnail(resources))
                bottomSheetScrimView?.updateSrcThumb(graphicOverlay.translateRect(searchedObject.boundingBox))

                bottomSheetTitleView?.text = resources
                        .getQuantityString(
                                R.plurals.bottom_sheet_title, productList.size, productList.size
                        )
                val clickListener = ProductPreviewClickListener{
                    lifecycleScope.launch {
                        viewIntentChannel.send(CameraXViewIntent.OnProductClicked(it))
                    }
                }
                productRecyclerView?.adapter = BottomSheetProductAdapter(productList, clickListener)
                slidingSheetUpFromHiddenState = true
                bottomSheetBehavior?.peekHeight = previewView.height?.div(2) ?: BottomSheetBehavior.PEEK_HEIGHT_AUTO
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            is CameraViewState.SearchedProductConfirmed -> {

                lifecycleScope.launch {
                    //TOODO inject Context in BitmapUtils
                    viewIntentChannel.send(CameraXViewIntent.AddProduct(requireContext(), bottomSheetScrimView?.getThumbnailBitmap(), cameraViewState.product))

                }

            }

            is CameraViewState.ProductAdded -> {
                //TODO nav event
                //finish()
                //bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }

        }


        /*when (cameraViewState) {
            CameraViewState.Detecting, CameraViewState.DETECTED, CameraViewState.CONFIRMING -> {
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(
                        if (cameraViewState == CameraViewState.CONFIRMING)
                            R.string.prompt_hold_camera_steady
                        else
                            R.string.prompt_point_at_an_object
                )
                startCameraPreview()
            }
            CameraViewState.CONFIRMED -> {
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_searching)
                stopCameraPreview()
            }
            CameraViewState.SEARCHING -> {
                searchProgressBar?.visibility = View.VISIBLE
                promptChip?.visibility = View.VISIBLE
                promptChip?.setText(R.string.prompt_searching)
                stopCameraPreview()
            }
            CameraViewState.SEARCHED -> {
                promptChip?.visibility = View.GONE
                stopCameraPreview()
            }
            else -> promptChip?.visibility = View.GONE
        }*/

        val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
        if (shouldPlayPromptChipEnteringAnimation && promptChipAnimator?.isRunning == false) {
            promptChipAnimator?.start()
        }
    }



}

