package com.na.didi.skinz.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.na.didi.skinz.databinding.FragmentMyProductsBinding
import com.na.didi.skinz.view.adapters.MyProductsAdapter
import com.na.didi.skinz.view.viewcontract.MyProductsViewContract
import com.na.didi.skinz.view.viewintent.MyProductsViewIntent
import com.na.didi.skinz.view.viewstate.MyProductsViewState
import com.na.didi.skinz.viewmodel.MyProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class MyProductsFragment : Fragment(), MyProductsViewContract {

    private lateinit var adapter: MyProductsAdapter
    private val intent = MyProductsViewIntent()
    private val viewModel: MyProductsViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMyProductsBinding.inflate(inflater, container, false)

        adapter = MyProductsAdapter(intent)
        binding.myProductsList.adapter = adapter

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


    override fun render(state: MyProductsViewState) {
        when(state) {
            is MyProductsViewState.Loading -> {}
            is MyProductsViewState.ProductList -> {

                state.pagingData?.let {
                    Log.v("TAGGG","size1: " + adapter.itemCount)

                    adapter.submitData(lifecycle, it)

                    Log.v("TAGGG","size2: " + adapter.itemCount)

                }
            }
            is MyProductsViewState.OpenContent -> {
                //TODO navigation event
            }

        }
    }
}

