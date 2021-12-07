package com.rohit.chitForChat

import android.Manifest
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

class HomeActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    var fusedLocationProviderClient:FusedLocationProviderClient?=null
    var locationRequest:LocationRequest?=null
    lateinit var binding: ActivityHomeBinding
    private  var locationCallback: LocationCallback?=null
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myViewPager.adapter = HomeTabApapter(supportFragmentManager)
        binding.myTablayout.setupWithViewPager(binding.myViewPager)
         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@HomeActivity)


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
         locationRequest = LocationRequest().setInterval(5000).setFastestInterval(5000)
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


                var users: Users = Users();
                users.name = MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_NAME)
                users.phone = MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)
                users.image = MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_IMAGE)
                users.lat = lat!!
                users.long = longi!!

                firebaseUsers.child(users.phone.toString()).setValue(users)
                MyUtils.saveStringValue(
                    this@HomeActivity,
                    MyConstants.USER_LATITUDE,
                    users.lat.toString()
                )
                MyUtils.saveStringValue(
                    this@HomeActivity,
                    MyConstants.USER_LONGITUDE,
                    users.long.toString()
                )

                // Few more things we can do here:
                // For example: Update the location of user on server
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

}