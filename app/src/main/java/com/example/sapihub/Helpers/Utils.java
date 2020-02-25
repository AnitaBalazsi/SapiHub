package com.example.sapihub.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static void showSnackbar (View view, String text, int color){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }


    public static String getCurrentUserToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }

    public static String imageNameFromUri(Context context, Uri imageUri) {
        Cursor cursor = context.getContentResolver().query(imageUri, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }


    public static Date getZeroTimeDate(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static String dateToString(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd. HH:mm");
        return format.format(date);
    }

    public static Date stringToDate (String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd. HH:mm");
        return format.parse(date);
    }


}
