package com.rohit.chitForChat.fragments

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.adapters.NearbyChatAdapter
import com.rohit.chitForChat.databinding.FragmentNearbyBinding


class NearbyFragment : Fragment() {
    var binding: FragmentNearbyBinding? = null
    var myLat: String = "0"
    var myLong: String = "0"
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)

    var chatNearbyList: ArrayList<Users> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNearbyBinding.inflate(inflater, container, false);
        return binding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchNearby()
        binding!!.imgSearch.setOnClickListener {
            searchNearby()
        }

    }

    private fun searchNearby() {
        binding!!.rippleEffect.startRippleAnimation()
        binding!!.rippleEffect.visibility = View.VISIBLE
        binding!!.recyclerChatList.visibility=View.INVISIBLE
        binding!!.imgSearch.visibility=View.INVISIBLE
//        MyUtils.showProgress(requireActivity())
        myLat = MyUtils.getStringValue(requireActivity(), MyConstants.USER_LATITUDE)
        myLong = MyUtils.getStringValue(requireActivity(), MyConstants.USER_LONGITUDE)

        val queryRef: Query = firebaseUsers.orderByChild("ghostMode").equalTo("off")

        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                MyUtils.stopProgress(requireActivity())
                if (snapshot.exists()) {
                    chatNearbyList.clear()
                    for (postSnapshot in snapshot.children) {
                        val user: Users? =
                            postSnapshot.getValue(Users::class.java)

                        if (!user!!.phone.equals(
                                MyUtils.getStringValue(
                                    requireActivity(),
                                    MyConstants.USER_PHONE
                                )
                            ) && !myLat.equals("") && getKmFromLatLong(
                                myLat.toFloat(),
                                myLong.toFloat(),
                                user!!.lat!!.toFloat(),
                                user!!.long!!.toFloat()
                            ) <= 1
                        ) {
                            chatNearbyList.add(user!!)
                        }

                        binding!!.recyclerChatList.adapter =
                            NearbyChatAdapter(requireActivity(), chatNearbyList!!)

                        Handler().postDelayed({
                            binding!!.rippleEffect.stopRippleAnimation()
                        binding!!.rippleEffect.visibility = View.INVISIBLE
                            binding!!.recyclerChatList.visibility=View.VISIBLE
                            binding!!.imgSearch.visibility=View.VISIBLE

                        },3000)
//                        binding!!.rippleEffect.stopRippleAnimation()
//                        binding!!.rippleEffect.visibility = View.INVISIBLE
                        // here you can access to name property like university.name
                    }


                } else {
                    MyUtils.showToast(requireContext(), "no data")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                MyUtils.stopProgress(requireActivity())
            }

        })
    }

    fun getKmFromLatLong(lat1: Float, lng1: Float, lat2: Float, lng2: Float): Float {
        val loc1 = Location("")
        loc1.latitude = lat1.toDouble()
        loc1.longitude = lng1.toDouble()
        val loc2 = Location("")
        loc2.latitude = lat2.toDouble()
        loc2.longitude = lng2.toDouble()
        val distanceInMeters = loc1.distanceTo(loc2)
        return distanceInMeters / 1000
    }

    override fun onResume() {
        super.onResume()

    }
}