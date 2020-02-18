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
    private String CHANNEL_ID = "event_notifications";
    private String CHANNEL_NAME = "event_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("proba","hali");
        String notificationTitle = intent.getStringExtra("notificationTitle");
        String notificationMessage = intent.getStringExtra("notificationMessage");
        String notificationDate = intent.getStringExtra("notificationDate");

        Intent notificationIntent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_ONE_SHOT);

//        /NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  //      notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setContentIntent(pendingIntent) //opens HomeActivity if user clicks on the notification
                .setAutoCancel(true) //removes notification after click
                .build();

 //       NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);

        //send notification and add to database
   //     managerCompat.notify(1,notification);
        notificationManager.notify(0,notification);
        DatabaseHelper.addNotification(Utils.getCurrentUserName(context),new com.example.sapihub.Model.Notification(notificationTitle,notificationMessage,notificationDate));
    }
}
