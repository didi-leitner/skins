package com.na.didi.hangerz.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.na.didi.hangerz.R
import com.na.didi.hangerz.databinding.FragmentViewPagerBinding
import com.na.didi.hangerz.ui.adapters.EXPLORE_PAGE_INDEX
import com.na.didi.hangerz.ui.adapters.HomeViewPagerAdapter
import com.na.didi.hangerz.ui.adapters.MY_PICS_PAGE_INDEX
import com.na.didi.hangerz.ui.adapters.WARDROBE_PAGE_INDEX

class HomeViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }


        viewPager.adapter = HomeViewPagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
        }.attach()


        //(activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            MY_PICS_PAGE_INDEX -> R.drawable.ic_tab_my_pics
            WARDROBE_PAGE_INDEX -> R.drawable.ic_tab_wardrobe
            EXPLORE_PAGE_INDEX -> R.drawable.ic_tab_explore
            else -> throw IndexOutOfBoundsException()
        }
    }

    /*private fun getTabTitle(position: Int): String? {
        return when (position) {
            MY_PICS_PAGE_INDEX -> getString(R.string.my_garden_title)
            WARDROBE_PAGE_INDEX -> getString(R.string.plant_list_title)
            else -> null
        }
    }*/
}