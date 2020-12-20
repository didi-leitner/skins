package com.na.didi.hangerz.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.na.didi.hangerz.databinding.FragmentUploadsBinding
import com.na.didi.hangerz.ui.adapters.UploadsAdapter
import com.na.didi.hangerz.viewmodel.UploadsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadsFragment : Fragment() {

    //private lateinit var binding: FragmentUploadsBinding
    private val viewModel: UploadsViewModel by viewModels()
    private var searchJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUploadsBinding.inflate(inflater, container, false)

        val adapter = UploadsAdapter()
        binding.uploadsList.adapter = adapter
        subscribeUi(adapter)


        return binding.root
    }

    private fun subscribeUi(adapter: UploadsAdapter) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.uploads.collectLatest {
                adapter.submitData(it)
            }
        }

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
}