package com.rohit.chitForChat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.rohit.chitForChat.adapters.HomeTabAdapter
import com.rohit.chitForChat.databinding.ActivityHomeBinding
import java.util.*

import android.graphics.Typeface

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging


class HomeActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    lateinit var binding: ActivityHomeBinding
    private var locationCallback: LocationCallback? = null
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
    var firebaseOnlineStatus =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_ONLINE_STATUS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myViewPager.adapter = HomeTabAdapter(supportFragmentManager)
        binding.myTablayout.setupWithViewPager(binding.myViewPager)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@HomeActivity)
        tokenGenerateAndUpdate()
        handleTab()

    }

    private fun handleTab() {
        binding.myTablayout
            .addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tabLayout = ( binding.myTablayout.getChildAt(0) as ViewGroup).getChildAt(
                        tab!!.position
                    ) as LinearLayout
                    val tabTextView = tabLayout.getChildAt(1) as TextView
                    tabTextView.setTypeface(tabTextView.typeface, Typeface.BOLD)

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val tabLayout = ( binding.myTablayout.getChildAt(0) as ViewGroup).getChildAt(
                        tab!!.position
                    ) as LinearLayout
                    val tabTextView = tabLayout.getChildAt(1) as TextView
                    tabTextView.setTypeface(null, Typeface.NORMAL)
                    tabTextView.textSize=14F
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            });
    }
    //getting current location of user

    override fun onResume() {
        super.onResume()
        if (!MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE).equals(""))
            firebaseOnlineStatus.child(
                MyUtils.getStringValue(
                    this@HomeActivity,
                    MyConstants.USER_PHONE
                )
            ).child(MyConstants.NODE_ONLINE_STATUS).setValue("Online")
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onPause() {
        super.onPause()
        if (!MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE).equals(""))
            firebaseOnlineStatus.child(
                MyUtils.getStringValue(
                    this@HomeActivity,
                    MyConstants.USER_PHONE
                )
            ).child(MyConstants.NODE_ONLINE_STATUS).setValue(MyUtils.convertIntoTime(Calendar.getInstance().timeInMillis.toString()))

    }

    private fun tokenGenerateAndUpdate() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    //Could not get FirebaseMessagingToken
                    return@addOnCompleteListener
                }
                if (null != task.result) {
                    //Got FirebaseMessagingToken
                    val firebaseMessagingToken = Objects.requireNonNull(task.result)!!

                    firebaseUsers.child(MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)).child("token").setValue(firebaseMessagingToken)
                    MyUtils.saveStringValue(this, MyConstants.TOKEN,firebaseMessagingToken)
                    //Use firebaseMessagingToken further
                }
            }
    }
}