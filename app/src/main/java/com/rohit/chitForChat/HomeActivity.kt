package com.rohit.chitForChat

import android.Manifest
import android.Manifest.permission
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.adapters.HomeTabApapter
import com.rohit.chitForChat.databinding.ActivityHomeBinding
import java.util.*
import android.content.DialogInterface

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.graphics.Typeface

import androidx.core.app.ActivityCompat.requestPermissions

import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.google.android.material.snackbar.Snackbar

import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
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


    var gotoChat=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenGenerateAndUpdate()
        handleTab()
        binding.myViewPager.adapter = HomeTabApapter(supportFragmentManager)
        binding.myTablayout.setupWithViewPager(binding.myViewPager)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@HomeActivity)


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


    private fun setUpLocationListener() {
        // for getting the current location update after every 2 seconds with high accuracy
        locationRequest = LocationRequest().setInterval(10000).setFastestInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                var lat = ""
                var longi = ""
                for (location in locationResult.locations) {
                    lat = location.latitude.toString()
                    longi = location.longitude.toString()
                }


                firebaseUsers.child(MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)).child("lat").setValue(lat)
                firebaseUsers.child(MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)).child("long").setValue(longi).addOnCompleteListener {
                    MyUtils.saveStringValue(
                        this@HomeActivity,
                        MyConstants.USER_LATITUDE,
                        lat
                    )
                    MyUtils.saveStringValue(
                        this@HomeActivity,
                        MyConstants.USER_LONGITUDE,
                       longi
                    )
                }

            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }



    override fun onResume() {
        super.onResume()

        when {
            MyUtils.isAccessFineLocationGranted(this) -> {
                when {
                    MyUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        MyUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                MyUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }


        var gotoChat=false
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

    override fun onStop() {
        super.onStop()
        if (!MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE).equals(""))
            firebaseOnlineStatus.child(
                MyUtils.getStringValue(
                    this@HomeActivity,
                    MyConstants.USER_PHONE
                )
            ).child(MyConstants.NODE_ONLINE_STATUS).setValue(MyUtils.convertIntoTime(Calendar.getInstance().timeInMillis.toString()))

    }

    override fun onPause() {
        super.onPause()

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

                    //Use firebaseMessagingToken further
                }
            }
    }





}