package com.rohit.chatever.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rohit.chatever.Models.PurchasingModel
import com.rohit.chatever.MyConstants
import com.rohit.chatever.MyUtils
import com.rohit.chatever.R
import com.rohit.chatever.databinding.ListItemPurchaseBinding


class PurchasingAdapter(var context: Context, var listItems: ArrayList<PurchasingModel>,var click: Click) :
    RecyclerView.Adapter<PurchasingAdapter.viewHolder>() {
    var binding: ListItemPurchaseBinding? = null
    var selectedItem:Int=-1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PurchasingAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(
           R.layout.list_item_purchase,
            parent,
            false
        );
        return viewHolder(view);

    }
    init {

    }
    override fun onBindViewHolder(holder: PurchasingAdapter.viewHolder, position: Int) {
        if(listItems.get(position).PurchaseType.equals(MyUtils.getStringValue(context, MyConstants.CURRENT_SUBSCRIPTION))){
            if(selectedItem==-1)
            selectedItem=position
        }
        if(position==selectedItem){
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_purchase)
        }else{
            holder!!.itemView.setBackgroundResource(R.drawable.bg_unselected_purchase)
        }
        holder!!.txtPrice.text = listItems.get(position).itemPrice
        holder!!.txtDistance.text = listItems.get(position).itemDistance
        holder!!.txtPeriod.text = listItems.get(position).itemPeriod


        holder.itemView.setOnClickListener {
            selectedItem=position
            notifyDataSetChanged()
            click.onClickItem(position)
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtDistance = itemView.findViewById<TextView>(R.id.txtDistance)
        var txtPeriod = itemView.findViewById<TextView>(R.id.txtPeriod)
        var txtPrice = itemView.findViewById<TextView>(R.id.txtPrice)

    }

    interface Click{
        fun onClickItem(position: Int)
    }

}