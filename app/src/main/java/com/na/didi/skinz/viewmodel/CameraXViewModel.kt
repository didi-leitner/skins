package com.na.didi.skinz.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.repository.ProductsRepo
import com.na.didi.skinz.utils.BitmapUtils
import com.na.didi.skinz.view.viewintent.CameraViewIntent
import com.na.didi.skinz.view.viewstate.CameraState
import com.na.didi.skinz.view.viewstate.CameraViewEffect
import com.na.didi.skinz.view.viewstate.ProductsResult
import com.na.didi.skinz.view.viewstate.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CameraXViewModel @ViewModelInject internal constructor(
    val productsRepo: ProductsRepo
) : BaseViewModel<ViewState, CameraViewEffect, CameraViewIntent>() {


    override suspend fun bindViewIntents(viewItents: Flow<CameraViewIntent>) {

        viewItents.collect {
            Log.v("CameraXLive", "cam view intent collected " + it)

            when (it) {
                is CameraViewIntent.CameraReady -> {
                    _state.value?.let { viewState ->
                        //may be result of
                        _state.value = viewState.copy(cameraState = CameraState.Detecting())
                    } ?: run {
                        _state.value = ViewState(CameraState.Detecting(), ProductsResult(emptyList(), null, null))
                    }
                }

                is CameraViewIntent.OnProductsFound -> {
                    _state.value = _state.value?.let { currentViewState ->
                        currentViewState.copy(productResult = it.productResult)
                    }
                }

                is CameraViewIntent.OnProductClickedInBottomSheet -> {
                    //TODO check if ingr are to be found,
                    //show some kind of loadinng within BottomSheet
                    //pos -> addProducts
                    //neg -> askForIngredients
                    addProduct(it.context, it.bitmap, it.product)
                }

                is CameraViewIntent.OnBottomSheetHidden -> {
                    _state.value = ViewState(CameraState.Detecting(), ProductsResult(emptyList(), null, null))
                }

            }
        }
    }


    private fun addProduct(context: Context, bitmap: Bitmap?, product: Product) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //save bitmap to app-private storage TODO scoped storage?
                if (bitmap != null) {
                    val uri = BitmapUtils.saveBitmapToAppPrivateStorage(
                        context, bitmap,
                        "my_products", System.currentTimeMillis().toString()
                    )

                    product.imagePath = uri.toString()
                }
                productsRepo.insertProduct(product)

                _effect.send(CameraViewEffect.OnProductAdded(product))
            }
        }
    }


}

