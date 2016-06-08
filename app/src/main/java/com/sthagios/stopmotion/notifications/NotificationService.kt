package com.sthagios.stopmotion.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   07.06.16
 */
class NotificationService : FirebaseMessagingService() {
    private val TAG = "NotificationService"

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        //TODO
        Log.d(TAG, "From: " + remoteMessage!!.from);
        Log.d(TAG, "Notification Message Body: " + remoteMessage.notification.body);
        for(data in remoteMessage.data.entries) {

        }
    }
}