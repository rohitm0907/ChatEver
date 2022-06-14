package com.rohit.chitForChat.Firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.annotation.RequiresApi
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.rohit.chitForChat.ChatLiveActivity
import com.rohit.chitForChat.R

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
                "CHANNEL_1",
                "Example channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        // the check ensures that the channel will only be made
        // if the device is running Android 8+
        val notification = NotificationCompat.Builder(applicationContext, "CHANNEL_1")
        // the second parameter is the channel id.
        // it should be the same as passed to the makeNotificationChannel() method

//        if(type.equalsIgnoreCase(AppConstants.NOTI_REQUEST_TYPE)) {
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                    new Intent(this, RequestsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//            notification.setContentIntent(contentIntent);
//        }
        notification
            .setSmallIcon(R.drawable.logo) // can use any other icon
            .setContentTitle(title)
            .setContentText(message)
            .setNumber(1) // this shows a number in the notification dots
        val notificationManager =
            (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.notify(1, notification.build())
        // it is better to not use 0 as notification id, so used 1.


        if (message.equals("You have been block")) {
            if(ChatLiveActivity.getInstance()!=null){
                ChatLiveActivity!!.getInstance()!!.onBlock(title!!);
            }
        }

    }
}