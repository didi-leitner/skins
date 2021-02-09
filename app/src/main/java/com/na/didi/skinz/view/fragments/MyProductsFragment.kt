package com.na.didi.skinz.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.na.didi.skinz.databinding.FragmentMyProductsBinding
import com.na.didi.skinz.view.adapters.MyProductsAdapter
import com.na.didi.skinz.view.adapters.ProductClickListener
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewEffect
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import com.na.didi.skinz.viewmodel.MyProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyProductsFragment : BaseFragmentMVI<MyProductsViewState, MyProductsViewEffect, MyProductsViewIntent>() {

    override val viewModel: MyProductsViewModel by viewModels()

    private lateinit var adapter: MyProductsAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMyProductsBinding.inflate(inflater, container, false)
        val clickListener = ProductClickListener{
            lifecycleScope.launch {
                viewIntentChannel.send(MyProductsViewIntent.OnListItemClicked(it))
            }
        }
        adapter = MyProductsAdapter(clickListener)
        binding.myProductsList.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lifecycleScope.launchWhenStarted {
            viewIntentChannel.send(MyProductsViewIntent.InitView)
        }

    }


    override fun renderState(state: MyProductsViewState) {
        when (state) {
            is MyProductsViewState.Loading -> {
            }
            is MyProductsViewState.ProductList -> {
                state.pagingData?.let {
                    adapter.submitData(lifecycle, it)
                }
            }
        }
    }

    override fun renderEffect(effect: MyProductsViewEffect) {
        when (effect) {
            is MyProductsViewEffect.OpenContent -> {
                //TODO navigation event
                Log.v("TAGGG", "open_content todo")
            }
        }
    }
}

