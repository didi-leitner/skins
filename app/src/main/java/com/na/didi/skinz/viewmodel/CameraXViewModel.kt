package com.na.didi.skinz.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import com.na.didi.skinz.view.viewstate.CameraViewEffect
import com.na.didi.skinz.view.viewstate.CameraViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CameraXViewModel @ViewModelInject internal constructor(
        val productsRepo: ProductsRepo
) : BaseViewModel<CameraViewState, CameraViewEffect, CameraViewIntent>() {


    var isCameraLive = false

    override fun bindViewIntents(coroutineScope: LifecycleCoroutineScope, viewItents: Flow<CameraViewIntent>) {

        coroutineScope.launch {
            viewItents.collect {
                Log.v("uuu","cam view intent collected " + it)

                when (it) {
                    is CameraViewIntent.StartDetecting ->{
                        _state.value = CameraViewState.Detecting
                    }
                    is CameraViewIntent.OnPointCameraToDetectedObject ->
                        _state.value = CameraViewState.Confirming

                    is CameraViewIntent.OnMovedAwayFromDetectedObject ->
                        _state.value = CameraViewState.Detected

                    is CameraViewIntent.OnConfirmedDetectedObject ->
                        _state.value = CameraViewState.Searching(it.detectedObjectInfo!!)

                    is CameraViewIntent.OnTextDetected ->
                        _state.value = CameraViewState.Searched(it.searchedObject)

                    is CameraViewIntent.OnProductClickedInBottomSheet -> {
                        //TODO check if ingr are to be found
                        //pos -> addProducts
                        //neg -> askForIngredients
                        _state.value = CameraViewState.SearchedProductConfirmed(it.product)

                    }

                    is CameraViewIntent.AddProduct -> addProduct(it.context, it.bitmap, it.product)
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

                _state.value = CameraViewState.ProductAdded
            }
        }
    }


    fun markCameraFrozen() {
        Log.v("UUU","markFroz")


    }


}

