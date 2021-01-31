package com.na.didi.skinz.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.na.didi.skinz.camera.DetectedObjectInfo
import com.na.didi.skinz.data.db.dao.UploadsDao
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.data.model.UploadsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadsRepo @Inject constructor(private val uploadsDao: UploadsDao) {

    fun getUploads(scope: CoroutineScope): Flow<PagingData<UploadsModel>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DB_PAGE_SIZE),
            pagingSourceFactory = { uploadsDao.getUploadsPaged() }
        ).flow.cachedIn(scope)
    }

    fun insertUpload(upload : UploadsModel) {
        uploadsDao.addUpload(upload)
    }

    fun search(
            detectedObject: DetectedObjectInfo,
            listener: (detectedObject: DetectedObjectInfo, productList: List<Product>) -> Unit
    ) {
        // Crops the object image out of the full image is expensive, so do it off the UI thread.
        /*Tasks.call<JsonObjectRequest>(requestCreationExecutor, Callable { createRequest(detectedObject) })
                .addOnSuccessListener { productRequest -> searchRequestQueue.add(productRequest.setTag(TAG)) }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to create product search request!", e)
                    // Remove the below dummy code after your own product search backed hooked up.
                    val productList = ArrayList<Product>()
                    for (i in 0..7) {
                        productList.add(
                                Product(/* imageUrl= */"", "Product title $i", "Product subtitle $i")
                        )
                    }
                    listener.invoke(detectedObject, productList)
                }*/

        //TODO
        val productList = ArrayList<Product>()
        for (i in 0..7) {
            productList.add(
                    Product(/* imageUrl= */"", "Product title $i", "Product subtitle $i")
            )
        }
        listener.invoke(detectedObject, productList)

    }

    companion object {
        private const val DB_PAGE_SIZE = 10
    }



}