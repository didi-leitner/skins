package com.na.didi.skinz.view.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.na.didi.skinz.view.fragments.MyProductsFragment
import com.na.didi.skinz.view.fragments.PlaceholderFragment
import com.na.didi.skinz.view.fragments.UploadsFragment

const val MY_PICS_PAGE_INDEX = 0
const val EXPLORE_PAGE_INDEX = 1
const val MY_PRODUCTS_PAGE_INDEX = 2


class HomeViewPagerAdapter (fragment: Fragment): FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        MY_PICS_PAGE_INDEX to { UploadsFragment() },
        MY_PRODUCTS_PAGE_INDEX to { MyProductsFragment() },
        EXPLORE_PAGE_INDEX to  {PlaceholderFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }




}