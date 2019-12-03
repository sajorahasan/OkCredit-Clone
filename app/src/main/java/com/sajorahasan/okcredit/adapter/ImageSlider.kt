package com.sajorahasan.okcredit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter
import com.sajorahasan.okcredit.R


class ImageSlider(private var context: Context) :
    SliderViewAdapter<ImageSlider.SliderAdapterVH>() {

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.item_welcome_img_slider, null)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        when (position) {
            0 -> {
                Glide.with(viewHolder.itemView)
                    .load(R.drawable.welcome_banner_1)
                    .into(viewHolder.ivWelcomeSlider)
                viewHolder.tvWelcomeSlider.text = context.getString(R.string.sliderText1)
            }
            1 -> {
                Glide.with(viewHolder.itemView)
                    .load(R.drawable.welcome_banner_2)
                    .into(viewHolder.ivWelcomeSlider)
                viewHolder.tvWelcomeSlider.text = context.getString(R.string.sliderText2)
            }
            2 -> {
                Glide.with(viewHolder.itemView)
                    .load(R.drawable.welcome_banner_3)
                    .into(viewHolder.ivWelcomeSlider)
                viewHolder.tvWelcomeSlider.text = context.getString(R.string.sliderText3)
            }
            else -> {
                Glide.with(viewHolder.itemView)
                    .load(R.drawable.welcome_banner_1)
                    .into(viewHolder.ivWelcomeSlider)
                viewHolder.tvWelcomeSlider.text = context.getString(R.string.sliderText1)
            }
        }
    }

    override fun getCount(): Int {
        //slider view count could be dynamic size
        return 3
    }

    inner class SliderAdapterVH(var itemView: View) :
        SliderViewAdapter.ViewHolder(itemView) {
        var ivWelcomeSlider: ImageView = itemView.findViewById(R.id.ivWelcomeSlider)
        var tvWelcomeSlider: TextView = itemView.findViewById(R.id.tvWelcomeSlider)

    }

}