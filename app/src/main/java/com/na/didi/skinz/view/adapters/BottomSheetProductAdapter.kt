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

/** Presents the list of product items from cloud product search.  */
class BottomSheetProductAdapter(private val productList: List<Product>) : Adapter<BottomSheetProductAdapter.ProductViewHolder>() {

    class ProductViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView = view.findViewById(R.id.product_image)
        private val titleView: TextView = view.findViewById(R.id.product_title)
        private val subtitleView: TextView = view.findViewById(R.id.product_subtitle)
        //private val imageSize: Int = view.resources.getDimensionPixelOffset(R.dimen.product_item_image_size)

        fun bindProduct(product: Product) {
            imageView.setImageDrawable(null)
            if (!TextUtils.isEmpty(product.imageUrl)) {
                //TODO Glide
                //ImageDownloadTask(imageView, imageSize).execute(product.imageUrl)
            } else {
                imageView.setImageResource(R.drawable.logo_google_cloud)
            }
            titleView.text = product.title
            subtitleView.text = product.subtitle
        }

        companion object {
            fun create(parent: ViewGroup) =
                    ProductViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
            ProductViewHolder.create(parent)

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bindProduct(productList[position])
    }

    override fun getItemCount(): Int = productList.size
}
