package com.rohit.chitForChat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
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
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactsAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_contacts, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.viewHolder, position: Int) {

        holder.txtName.text=listContacts.get(position).name
//        if(!listContacts.get(position).nam.equals("")) {
//            Glide.with(context).load(listContacts.get(position).image).into(holder.imgUser)
//        }
        if(!MyUtils.listAllUsersNumbers.contains(listContacts.get(position).mobileNumber.toString())){
            holder.txtTitle.visibility=View.VISIBLE
        }else{
            holder.txtTitle.visibility=View.GONE
        }

        if(MyUtils.listAllUsersNumbers.contains(listContacts.get(position).mobileNumber.toString())) {
            var pos =
                MyUtils.listAllUsersNumbers.indexOf(listContacts.get(position).mobileNumber.toString())
     Glide.with(context).load(MyUtils.listAllUsers.get(pos).image).into(holder.imgUser)
        }

            holder.txtStatus.setText(listContacts.get(position).mobileNumber)
        holder.itemView.setOnClickListener {
            if(MyUtils.listAllUsersNumbers.contains(listContacts.get(position).mobileNumber.toString())){
              var pos=  MyUtils.listAllUsersNumbers.indexOf(listContacts.get(position).mobileNumber.toString())
            context.startActivity(
                Intent(context, ChatLiveActivity::class.java).putExtra(MyConstants.OTHER_USER_NAME,listContacts.get(position).name)
                    .putExtra(MyConstants.OTHER_USER_PHONE,listContacts.get(position).mobileNumber)
                    .putExtra(MyConstants.OTHER_USER_IMAGE,MyUtils.listAllUsers.get(pos).image)
//                    .putExtra(MyConstants.OTHER_USER_IMAGE,listContacts.get(position).image)
            )
            }else{
                val smsIntent = Intent(Intent.ACTION_VIEW)
                smsIntent.type = "vnd.android-dir/mms-sms"
                smsIntent.putExtra("address", "${listContacts.get(position).mobileNumber}")
                smsIntent.putExtra("sms_body", "Let's Start chat with your nearbies \nhttps://play.google.com/store/apps/details?id=com.jigar.app")
                context.startActivity(smsIntent)
            }
        }


        holder.imgUser.setOnClickListener {

//            firebaseUsers.child(chatNearbyList.get(position).phone.toString())
//                .child("captions").addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            var caption: String? = snapshot.getValue(String::class.java)
//                            MyUtils.showProfileDialog(
//                                context,
//                                listContacts.get(position).image.toString(),
//                                listContacts.get(position).captions.toString(),
//                                listContacts.get(position).totalLikes.toString()
//                            )

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
        return listContacts.size
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        var txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }

}