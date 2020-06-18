package com.example.sapihub.Helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String user = intent.getStringExtra("user");
        String event = intent.getStringExtra("event");
        String date = intent.getStringExtra("date");

        NotificationData notificationData = new NotificationData(user,context.getString(R.string.upcomingEvent),event,date);
        DatabaseHelper.sendNotification(context,notificationData);
    }
}
