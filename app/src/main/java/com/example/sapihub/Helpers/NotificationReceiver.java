package com.example.sapihub.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sapihub.Activities.HomeActivity;
import com.example.sapihub.Model.Event;
import com.example.sapihub.R;

public class NotificationReceiver extends BroadcastReceiver {
    private String CHANNEL_ID = "channel_id";
    private String CHANNEL_NAME = "channel_name";
    private String CHANNEL_DESCRIPTION = "description";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventMessage = intent.getStringExtra("eventMessage");
        String eventDate = intent.getStringExtra("eventDate");

        Intent activityIntent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,activityIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESCRIPTION);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.upcomingEvent))
                .setContentText(eventMessage + " " + eventDate)
                .setDeleteIntent(pendingIntent)
                .setGroup("group_calendar")
                .build();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1,notification);
    }
}
