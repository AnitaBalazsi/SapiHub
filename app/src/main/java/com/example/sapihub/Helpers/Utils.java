package com.example.sapihub.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.R;
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

    public static void loadProfilePicture(final Context context, final ImageView imageView, String userId, final int width, final int height){
        DatabaseHelper.getProfilePicture(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(context).load(imageUri.toString())
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .apply(new RequestOptions().override(width, height))
                            .placeholder(context.getDrawable(R.drawable.ic_account_circle))
                            .into(imageView);
                } else {
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle));
                    imageView.getLayoutParams().height = height;
                    imageView.getLayoutParams().width = width;
                }
            }
        });
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
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
        return format.format(date);
    }

    public static Date stringToDate (String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
        return format.parse(date);
    }

    public static long differenceBetweenDates(String date1, String date2){
        long diff = 0;
        try {
            diff = stringToDate(date1).getTime() - stringToDate(date2).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Math.abs(diff);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
