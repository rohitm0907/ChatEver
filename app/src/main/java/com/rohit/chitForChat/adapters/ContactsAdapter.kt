package com.rohit.chitForChat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.ChatLiveActivity
import com.rohit.chitForChat.Models.ContactModel
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.R
import de.hdodenhof.circleimageview.CircleImageView


class ContactsAdapter(var context: Context, var listContacts: ArrayList<ContactModel>) :
    RecyclerView.Adapter<ContactsAdapter.viewHolder>() {
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
    var firebaseFriendsUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var firebaselikedList =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_LIKED_USERS)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactsAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_contacts, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.viewHolder, position: Int) {

        holder.txtName.text = listContacts.get(position).name
//        if(!listContacts.get(position).nam.equals("")) {
//            Glide.with(context).load(listContacts.get(position).image).into(holder.imgUser)
//        }
        if (MyUtils.listAllUsersNumbers.toString()
                .contains(listContacts.get(position).mobileNumber.toString())
        ) {
            holder.btnInvite.visibility = View.GONE
        } else {
            holder.btnInvite.visibility = View.VISIBLE

        }

        if (MyUtils.listAllUsersNumbers.contains(listContacts.get(position).mobileNumber.toString())
            || MyUtils.listAllUsersNumbersWithoutCode.contains(listContacts.get(position).mobileNumber.toString())) {
            var pos =
                MyUtils.listAllUsersNumbers.indexOf(listContacts.get(position).mobileNumber.toString())
            Glide.with(context).load(MyUtils.listAllUsers.get(pos).image)
                .placeholder(R.drawable.user).into(holder.imgUser)
        }

        holder.txtStatus.setText(listContacts.get(position).mobileNumber)
        holder.itemView.setOnClickListener {
            if (MyUtils.listAllUsersNumbers.contains(listContacts.get(position).mobileNumber.toString())
                || MyUtils.listAllUsersNumbersWithoutCode.contains(listContacts.get(position).mobileNumber.toString())) {
                var pos =
                    MyUtils.listAllUsersNumbers.indexOf(listContacts.get(position).mobileNumber.toString())

                firebaseFriendsUsers.child(MyUtils.getStringValue(context, MyConstants.USER_PHONE))
                    .child(listContacts.get(position).mobileNumber.toString())
                    .addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if (snapshot.exists()) {
                                val blockStatus =
                                    snapshot.child("blockStatus").getValue(String::class.java)
                                if (blockStatus.equals("1")) {
                                    MyUtils.showToast(context, "You have blocked by this user")
                                } else if (blockStatus.equals("2")) {
                                    MyUtils.showToast(context, "You have blocked this user")
                                } else {
                                    val timeStamp =
                                        snapshot.child("deleteTime").getValue(String::class.java)
                                    context.startActivity(
                                        Intent(context, ChatLiveActivity::class.java).putExtra(
                                            MyConstants.OTHER_USER_NAME,
                                            MyUtils.listAllUsers.get(pos).name
                                        )
                                            .putExtra(
                                                MyConstants.OTHER_USER_PHONE,
                                                MyUtils.listAllUsers.get(pos).phone
                                            )
                                            .putExtra(
                                                MyConstants.OTHER_USER_IMAGE,
                                                MyUtils.listAllUsers.get(pos).image
                                            )
                                            .putExtra(MyConstants.DELETE_TIME, timeStamp)
                                    )
                                }

                            } else {
                                context.startActivity(
                                    Intent(context, ChatLiveActivity::class.java).putExtra(
                                        MyConstants.OTHER_USER_NAME,
                                        MyUtils.listAllUsers.get(pos).name
                                    )
                                        .putExtra(
                                            MyConstants.OTHER_USER_PHONE,
                                            MyUtils.listAllUsers.get(pos).phone
                                        )
                                        .putExtra(
                                            MyConstants.OTHER_USER_IMAGE,
                                            MyUtils.listAllUsers.get(pos).image
                                        )
                                )
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })


//                    .putExtra(MyConstants.OTHER_USER_IMAGE,listContacts.get(position).image)

            } else {
                val smsIntent = Intent(Intent.ACTION_VIEW)
                smsIntent.type = "vnd.android-dir/mms-sms"
                smsIntent.putExtra("address", "${listContacts.get(position).mobileNumber}")
                smsIntent.putExtra(
                    "sms_body",
                    "Let's Start chat with your nearbies \nhttps://drive.google.com/file/d/1k3GKwszKvpiVBpbMVQIvNY93wIf2H0vT/view?usp=sharing"
                )
                context.startActivity(smsIntent)
            }
        }



        holder.btnInvite.apply {
            setOnClickListener {
                val smsIntent = Intent(Intent.ACTION_VIEW)
                smsIntent.type = "vnd.android-dir/mms-sms"
                smsIntent.putExtra("address", "${listContacts.get(position).mobileNumber}")
                smsIntent.putExtra(
                    "sms_body",
                    "Let's Start chat with your nearbies \nhttps://drive.google.com/file/d/1k3GKwszKvpiVBpbMVQIvNY93wIf2H0vT/view?usp=sharing"
                )
                context.startActivity(smsIntent)
            }


        }

        holder.imgUser.setOnClickListener {
            firebaseUsers.child(listContacts.get(position).mobileNumber.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var caption: String? =
                                snapshot.child("captions").getValue(String::class.java)
                            var image: String? =
                                snapshot.child("image").getValue(String::class.java)

                            firebaselikedList.child(listContacts.get(position).mobileNumber.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            MyUtils.showProfileDialog(
                                                context,
                                                image.toString(),
                                                caption.toString(),
                                                snapshot.childrenCount.toString()
                                            )
                                        } else {
                                            MyUtils.showProfileDialog(
                                                context,
                                                image.toString(),
                                                caption.toString(),
                                                "0"
                                            )
                                        }


                                    }


                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })

                        } else {
                            MyUtils.showToast(context, "no data found")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        var btnInvite = itemView.findViewById<AppCompatButton>(R.id.btnInvite)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }

}