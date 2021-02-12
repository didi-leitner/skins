package com.na.didi.skinz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.na.didi.skinz.data.model.Product
import com.na.didi.skinz.databinding.ListItemMyProductBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyProductsAdapter(val productClickListener: ProductClickListener) : PagingDataAdapter<Product,
        MyProductsAdapter.MyProductsViewHolder>(MyProductsDiffCallback()) {


    override fun onBindViewHolder(holder: MyProductsViewHolder, position: Int) {
        val product = getItem(position)
        if (product != null) {
            holder.bind(product, position)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyProductsViewHolder {
        return MyProductsViewHolder(
            ListItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            productClickListener
        )
    }

    class MyProductsViewHolder(
        private val binding: ListItemMyProductBinding,
        private val productClickListener: ProductClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product, position: Int) {
            binding.apply {
                product = item
                clickListener = productClickListener
                executePendingBindings()
            }
        }
    }


    private class MyProductsDiffCallback : DiffUtil.ItemCallback<Product>() {

        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }


}

class ProductClickListener(val clickListener: (product: Product) -> Unit) {
    fun onClick(product: Product) {

        clickListener(product)
    }
}


