package com.sajorahasan.okcredit.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


class ContactsPagerAdapter : PagerAdapter() {
    private val views = ArrayList<View>()


    override fun getItemPosition(`object`: Any): Int {
        val index = views.indexOf(`object`)
        return if (index == -1)
            POSITION_NONE
        else
            index
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = views[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(views[position])
    }

    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }


    @JvmOverloads
    fun addView(view: View, position: Int = views.size): Int {
        views.add(position, view)
        return position
    }

    fun removeView(pager: ViewPager, view: View): Int {
        return removeView(pager, views.indexOf(view))
    }

    fun removeView(pager: ViewPager, position: Int): Int {
        pager.adapter = null
        views.removeAt(position)
        pager.adapter = this
        return position
    }

    fun getView(position: Int): View {
        return views[position]
    }
}