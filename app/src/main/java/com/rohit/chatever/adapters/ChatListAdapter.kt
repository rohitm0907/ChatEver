package com.rohit.chatever.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chatever.ChatLiveActivity
import com.rohit.chatever.Firebase.FirebaseNotification.MyNotification
import com.rohit.chatever.Models.ChatFriendsModel
import com.rohit.chatever.Models.Users
import com.rohit.chatever.MyConstants
import com.rohit.chatever.MyUtils
import com.rohit.chatever.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class ChatListAdapter(var context: Context, var chatFriendList: ArrayList<ChatFriendsModel>) :
    RecyclerView.Adapter<ChatListAdapter.viewHolder>() {
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)

    var firebasefriendList =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)

    var firebaselikedList =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_LIKED_USERS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_chat, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChatListAdapter.viewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.txtName.setText(chatFriendList.get(position).name)
        if (chatFriendList.get(position).seenStatus.equals("1")) {
            holder.txtLastMessage.setText(chatFriendList.get(position).origonalMessage)
        } else {
            holder.txtLastMessage.setTypeface(null, Typeface.BOLD);
            holder.txtLastMessage.setText(chatFriendList.get(position).lastMessage)
            showAnimation(holder.itemView,R.anim.shaking);
        }
//        holder.txtTime.setText("2:00 pm")
        if (chatFriendList.get(position).blockStatus == "0") {
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.white))
            holder.txtName.setTextColor(context.resources.getColor(R.color.black))
            holder.txtLastMessage.setTextColor(context.resources.getColor(R.color.black))
        } else if (chatFriendList.get(position).blockStatus == "1") {
            holder.txtName.setTextColor(context.resources.getColor(R.color.white))
            holder.txtLastMessage.setTextColor(context.resources.getColor(R.color.white))
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.holo_red_light))
        } else if (chatFriendList.get(position).blockStatus == "2") {
            holder.txtName.setTextColor(context.resources.getColor(R.color.white))
            holder.txtLastMessage.setTextColor(context.resources.getColor(R.color.white))
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.darker_gray))
        }

        if (!chatFriendList.get(position).image.equals("")) {
            Glide.with(context).load(chatFriendList.get(position).image).placeholder(R.drawable.user).into(holder.imgUser)
        }else{
            Glide.with(context).load(chatFriendList.get(position).image).placeholder(R.drawable.user).into(holder.imgUser)
        }

        holder.itemView.setOnClickListener {
            if (chatFriendList.get(position).blockStatus == "0") {
                context.startActivity(
                    Intent(
                        context,
                        com.rohit.chatever.ChatLiveActivity::class.java
                    ).putExtra(MyConstants.OTHER_USER_NAME, chatFriendList.get(position).name)
                        .putExtra(MyConstants.OTHER_USER_PHONE, chatFriendList.get(position).userId)
                        .putExtra(MyConstants.OTHER_USER_IMAGE, chatFriendList.get(position).image)
                        .putExtra(MyConstants.LIKE_STATUS, chatFriendList.get(position).likedStatus)
                        .putExtra(MyConstants.FROM, MyConstants.CHAT_LIST_SCREEN)
                        .putExtra(MyConstants.DELETE_TIME, chatFriendList.get(position).deleteTime)
                )
            } else if (chatFriendList.get(position).blockStatus == "1") {
                MyUtils.showToast(context, "you have blocked by other user.")
            } else if (chatFriendList.get(position).blockStatus == "2") {
                MyUtils.showToast(context, "you have blocked this user.")
            }
        }


        holder.imgDots.setOnClickListener {
            showPoppupDialog(position, holder.itemView)
        }


        holder.imgUser.setOnClickListener {
            firebaseUsers.child(chatFriendList.get(position).userId.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var data: Users? = snapshot.getValue(Users::class.java)

                            firebaselikedList.child(chatFriendList.get(position).userId.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        if (snapshot.exists()) {

                                            MyUtils.showProfileDialog(
                                                context,
                                                chatFriendList.get(position).image.toString(),
                                                data!!.captions.toString(),
                                                snapshot.childrenCount.toString()
                                            )
                                        } else {
                                            MyUtils.showProfileDialog(
                                                context,
                                                chatFriendList.get(position).image.toString(),
                                                data!!.captions.toString(),
                                                "0"
                                            )
                                        }


                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }


                                })


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    fun showAnimation(view:View,animation:Int){
        val animation: Animation = AnimationUtils.loadAnimation(context, animation)
        animation.setStartOffset(0)
        view.startAnimation(animation)
    }

    private fun showPoppupDialog(position: Int, view: View) {
        val popup = PopupMenu(context, view.findViewById(R.id.imgDots))
        popup.getMenuInflater().inflate(com.rohit.chatever.R.menu.pop_menu, popup.menu)
        var menu: Menu = popup.menu
        if (chatFriendList.get(position).blockStatus.equals("1")) {
            menu.findItem(R.id.txtBlock).setVisible(false)
            menu.findItem(R.id.txtUnblock).setVisible(false)

        } else if (chatFriendList.get(position).blockStatus.equals("2")) {
            menu.findItem(R.id.txtBlock).setVisible(false)
            menu.findItem(R.id.txtDeleteChat).setVisible(false)
        } else {
            menu.findItem(R.id.txtUnblock).setVisible(false)
        }


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

            when (it.itemId) {
                R.id.txtBlock -> {
                    firebaseUsers.child(chatFriendList.get(position).userId.toString())
                        .child("token")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    var token = snapshot.getValue(String::class.java)!!
                                    com.rohit.chatever.Firebase.FirebaseNotification.MyNotification.sendNotification(
                                        MyUtils.getStringValue(context, MyConstants.USER_NAME)
                                            .toString(),
                                        "You have been block",
                                        token,
                                        MyConstants.NOTI_REQUEST_TYPE,
                                        ""
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    firebasefriendList.child(chatFriendList.get(position).userId.toString())
                        .child(MyUtils.getStringValue(context, MyConstants.USER_PHONE))
                        .child("blockStatus").setValue("1")
                    firebasefriendList.child(
                        MyUtils.getStringValue(
                            context,
                            MyConstants.USER_PHONE
                        )
                    ).child(chatFriendList.get(position).userId.toString()).child("blockStatus")
                        .setValue("2")
                        .addOnSuccessListener(OnSuccessListener {
                            MyUtils.showToast(context, "block User")
                        })
                }

                R.id.txtUnblock -> {
                    firebasefriendList.child(chatFriendList.get(position).userId.toString())
                        .child(MyUtils.getStringValue(context, MyConstants.USER_PHONE))
                        .child("blockStatus").setValue("0")
                    firebasefriendList.child(
                        MyUtils.getStringValue(
                            context,
                            MyConstants.USER_PHONE
                        )
                    ).child(chatFriendList.get(position).userId.toString()).child("blockStatus")
                        .setValue("0")
                        .addOnSuccessListener(OnSuccessListener {
                            MyUtils.showToast(context, "Unblock User")
                        })



                    firebaseUsers.child(chatFriendList.get(position).userId.toString())
                        .child("token")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    var token = snapshot.getValue(String::class.java)!!
                                    com.rohit.chatever.Firebase.FirebaseNotification.MyNotification.sendNotification(
                                        MyUtils.getStringValue(context, MyConstants.USER_NAME)
                                            .toString(),
                                        "You have been Unblock",
                                        token,
                                        MyConstants.NOTI_REQUEST_TYPE,
                                    "")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                }
                R.id.txtDeleteChat -> {
                    firebasefriendList.child(
                        MyUtils.getStringValue(
                            context,
                            MyConstants.USER_PHONE
                        )
                    ).child(chatFriendList.get(position).userId.toString()).child("deleteTime")
                        .setValue(Calendar.getInstance().timeInMillis.toString())


                    if (MyUtils.listFriends.contains(chatFriendList.get(position).userId)) {
                        var pos = MyUtils.listFriends.indexOf(chatFriendList.get(position).userId)
                        MyUtils.listFriends.removeAt(pos)
                    }
                }

            }

            false;
        })
        popup.show() //showing popup menu

    }

    override fun getItemCount(): Int {
        return chatFriendList.size;
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtLastMessage = itemView.findViewById<TextView>(R.id.txtLastMessage)
        var imgDots = itemView.findViewById<ImageView>(R.id.imgDots)
        //        var txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }
}