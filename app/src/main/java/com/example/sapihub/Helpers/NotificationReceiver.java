package com.example.sapihub.Helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.sapihub.Activities.HomeActivity;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.R;

public class NotificationReceiver extends BroadcastReceiver {
    private String CHANNEL_ID = "notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle = intent.getStringExtra("notificationTitle");
        String notificationMessage = intent.getStringExtra("notificationMessage");
        String notificationDate = intent.getStringExtra("notificationDate");

        Intent notificationIntent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(notificationTitle + notificationMessage)
                .setContentIntent(pendingIntent) //opens HomeActivity if user clicks on the notification
                .setAutoCancel(true) //removes notification after click
                .build();

        notificationManager.notify(0,notification);
        DatabaseHelper.addNotification(Utils.getCurrentUserToken(context),new com.example.sapihub.Model.Notification(notificationTitle,notificationMessage,notificationDate));
    }
}
