package com.rohit.chitchat.Firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rohit.chitchat.ChatLiveActivity
import com.rohit.chitchat.HomeActivity
import com.rohit.chitchat.R
import kotlin.random.Random

class MyFireBaseMessagingService : FirebaseMessagingService() {
    var title: String? = null
    var message: String? = null
    var type: String? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        title = remoteMessage.data["Title"]
        message = remoteMessage.data["Message"]
        type = remoteMessage.data["Type"]
        notification(message)
    }

    override fun onNewToken(token: String) {
        val refreshedToken = token
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun makeNotificationChannel(id: String?, name: String?, importance: Int) {
        val channel = NotificationChannel(id, name, importance)
        channel.setShowBadge(true) // set false to disable badges, Oreo exclusive
        val notificationManager =
            (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.createNotificationChannel(channel)
    }

    fun notification(message: String?) {
        // make the channel. The method has been discussed before.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(
                title,
                title,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        // the check ensures that the channel will only be made
        // if the device is running Android 8+
        val notification = NotificationCompat.Builder(applicationContext, "CHANNEL_1")
        // the second parameter is the channel id.
        // it should be the same as passed to the makeNotificationChannel() method

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, HomeActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val id= Random(System.currentTimeMillis()).nextInt(1000)
        notification.setContentIntent(contentIntent)
        notification
            .setSmallIcon(R.drawable.logo) // can use any other icon
            .setContentTitle(title)
            .setContentText(message)
//         this shows a number in the notification dots
        val notificationManager =
            (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.notify(title!!.get(0).toInt(), notification.build())
        // it is better to not use 0 as notification id, so used 1.


        if (message.equals("You have been block")) {
            if(ChatLiveActivity.getInstance()!=null){
                ChatLiveActivity!!.getInstance()!!.onBlock(title!!);
            }
        }

    }
}