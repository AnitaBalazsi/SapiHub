package com.example.sapihub.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sapihub.Activities.HomeActivity;
import com.example.sapihub.R;

public class NotificationReceiver extends BroadcastReceiver {
    private String CHANNEL_ID = "event_notifications";
    private String CHANNEL_NAME = "event_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventMessage = intent.getStringExtra("eventMessage");
        String eventDate = intent.getStringExtra("eventDate");

        Intent notificationIntent = new Intent(context, HomeActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.upcomingEvents))
                .setContentText(eventMessage + " " + eventDate)
                .setContentIntent(pendingIntent) //opens HomeActivity if user clicks on the notification
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //removes notification if user clicks

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1,notification);
    }
}
