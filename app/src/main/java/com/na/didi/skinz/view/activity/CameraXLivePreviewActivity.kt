package com.na.didi.skinz.view.activity


import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.na.didi.skinz.util.Event
import com.na.didi.skinz.view.adapters.BottomSheetProductAdapter
import com.na.didi.skinz.view.custom.BottomSheetScrimView
import com.na.didi.skinz.view.viewcontract.CameraXPreviewViewContract
import com.na.didi.skinz.view.viewintent.CameraXViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewState
import com.na.didi.skinz.viewmodel.CameraXViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class CameraXLivePreviewActivity :
        AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener,
        CameraXPreviewViewContract {

    private val cameraXViewModel: CameraXViewModel by viewModels()
    private val cameraXViewIntent = CameraXViewIntent()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

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

        setContentView(R.layout.fragment_camera)

        previewView = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            setOnClickListener(this@CameraXLivePreviewActivity)
        }

        promptChip = findViewById(R.id.bottom_prompt_chip)
        promptChipAnimator =
                (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                    setTarget(promptChip)
                }
        searchProgressBar = findViewById(R.id.search_progress_bar)

        setUpBottomSheet()

        findViewById<View>(R.id.close_button).setOnClickListener(this)
        flashButton = findViewById<View>(R.id.flash_button).apply {
            setOnClickListener(this@CameraXLivePreviewActivity)
        }
        settingsButton = findViewById<View>(R.id.settings_button).apply {
            setOnClickListener(this@CameraXLivePreviewActivity)
        }
        /*val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)
         */

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        setupCameraProvider()
        cameraXViewModel.bindViewIntents(this@CameraXLivePreviewActivity)


        if (!allPermissionsGranted()) {
            runtimePermissions
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }


    public override fun onResume() {
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

    public override fun onDestroy() {
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

            prominentObjectImageProcessor = ProminentObjectDetectorProcessor(cameraXViewIntent, graphicOverlay)


            needUpdateGraphicOverlayImageSourceInfo = true

            val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

            val builder = ImageAnalysis.Builder()
            builder.setTargetAspectRatio(screenAspectRatio)
            analysisUseCase = builder.build()
            analysisUseCase?.setAnalyzer(
                    ContextCompat.getMainExecutor(this),
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
                                    applicationContext,
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
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")


        val builder = Preview.Builder()
        builder.setTargetAspectRatio(screenAspectRatio)
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView.getSurfaceProvider())

        Log.v(TAG,"bindPreviewUseCase " + previewView.getSurfaceProvider() + " " + cameraProvider)
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */this, cameraSelector!!, previewUseCase)

        previewUseCase!!.setSurfaceProvider(previewView.getSurfaceProvider())

    }


    private fun bindAllCameraUseCases() {
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
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
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
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
            /*R.id.product_search_button -> {
                searchButton?.isEnabled = false
                cameraXViewModel?.onSearchButtonClicked()
            }*/
            R.id.bottom_sheet_scrim_view -> bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
            R.id.close_button -> onBackPressed()
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
        Log.v("UUU","startCameraPreview " + cameraXViewModel.isCameraLive)
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

    private fun setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))
        bottomSheetBehavior?.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.d(TAG, "Bottom sheet new state: $newState")
                        bottomSheetScrimView?.visibility =
                                if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
                        graphicOverlay?.clear()

                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> {
                                cameraXViewIntent.onBottomSheetHidden.value = Event(true)
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

        bottomSheetScrimView = findViewById<BottomSheetScrimView>(R.id.bottom_sheet_scrim_view).apply {
            setOnClickListener(this@CameraXLivePreviewActivity)
        }

        bottomSheetTitleView = findViewById(R.id.bottom_sheet_title)
        productRecyclerView = findViewById<RecyclerView>(R.id.product_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CameraXLivePreviewActivity)
            adapter = BottomSheetProductAdapter(ImmutableList.of())
        }
    }

    private fun setupCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable
        {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            if (allPermissionsGranted()) {
                Log.v(TAG, "Bind all camera usecases from camPRvoderFuture")
                bindAllCameraUseCases()
            }

        }, ContextCompat.getMainExecutor(this))
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

    override fun initState() = MutableStateFlow(true)

    override fun onBottomSheetHidden() = cameraXViewIntent.onBottomSheetHidden.filterNotNull()

    override fun onMovedAwayFromDetectedObject() = cameraXViewIntent.onMovedAwayFromDetectedObject.filterNotNull()

    override fun onConfirmedDetectedObjectWithCameraHold() = cameraXViewIntent.onConfirmedDetectedObject.filterNotNull()

    override fun onConfirmingDetectedObject() = cameraXViewIntent.onConfirmingDetectedObject.filterNotNull()

    override fun onNothingFoundInFrame() = cameraXViewIntent.onNothingFoundInFrame.filterNotNull()

    override fun onTextDetected() = cameraXViewIntent.onTextDetected.filterNotNull()

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

                //bindTextRecognitionUseCase(cameraViewState.bitmap)
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
                productRecyclerView?.adapter = BottomSheetProductAdapter(productList)
                slidingSheetUpFromHiddenState = true
                bottomSheetBehavior?.peekHeight = previewView.height?.div(2) ?: BottomSheetBehavior.PEEK_HEIGHT_AUTO
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
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

