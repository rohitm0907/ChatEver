package com.rohit.chitchat

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.rohit.chitchat.adapters.HomeTabAdapter
import com.rohit.chitchat.databinding.ActivityHomeBinding
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


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

    var firebasePurchase =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_PURCHASES)

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
        checkForAnyPurchase()
    }

    private fun checkForAnyPurchase() {
        firebasePurchase.child(MyUtils.getStringValue(this, MyConstants.USER_PHONE))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var subscriptionType =
                            snapshot.child("purchaseType").getValue(String::class.java)
                        var endTime = snapshot.child("endTime").getValue(String::class.java)
                        var startTime = snapshot.child("startTime").getValue(String::class.java)
                        var calender:Calendar=Calendar.getInstance()
                        calender.setTimeZone(TimeZone.getTimeZone("GMT"))
                        if (calender.timeInMillis>endTime!!.toLong() || calender.timeInMillis<startTime!!.toLong()) {
                            MyUtils.saveStringValue(
                                this@HomeActivity,
                                MyConstants.CURRENT_SUBSCRIPTION,
                                ""
                            )
                            MyUtils.saveStringValue(
                                this@HomeActivity,
                                MyConstants.SEARCH_DISTANCE,
                                "3"
                            )
                            firebasePurchase.child(MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)).removeValue()
                        } else {
                            MyUtils.saveStringValue(
                                this@HomeActivity,
                                MyConstants.CURRENT_SUBSCRIPTION,
                                subscriptionType.toString()
                            )
                        }

                    } else {
                        MyUtils.saveStringValue(
                            this@HomeActivity,
                            MyConstants.CURRENT_SUBSCRIPTION,
                            ""
                        )
                        MyUtils.saveStringValue(
                            this@HomeActivity,
                            MyConstants.SEARCH_DISTANCE,
                            "3"
                        )
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

    private fun handleTab() {
        binding.myTablayout
            .addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tabLayout = (binding.myTablayout.getChildAt(0) as ViewGroup).getChildAt(
                        tab!!.position
                    ) as LinearLayout
                    val tabTextView = tabLayout.getChildAt(1) as TextView
                    tabTextView.setTypeface(tabTextView.typeface, Typeface.BOLD)

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val tabLayout = (binding.myTablayout.getChildAt(0) as ViewGroup).getChildAt(
                        tab!!.position
                    ) as LinearLayout
                    val tabTextView = tabLayout.getChildAt(1) as TextView
                    tabTextView.setTypeface(null, Typeface.NORMAL)
                    tabTextView.textSize = 14F
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
            ).child(MyConstants.NODE_ONLINE_STATUS)
                .setValue(Calendar.getInstance().timeInMillis.toString())

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

                    firebaseUsers.child(
                        MyUtils.getStringValue(
                            this@HomeActivity,
                            MyConstants.USER_PHONE
                        )
                    ).child("token").setValue(firebaseMessagingToken)
                    MyUtils.saveStringValue(this, MyConstants.TOKEN, firebaseMessagingToken)
                    //Use firebaseMessagingToken further
                }
            }
    }


    var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity()
            System.exit(0)
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val m = menuInflater
        m.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.txtShare -> {
                val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                    link = Uri.parse("https://chitforchat.com/?phone=${MyUtils.getStringValue(this@HomeActivity, MyConstants.USER_PHONE)}")
                    domainUriPrefix = "https://chitforchat.page.link/"
                    // Open links with this app on Android
                    androidParameters("$packageName") {
//                        phone ="919815187258"
                    }
                    socialMetaTagParameters {  }

                }

                val dynamicLinkUri = dynamicLink.uri
                val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
                    longLink = Uri.parse(dynamicLinkUri.toString())
                }.addOnSuccessListener { (shortLink, flowChartLink) ->
                    // You'll need to import com.google.firebase.dynamiclinks.ktx.component1 and
                    // com.google.firebase.dynamiclinks.ktx.component2

                    // Short link created
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Let's chat with your nearby friends.\n\n" +
                                "Please share and install app via link.\nAfter register user via link, each will get - \n" +
                                "ONE LIKE" +
                                "\n\n$shortLink"
                    )
                    startActivity(Intent.createChooser(intent, "Share via"))
                }.addOnFailureListener {
                    // Error
                    // ...
                }


            }


            R.id.txtContactUs->{
                val intent = Intent(Intent.ACTION_SEND)
                val recipients = arrayOf("rohit.m0907@gmail.com")
                intent.putExtra(Intent.EXTRA_EMAIL, recipients)
                intent.putExtra(Intent.EXTRA_SUBJECT, "ChitChat Review")
                intent.type = "text/html"
                intent.setPackage("com.google.android.gm")
                startActivity(Intent.createChooser(intent, "Send mail"))
            //                try {
//                    val email = "rohit.m0907@gmail.com"
//                    val subject = "ChitChat Feedback"
//
//                    ShareCompat.IntentBuilder.from(this@HomeActivity)
//                        .setType("message/rfc822")
//                        .addEmailTo(email)
//                        .setSubject(subject)
//                        .setText("")
//                        .setChooserTitle("Select App")
//                        .startChooser()
//                } catch (e: Exception) {
//                }

            }
        }
        return true
    }

}