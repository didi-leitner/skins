package com.na.didi.skinz.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.na.didi.skinz.databinding.FragmentUploadsBinding
import com.na.didi.skinz.view.adapters.UploadItemClickListener
import com.na.didi.skinz.view.adapters.UploadsAdapter
import com.na.didi.skinz.view.viewintent.UploadsViewIntent
import com.na.didi.skinz.view.viewstate.UploadsViewEffect
import com.na.didi.skinz.view.viewstate.UploadsViewState
import com.na.didi.skinz.viewmodel.UploadsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadsFragment : BaseFragmentMVI<UploadsViewState, UploadsViewEffect, UploadsViewIntent>() {

    private lateinit var adapter: UploadsAdapter
    override val viewModel: UploadsViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUploadsBinding.inflate(inflater, container, false)

        adapter = UploadsAdapter(UploadItemClickListener {
            lifecycleScope.launch {
                viewIntentChannel.send(UploadsViewIntent.SelectContent(it))
            }
        })

        binding.uploadsList.adapter = adapter

        return binding.root
    }


    override fun renderState(state: UploadsViewState) {
        when (state) {
            is UploadsViewState.Loading -> {
            }
            is UploadsViewState.UploadsList -> {
                state.pagingData?.let {
                    adapter.submitData(lifecycle, it)
                }
            }
        }
    }

    override fun renderEffect(effect: UploadsViewEffect) {
        when (effect) {
            is UploadsViewEffect.OpenContent -> TODO() //TODO nav event
        }
    }


}