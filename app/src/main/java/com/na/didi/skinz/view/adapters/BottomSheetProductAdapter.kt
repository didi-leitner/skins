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

class BottomSheetProductAdapter(private val productList: List<Product>, val clickListener: ProductPreviewClickListener)
    : Adapter<BottomSheetProductAdapter.ProductViewHolder>() {

    class ProductViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView = view.findViewById(R.id.product_image)
        private val titleView: TextView = view.findViewById(R.id.product_title)
        private val subtitleView: TextView = view.findViewById(R.id.product_subtitle)
        private val parent: ViewGroup = view.findViewById(R.id.product_item_layout)
        //private val imageSize: Int = view.resources.getDimensionPixelOffset(R.dimen.product_item_image_size)

        fun bindProduct(product: Product, clickListener: ProductPreviewClickListener) {
            imageView.setImageDrawable(null)
            if (!TextUtils.isEmpty(product.imagePath)) {
                //TODO ?
            } else {
                imageView.setImageResource(R.drawable.logo_google_cloud)
            }
            titleView.text = product.title
            subtitleView.text = product.subtitle
            parent.setOnClickListener {
                clickListener.onClick(product)
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
        holder.bindProduct(productList[position], clickListener)
    }

    override fun getItemCount(): Int = productList.size
}

class ProductPreviewClickListener(val clickListener: (product: Product) -> Unit) {
    fun onClick(product: Product) {
        clickListener(product)
    }
}
