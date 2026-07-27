package com.assist.doorstepdocuments

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.os.Build
import androidx.core.content.ContextCompat
//import java.util.jar.Manifest
import android.content.pm.PackageManager
import android.Manifest


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {

        val notification = NotificationCompat.Builder(this, "default")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title ?: "Notification")
            .setContentText(body ?: "")
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
//
//    private fun showNotification(title: String?, body: String?) {
//        val notification = NotificationCompat.Builder(this, "default")
//            //.setSmallIcon(R.drawable.notification)
//            .setSmallIcon(android.R.drawable.ic_popup_reminder)
//            //.setSmallIcon(R.drawable.notification)// 👈 Using system icon temporarily
//            .setContentTitle(title ?: "Notification")
//            .setContentText(body ?: "")
//            .setAutoCancel(true)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(System.currentTimeMillis().toInt(), notification)
//    }
}