package com.sthagios.stopmotion.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.settings.isPushOn

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

        if (isPushOn()) {
            val builder = Notification.Builder(this)
            builder.setSmallIcon(R.drawable.ic_camera_black_36dp)
                    .setContentText(remoteMessage!!.notification.body)
                    .setContentTitle(remoteMessage.from)
                    .build()

            val intent = Intent(this, ListActivity::class.java)

            val stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addParentStack(ListActivity::class.java)
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(intent);
            val resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            val mNotificationManager = getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            // mId allows you to update the notification later on.
            mNotificationManager.notify(remoteMessage.from.hashCode(), builder.build());

            Log.d(TAG, "From: " + remoteMessage.from);
            Log.d(TAG, "Notification Message Body: " + remoteMessage.notification.body);
            for (data in remoteMessage.data.entries) {

            }
        }
    }
}