package com.na.didi.skinz.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.util.onEachEvent
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.viewcontract.CameraXPreviewViewContract
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
class CameraXViewModel @ViewModelInject internal constructor(
        productsRepo: ProductsRepo
) : ViewModel() {

    private val productsRepo: ProductsRepo = productsRepo
    private val cameraViewState = MutableStateFlow<CameraViewState>(CameraViewState.Idle())


    var isCameraLive = false
        private set


    fun bindViewIntents(viewContract: CameraXPreviewViewContract) {

        viewContract.initState().onEach {

            cameraViewState.filterNotNull().collect {
                Log.v("TAGGG", "collect state, render " + it)
                viewContract.render(it)
            }
            Log.v("TAGGG", "onEach, after collect call")

        }.launchIn(viewModelScope)

        viewContract.onBottomSheetHidden().onEachEvent {
            cameraViewState.value = CameraViewState.Detecting()
        }.launchIn(viewModelScope)

        viewContract.onProductConfirmedWithClick().onEach {

            Log.v("ESTE", "Product: " + it.title + " " + it.subtitle)
            //hide bottom sheet
            //add product to myProducts - title/subtitle/imagepath
            //search for ingredients

            //save bitmap
            cameraViewState.value = CameraViewState.SearchedProductConfirmed(it)


        }.launchIn(viewModelScope)


        //TODO sep cameraContract?
        viewContract.onNothingFoundInFrame().onEachEvent {
            cameraViewState.value = CameraViewState.Detecting()
        }.launchIn(viewModelScope)

        viewContract.onMovedAwayFromDetectedObject().onEachEvent {
            cameraViewState.value = CameraViewState.Detected()
        }.launchIn(viewModelScope)

        viewContract.onConfirmingDetectedObject().onEachEvent {
            cameraViewState.value = CameraViewState.Confirming()
        }.launchIn(viewModelScope)

        viewContract.onConfirmedDetectedObjectWithCameraHold().onEach {
            cameraViewState.value = CameraViewState.Searching(it)

        }.launchIn(viewModelScope)

        viewContract.onTextDetected().onEach {
            if(it != null) {
                cameraViewState.value = CameraViewState.Searched(it)
            }

        }.launchIn(viewModelScope)
    }

    fun addProduct(context: Context, bitmap: Bitmap?, product: Product) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //save bitmap to app-private storage
                //TODO scoped storage?
                if(bitmap != null) {
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

