package com.android.chatever.Firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.chatever.ChatLiveActivity
import com.android.chatever.HomeActivity
import com.android.chatever.MyUtils
import com.android.chatever.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFireBaseMessagingService : FirebaseMessagingService() {
    var title: String? = null
    var message: String? = null
    var type: String? = null
    var chatId: String? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        title = remoteMessage.data["Title"]
        message = remoteMessage.data["Message"]
        type = remoteMessage.data["Type"]
        chatId = remoteMessage.data["chatId"]
//        notification(message)
        Note()
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
            .setSound( Uri.parse("android.resource://com.jigar.app/raw/rain_drop"))
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



    fun Note() {
        //Creating a notification channel
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                title,
                title,
                NotificationManager.IMPORTANCE_HIGH
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        //Creating the notification object
        val notification = NotificationCompat.Builder(this, title.toString())
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, HomeActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        notification!!.setContentIntent(contentIntent)
        //notification.setAutoCancel(true);
        notification.setContentTitle(title)
        notification.setContentText(message)
        notification.setSound( Uri.parse("android.resource://com.android.chatever/raw/rain_drop"))
        notification.setSmallIcon(R.mipmap.app_logo_round)
        Log.d("mylog","current chatid $chatId")
        //make the notification manager to issue a notification on the notification's channel
        if(chatId!=null){
            if(!chatId.equals("") && chatId!!.equals(MyUtils.currentChatId)){
//                var mediaPlayer=MediaPlayer()
//                mediaPlayer.setDataSource(R.raw.rain_drop)

            }else{
                manager.notify(title!!.get(0).toInt()+title!!.get(1).toInt(),notification.build())
            }

        }else{
            manager.notify(title!!.get(0).toInt()+title!!.get(1).toInt(),notification.build())

        }

        if (message.equals("You have been block")) {
            if(ChatLiveActivity.getInstance()!=null){
                ChatLiveActivity!!.getInstance()!!.onBlock(title!!);
            }
        }

   }

    fun showNotification(
        context: Context,
        title: String?,
        message: String?,
        intent: Intent?,
        reqCode: Int
    ) {
        val pendingIntent =
            PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_IMMUTABLE)
        val CHANNEL_ID = "channel_name123" // The id of the channel.
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_logo_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Channel Name" // The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(
            reqCode,
            notificationBuilder.build()
        ) // 0 is the request code, it should be unique id
        Log.d("showNotification", "showNotification: $reqCode")
    }
}