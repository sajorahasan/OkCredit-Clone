package com.sajorahasan.okcredit.base

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.sajorahasan.okcredit.R

open class BaseFragment : Fragment() {

    fun StatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity!!.window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        }
    }

    fun hideSoftKeyboard() {
        if (activity!!.currentFocus != null) {
            val inputMethodManager =
                activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken, 0)
        }
    }
    fun changeFragment_back(targetFragment: Fragment) {
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.anim_right,
            R.anim.anim_left,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        transaction.replace(R.id.frameLayout, targetFragment, "fragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }
    fun changeFragment_back(targetFragment: Fragment, from: String) {
        val bundle = Bundle()
        bundle.putString("from", from)
        targetFragment.arguments = bundle
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.anim_right,
            R.anim.anim_left,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        transaction.replace(R.id.frameLayout, targetFragment, "fragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }

}