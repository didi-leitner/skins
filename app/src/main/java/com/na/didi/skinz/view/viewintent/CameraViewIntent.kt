package com.na.didi.skinz.view.viewintent

import android.content.Context
import android.graphics.Bitmap
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.view.viewstate.ProductsResult


sealed class CameraViewIntent {

    object OnBottomSheetHidden: CameraViewIntent()
    object CameraReady : CameraViewIntent()

    data class OnProductClickedInBottomSheet(
        val context: Context,
        val bitmap: Bitmap?,
        val product: Product
    ) : CameraViewIntent()

    data class OnProductsFound(val productResult: ProductsResult) : CameraViewIntent()


}

