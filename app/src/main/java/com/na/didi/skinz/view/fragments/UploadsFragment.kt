package com.na.didi.skinz.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.na.didi.skinz.databinding.FragmentUploadsBinding
import com.na.didi.skinz.view.adapters.UploadsAdapter
import com.na.didi.skinz.view.viewcontract.UploadsViewContract
import com.na.didi.skinz.view.viewintent.UploadsViewIntent
import com.na.didi.skinz.view.viewstate.UploadsViewState
import com.na.didi.skinz.viewmodel.UploadsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class UploadsFragment : Fragment(), UploadsViewContract {

    private lateinit var adapter: UploadsAdapter
    private val intent = UploadsViewIntent()
    private val viewModel: UploadsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUploadsBinding.inflate(inflater, container, false)

        adapter = UploadsAdapter(intent)
        binding.uploadsList.adapter = adapter

        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bindIntents(this)
    }


    override fun initState() = MutableStateFlow(true)

    override fun onSwipeToRefresh() = intent.swipeToRefresh.filterNotNull()

    override fun onListEntryClicked() = intent.onListItemClicked.filterNotNull()


    override fun render(state: UploadsViewState) {
        when(state) {
            is UploadsViewState.Loading -> {}
            is UploadsViewState.UploadsList -> {

                state.pagingData?.let {
                    Log.v("TAGGG","size1: " + adapter.itemCount)

                    adapter.submitData(lifecycle, it)

                    Log.v("TAGGG","size2: " + adapter.itemCount)

                }
            }
            is UploadsViewState.OpenContent -> {
                //TODO navigation event
            }

        }
    }
}