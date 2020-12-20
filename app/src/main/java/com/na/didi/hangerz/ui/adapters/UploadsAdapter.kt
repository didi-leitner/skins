package com.na.didi.hangerz.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.na.didi.hangerz.databinding.ListItemUploadBinding
import com.na.didi.hangerz.model.UploadsModel

class UploadsAdapter : PagingDataAdapter<UploadsModel, UploadsAdapter.UploadsViewHolder>(UploadsDiffCallback()) {

    override fun onBindViewHolder(holder: UploadsViewHolder, position: Int) {
        val upload = getItem(position)
        if (upload != null) {
            holder.bind(upload)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadsViewHolder {
        return UploadsViewHolder(
            ListItemUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    class UploadsViewHolder(private val binding: ListItemUploadBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { view ->
                binding.upload?.let { upload ->
                    val uri = Uri.parse(upload.localImageUrl)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    view.context.startActivity(intent)
                }
            }
        }

        fun bind(item: UploadsModel) {
            binding.apply {
                upload = item
                executePendingBindings()
            }
        }
    }


    private class UploadsDiffCallback : DiffUtil.ItemCallback<UploadsModel>() {

        override fun areItemsTheSame(oldItem: UploadsModel, newItem: UploadsModel): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: UploadsModel, newItem: UploadsModel): Boolean {
            return oldItem == newItem

        }

    }
}


