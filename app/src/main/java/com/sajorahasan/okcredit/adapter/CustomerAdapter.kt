package com.sajorahasan.okcredit.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.model.Customer
import com.sajorahasan.okcredit.utils.Tools
import kotlinx.android.synthetic.main.item_customer.view.*


class CustomerAdapter(
    private val contactList: MutableList<Customer>,
    private val context: Context
) :
    RecyclerView.Adapter<CustomerAdapter.MyViewHolder>() {

    private var mListener: OnItemClickListener? = null

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.tvCustomerName
        val pic: ImageView = view.pic
        val tvBalance: TextView = view.tvBalance
        val tvBalanceStatus: TextView = view.tvBalanceStatus
    }

    // Define the mListener interface
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // Define the method that allows the parent activity or fragment to define the listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false) as LinearLayout
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, pos: Int) {
        val c: Customer = contactList[pos]
        holder.tvName.text = c.name

        if (c.profileImage !== null) {
            Glide.with(context)
                .load(Uri.parse(c.profileImage))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.pic)
        } else {
            Glide.with(context)
                .load(R.drawable.ic_account_125dp)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.pic)
        }
        holder.tvBalanceStatus.text = c.balanceType.toUpperCase()
        holder.tvBalance.text = Tools.getCurrency(c.balance)
        if (c.balanceType == context.getString(R.string.due)) {
            holder.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            holder.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        }

        holder.itemView.setOnClickListener { mListener?.onItemClick(it, pos) }

    }

    override fun getItemCount() = contactList.size
}