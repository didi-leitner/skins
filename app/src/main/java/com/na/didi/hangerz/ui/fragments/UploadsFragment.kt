package com.na.didi.hangerz.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.na.didi.hangerz.databinding.FragmentUploadsBinding
import com.na.didi.hangerz.ui.adapters.UploadsAdapter
import com.na.didi.hangerz.ui.viewcontract.UploadsViewContract
import com.na.didi.hangerz.ui.viewintent.UploadsViewIntent
import com.na.didi.hangerz.ui.viewmodel.UploadsViewModel
import com.na.didi.hangerz.ui.viewstate.UploadsViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class UploadsFragment : Fragment(), UploadsViewContract {

    private lateinit var binding: FragmentUploadsBinding
    private lateinit var adapter: UploadsAdapter
    private val intent = UploadsViewIntent()
    private val viewModel: UploadsViewModel by viewModels()
    private var searchJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUploadsBinding.inflate(inflater, container, false)

        adapter = UploadsAdapter(intent)
        binding.uploadsList.adapter = adapter



        //subscribeUi(adapter)


        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bindIntents(this)
        //if (savedInstanceState == null)
            //intent.loadFromNetwork.value = true

    }

    private fun subscribeUi(adapter: UploadsAdapter) {
        /*searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.uploads.collectLatest {
                adapter.submitData(it)
            }
        }*/

    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        /*@JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }*/
    }




    override fun initState() = MutableStateFlow(true)

    override fun loadFromNetwork() = intent.loadFromNetwork.filterNotNull()

    override fun selectContent() = intent.selectContent.filterNotNull()


    override fun render(state: UploadsViewState) {
        when(state) {
            is UploadsViewState.Loading -> {}
            is UploadsViewState.UploadsList -> {
                state.pagingData?.let { adapter.submitData(lifecycle, it) }
            }
            is UploadsViewState.OpenContent -> {
                //TODO navigation event
            }

        }
    }
}