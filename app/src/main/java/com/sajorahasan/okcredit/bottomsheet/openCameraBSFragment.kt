package com.sajorahasan.okcredit.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sajorahasan.okcredit.R
import kotlinx.android.synthetic.main.gallery_type_picker_bottom_sheet.*

/**
 * Created by Aipxperts
 */

class openCameraBSFragment : BottomSheetDialogFragment() {

    var bottomSheetDialog: Dialog? = null
    var mContext: Context? = null
    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.bottomSheetDialog)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        //Get the content View
        val contentView = View.inflate(context, R.layout.gallery_type_picker_bottom_sheet, null)
        dialog.setContentView(contentView)
        bottomSheetDialog = dialog
        mContext = activity
        var bottomSheet = bottomSheetDialog!!.window!!.findViewById(R.id.design_bottom_sheet) as FrameLayout
        bottomSheet!!.setBackgroundResource(R.drawable.corner_top_white)
        BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED

        //Set the coordinator layout behavior
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        prepareView()
        setOnClickListner()
        //Set callback
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
    }

    private fun prepareView() {
        if (arguments != null) {
            Log.e("***********", ""+arguments?.getString("title"))

        }

    }

    private fun setOnClickListner() {
        bottomSheetDialog!!.btn_cancel.setOnClickListener {
            dismiss()
        }

        bottomSheetDialog!!.camera.setOnClickListener {

            dismiss() // add validation


        }
        bottomSheetDialog!!.gallery.setOnClickListener {
            dismiss()
        }
    }


}
