package com.example.sapihub.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Activities.ChatActivity;
import com.example.sapihub.Helpers.Adapters.UserListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Utils {
    public static final String ADD_POST = "ADD_POST";
    public static final String ADD_POLL = "ADD_POLL";
    public static final String VIEW_POLL = "VIEW_POLL";

    public static void showSnackbar (View view, String text, int color){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }

    public static String getCurrentUserToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }

    public static String fileNameFromUri(Context context, Uri imageUri) {
        Cursor cursor = context.getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
        return imageUri.toString();
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ROOT);
        return format.format(date);
    }

    public static Date stringToDate (String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
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
        if (activity.getCurrentFocus() != null){
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void downloadFile(Context context, String fileName, Uri uri) {
        Toast.makeText(context,context.getString(R.string.downloading),Toast.LENGTH_LONG).show();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,DIRECTORY_DOWNLOADS,fileName);

        downloadManager.enqueue(request);
    }

    public static CharSequence getRelativeDate(Date date){
        return DateUtils.getRelativeTimeSpanString(date.getTime() , System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
    }
    public static void showImageDialog(final Context context, final Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        final ImageView imageView = new ImageView(context);
        builder.setView(imageView);
        final AlertDialog imageDialog = builder.create();
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String[] options = {context.getString(R.string.save), context.getString(R.string.sendForward)};

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            downloadFile(context,imageUri.toString(), imageUri);
                            imageDialog.dismiss();
                        } else {
                            shareInChat(context, imageUri.toString(), "image", new FirebaseCallback() {
                                @Override
                                public void onCallback(Object object) {
                                    imageDialog.dismiss();
                                    //opens chat
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("userId", (String) object);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

        loadImage(context,imageUri,imageView,3500,2000);

        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        imageDialog.show();
    }

    public static void loadImage(Context context,Uri uri, ImageView imageView, int width, int height){
        Glide.with(context).load(uri.toString())
                .apply(new RequestOptions().override(width, height))
                .into(imageView);
    }

    public static void shareInChat(final Context context, final String data, final String type, final FirebaseCallback callback){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.share_post_popup);
        bottomSheetDialog.setTitle(context.getString(R.string.sendForward));

        final RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.userList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        SearchView searchView = bottomSheetDialog.findViewById(R.id.searchView);

        final List<User> users = new ArrayList<>();
        final UserListAdapter adapter = new UserListAdapter(context, users, new UserListAdapter.UserClickListener() {
            @Override
            public void onUserClick(final int position) {
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                final Message message = new Message(Utils.getCurrentUserToken(context), users.get(position).getToken(), data, date, type, false);
                getChatId(context, users.get(position).getToken(), new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        final DataSnapshot chatData = (DataSnapshot) object;
                        if (chatData != null) {
                            sharePost(chatData, message);
                            bottomSheetDialog.dismiss();
                            callback.onCallback(users.get(position).getToken());
                        } else {
                            createChat(context, users.get(position).getToken(), new FirebaseCallback() {
                                @Override
                                public void onCallback(Object object) {
                                    sharePost((DataSnapshot) object, message);
                                    bottomSheetDialog.dismiss();
                                    callback.onCallback(users.get(position).getToken());
                                }
                            });
                        }

                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        getUsers(context, "", new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                users.addAll((List<User>) object);
                adapter.notifyDataSetChanged();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getUsers(context, query, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        users.clear();
                        users.addAll((List<User>) object);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getUsers(context, newText, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        users.clear();
                        users.addAll((List<User>) object);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });

        bottomSheetDialog.show();
    }

    public static void sharePost(DataSnapshot chatData, Message message) {
        Chat chat = chatData.getValue(Chat.class);
        chat.addMessage(message);
        DatabaseHelper.addMessage(chatData.getKey(),chat.getMessages());
    }

    public static void createChat(Context context, String token, final FirebaseCallback callback) {
        //if chatroom not exits create one
        ArrayList<String> users = new ArrayList<>();
        users.add(Utils.getCurrentUserToken(context));
        users.add(token);
        DatabaseHelper.createChat(new Chat(users), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                DatabaseHelper.chatReference.child((String) object).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        callback.onCallback(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public static void getChatId(final Context context, final String token, final FirebaseCallback callback) {
        final boolean[] idFound = {false};
        DatabaseHelper.chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatData : dataSnapshot.getChildren()){
                    Chat chat = chatData.getValue(Chat.class);
                    if (chat.getUsers().contains(Utils.getCurrentUserToken(context)) && chat.getUsers().contains(token)){
                        idFound[0] = true;
                        callback.onCallback(chatData);
                    }
                }
                if (!idFound[0]){
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getUsers(final Context context, final String query, final FirebaseCallback callback) {
        final List<User> userList = new ArrayList<>();
        DatabaseHelper.userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userData : dataSnapshot.getChildren()){
                    User user = userData.getValue(User.class);
                    if (!user.getToken().equals(Utils.getCurrentUserToken(context)) && user.getName().contains(query)){
                        userList.add(user);
                    }
                }
                callback.onCallback(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
