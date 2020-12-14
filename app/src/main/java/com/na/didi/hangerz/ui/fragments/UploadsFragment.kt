package com.na.didi.hangerz.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.na.didi.hangerz.databinding.FragmentMyPicsBinding
import com.na.didi.hangerz.ui.main.PlaceholderFragment
import com.na.didi.hangerz.viewmodel.UploadsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadsFragment : Fragment() {

    private lateinit var binding: FragmentMyPicsBinding
    private val viewModel: UploadsViewModel by viewModels()


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyPicsBinding.inflate(inflater, container, false)

        //val adapter = GardenPlantingAdapter()
        //binding.gardenList.adapter = adapter
        //subscribeUi(adapter, binding)

        /*pageViewModel.text.observe(this, Observer<String> {
            textView.text = it
        })*/
        return binding.root
    }

    /*private fun subscribeUi(adapter: GardenPlantingAdapter, binding: FragmentMyPicsBinding) {
        viewModel.plantAndGardenPlantings.observe(viewLifecycleOwner) { result ->
            binding.hasPlantings = !result.isNullOrEmpty()
            adapter.submitList(result)
        }
    }*/

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
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}