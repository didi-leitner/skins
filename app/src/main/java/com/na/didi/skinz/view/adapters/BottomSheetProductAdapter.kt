package com.na.didi.skinz.view.adapters


import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.na.didi.skinz.R
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.view.viewintent.CameraXViewIntent

/** Presents the list of product items from cloud product search.  */
class BottomSheetProductAdapter(private val productList: List<Product>, val cameraViewIntent: CameraXViewIntent)
    : Adapter<BottomSheetProductAdapter.ProductViewHolder>() {

    class ProductViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView = view.findViewById(R.id.product_image)
        private val titleView: TextView = view.findViewById(R.id.product_title)
        private val subtitleView: TextView = view.findViewById(R.id.product_subtitle)
        private val parent: ViewGroup = view.findViewById(R.id.product_item_layout)
        //private val imageSize: Int = view.resources.getDimensionPixelOffset(R.dimen.product_item_image_size)

        fun bindProduct(product: Product, viewIntent: CameraXViewIntent) {
            imageView.setImageDrawable(null)
            if (!TextUtils.isEmpty(product.imagePath)) {
                //TODO Glide
                //ImageDownloadTask(imageView, imageSize).execute(product.imageUrl)
            } else {
                imageView.setImageResource(R.drawable.logo_google_cloud)
            }
            titleView.text = product.title
            subtitleView.text = product.subtitle
            parent.setOnClickListener { view ->
                viewIntent.onProductClicked.value = product
            }

        }

        companion object {
            fun create(parent: ViewGroup) =
                    ProductViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
            ProductViewHolder.create(parent)

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bindProduct(productList[position], cameraViewIntent)
    }

    override fun getItemCount(): Int = productList.size
}
