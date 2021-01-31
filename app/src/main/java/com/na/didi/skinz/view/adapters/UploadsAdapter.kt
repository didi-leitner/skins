package com.na.didi.skinz.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.na.didi.skinz.data.model.UploadsModel
import com.na.didi.skinz.databinding.ListItemUploadBinding
import com.na.didi.skinz.util.Event
import com.na.didi.skinz.view.viewintent.UploadsViewIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class UploadsAdapter(uploadsIntent: UploadsViewIntent) : PagingDataAdapter<UploadsModel,
        UploadsAdapter.UploadsViewHolder>(UploadsDiffCallback()) {

    val intent: UploadsViewIntent = uploadsIntent


    override fun onBindViewHolder(holder: UploadsViewHolder, position: Int) {
        val upload = getItem(position)
        if (upload != null) {
            holder.bind(upload, position)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadsViewHolder {
        return UploadsViewHolder(
                ListItemUploadBinding.inflate(LayoutInflater.from(parent.context), parent, false), intent
        )
    }

    class UploadsViewHolder(private val binding: ListItemUploadBinding, private val intent: UploadsViewIntent)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UploadsModel, position: Int) {
            binding.apply {
                upload = item
                clickListener = View.OnClickListener { view ->
                    intent.onListItemClicked.value = Event(UploadsViewIntent.SelectContent(item, position))
                }
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


