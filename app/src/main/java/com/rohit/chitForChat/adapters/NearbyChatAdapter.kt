package com.rohit.chitForChat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.rohit.chitForChat.ChatLiveActivity
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.R
import de.hdodenhof.circleimageview.CircleImageView

class NearbyChatAdapter(var context: Context, var chatNearbyList: ArrayList<Users>) :
    RecyclerView.Adapter<NearbyChatAdapter.viewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NearbyChatAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_nearby, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: NearbyChatAdapter.viewHolder, position: Int) {
        holder.txtName.text=chatNearbyList.get(position).name
        if(!chatNearbyList.get(position).image.equals("")) {
            Glide.with(context).load(chatNearbyList.get(position).image).into(holder.imgUser)
        }

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ChatLiveActivity::class.java).putExtra(MyConstants.OTHER_USER_NAME,chatNearbyList.get(position).name)
                    .putExtra(MyConstants.OTHER_USER_PHONE,chatNearbyList.get(position).phone)
                    .putExtra(MyConstants.OTHER_USER_IMAGE,chatNearbyList.get(position).image)
            )


        }
    }

    override fun getItemCount(): Int {
        return chatNearbyList.size
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }

}