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

import androidx.core.app.ActivityCompat.requestPermissions

import android.os.Build
import androidx.appcompat.app.AlertDialog

import com.google.android.material.snackbar.Snackbar

import androidx.core.content.ContextCompat




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
        binding.myViewPager.adapter = HomeTabApapter(supportFragmentManager)
        binding.myTablayout.setupWithViewPager(binding.myViewPager)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@HomeActivity)

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
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
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


}