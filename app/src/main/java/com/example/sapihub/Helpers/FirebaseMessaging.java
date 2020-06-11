package com.example.sapihub.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import com.example.sapihub.Activities.HomeActivity;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;

public class FirebaseMessaging extends FirebaseMessagingService {
    public static final int notification_id = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        String notificationText = remoteMessage.getNotification().getTitle();
        if (notificationText.contains(getString(R.string.newPostNotification))){
            //check if notifications are enabled
            if (Utils.checkIfEnabled(getString(R.string.postNotification),this)){
                sendNotification(remoteMessage);
            }
        }

        if (remoteMessage.getNotification().getBody().contains(getString(R.string.commentNotification))){
            if (Utils.checkIfEnabled(getString(R.string.commentNotifSetting),this)){
                sendNotification(remoteMessage);
            }
        }

        if (notificationText.contains(getString(R.string.upcomingEvent))){
            if (Utils.checkIfEnabled(getString(R.string.eventNotification),this)){
                sendNotification(remoteMessage);
            }
        }

        if (notificationText.contains(getString(R.string.messageNotification))){
            if (Utils.checkIfEnabled(getString(R.string.messageNotification),this)){
                sendNotification(remoteMessage);
            } //todo
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,notification_id,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name),getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        manager.notify(notification_id,builder.build());
    }
}
