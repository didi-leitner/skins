package com.na.didi.skinz.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.viewcontract.CameraXPreviewViewContract
import com.na.didi.skinz.view.viewintent.CameraXViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CameraXViewModel @ViewModelInject internal constructor(
        productsRepo: ProductsRepo
) : ViewModel() {

    private val productsRepo: ProductsRepo = productsRepo
    private val cameraViewState = MutableStateFlow<CameraViewState>(CameraViewState.Idle())


    var isCameraLive = false
        private set


    fun bindViewIntents(viewContract: CameraXPreviewViewContract) {

        viewModelScope.launch {

            cameraViewState.filterNotNull().collect {
                Log.v("TAGGG", "collect state, render " + it)
                viewContract.render(it)
            }
            Log.v("TAGGG", "onEach, after collect call")

        }

        viewModelScope.launch {
            viewContract.viewIntentFlow().collect {

                when (it) {
                    CameraXViewIntent.OnBottomSheetHidden -> CameraViewState.Detecting()
                    CameraXViewIntent.OnNothingFoundInFrame ->
                        cameraViewState.value = CameraViewState.Detecting()

                    CameraXViewIntent.OnMovedAwayFromDetectedObject ->
                        cameraViewState.value = CameraViewState.Detected()

                    is CameraXViewIntent.OnConfirmedDetectedObjec ->
                        cameraViewState.value = CameraViewState.Searching(it.detectedObjectInfo!!)

                    CameraXViewIntent.OnConfirmingDetectedObject ->
                        cameraViewState.value = CameraViewState.Confirming()

                    is CameraXViewIntent.OnTextDetected ->
                        cameraViewState.value = CameraViewState.Searched(it.searchedObject)

                    is CameraXViewIntent.OnProductClicked ->
                        cameraViewState.value = CameraViewState.SearchedProductConfirmed(it.product)

                    is CameraXViewIntent.AddProduct -> addProduct(it.context, it.bitmap, it.product)

                }
            }
        }


    }

    private fun addProduct(context: Context, bitmap: Bitmap?, product: Product) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //save bitmap to app-private storage
                //TODO scoped storage?
                if (bitmap != null) {
                    val uri = BitmapUtils.saveBitmapToAppPrivateStorage(context, bitmap,
                            "my_products", System.currentTimeMillis().toString())

                    product.imagePath = uri.toString()
                }
                productsRepo.insertProduct(product)

                cameraViewState.value = CameraViewState.ProductAdded()
            }
        }
    }


    fun markCameraLive() {
        isCameraLive = true
        cameraViewState.value = CameraViewState.Detecting()

    }

    fun markCameraFrozen() {
        isCameraLive = false
        cameraViewState.value = CameraViewState.Idle()

    }


}

