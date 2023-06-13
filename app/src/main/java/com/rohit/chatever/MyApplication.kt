package com.rohit.chatever

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.database.FirebaseDatabase
import java.util.*

var firebaseOnlineStatus =
    FirebaseDatabase.getInstance(com.rohit.chatever.MyConstants.FIREBASE_BASE_URL)
        .getReference(com.rohit.chatever.MyConstants.NODE_ONLINE_STATUS)

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    var lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                //your code here
                changeOnlineState(false)
            }
            Lifecycle.Event.ON_START -> {
                //your code here
                changeOnlineState(true)
                Log.d("mylog123", "In App")

            }
            else -> {}
        }
    }

    private fun changeOnlineState(isOnline: Boolean) {
        if (!com.rohit.chatever.MyUtils.getStringValue(
                this,
                com.rohit.chatever.MyConstants.USER_PHONE
            ).equals("")
        ) {

            if (isOnline) {
                firebaseOnlineStatus.child(
                    com.rohit.chatever.MyUtils.getStringValue(
                        this,
                        com.rohit.chatever.MyConstants.USER_PHONE
                    )
                ).child(com.rohit.chatever.MyConstants.NODE_ONLINE_STATUS).setValue("Online")
            } else {
                firebaseOnlineStatus.child(
                    com.rohit.chatever.MyUtils.getStringValue(
                        this,
                        com.rohit.chatever.MyConstants.USER_PHONE
                    )
                ).child(com.rohit.chatever.MyConstants.NODE_ONLINE_STATUS)
                    .setValue(Calendar.getInstance().timeInMillis.toString())
            }


        }
    }


}
