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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.ChatLiveActivity
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.R
import de.hdodenhof.circleimageview.CircleImageView

class NearbyChatAdapter(var context: Context, var chatNearbyList: ArrayList<Users>) :
    RecyclerView.Adapter<NearbyChatAdapter.viewHolder>() {


    var friendsUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
    var firebaselikedUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_LIKED_USERS)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NearbyChatAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_nearby, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: NearbyChatAdapter.viewHolder, position: Int) {
        holder.txtName.text = chatNearbyList.get(position).name
        if (!chatNearbyList.get(position).image.equals("")) {
            Glide.with(context).load(chatNearbyList.get(position).image).into(holder.imgUser)
        }

//        if(MyUtils.listFriends.contains(chatNearbyList.get(position).phone)){
//           holder.txtTitle.setText("Already Friends")
//        }

        holder.txtStatus.setText(chatNearbyList.get(position).captions)
        holder.itemView.setOnClickListener {

            friendsUsers.child(MyUtils.getStringValue(context, MyConstants.USER_PHONE))
                .child(chatNearbyList.get(position).phone.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val timeStamp =
                                snapshot.child("deleteTime").getValue(String::class.java)
                            val blockStatus =
                                snapshot.child("blockStatus").getValue(String::class.java)
                            if (blockStatus.equals("1")) {
                                MyUtils.showToast(context, "You have blocked by this user")
                            } else if (blockStatus.equals("2")) {
                                MyUtils.showToast(context, "You have blocked this user")
                            } else {
                                context.startActivity(
                                    Intent(context, ChatLiveActivity::class.java).putExtra(
                                        MyConstants.OTHER_USER_NAME,
                                        chatNearbyList.get(position).name
                                    )
                                        .putExtra(
                                            MyConstants.OTHER_USER_PHONE,
                                            chatNearbyList.get(position).phone
                                        )
                                        .putExtra(
                                            MyConstants.OTHER_USER_IMAGE,
                                            chatNearbyList.get(position).image
                                        )
                                        .putExtra(MyConstants.DELETE_TIME, timeStamp)
                                )
                            }

                        } else {
                            context.startActivity(
                                Intent(
                                    context,
                                    ChatLiveActivity::class.java
                                ).putExtra(
                                    MyConstants.OTHER_USER_NAME,
                                    chatNearbyList.get(position).name
                                )
                                    .putExtra(
                                        MyConstants.OTHER_USER_PHONE,
                                        chatNearbyList.get(position).phone
                                    )
                                    .putExtra(
                                        MyConstants.OTHER_USER_IMAGE,
                                        chatNearbyList.get(position).image
                                    )
                            )
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })


        }


        holder.imgUser.setOnClickListener {
//            firebaseUsers.child(chatNearbyList.get(position).phone.toString())
//                .child("captions").addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            var caption: String? = snapshot.getValue(String::class.java)


            firebaselikedUsers.child(chatNearbyList.get(position).phone.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            MyUtils.showProfileDialog(
                                context,
                                chatNearbyList.get(position).image.toString(),
                                chatNearbyList.get(position).captions.toString(),
                                snapshot.childrenCount.toString()
                            )

                        } else {
                            MyUtils.showProfileDialog(
                                context,
                                chatNearbyList.get(position).image.toString(),
                                chatNearbyList.get(position).captions.toString(),
                                "0"
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//
//                    }
//
//                })
//
        }
    }

    override fun getItemCount(): Int {
        return chatNearbyList.size
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        var txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }

}