package com.example.sapihub.Helpers.Database;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.sapihub.Model.Event;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notification;
import com.example.sapihub.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseHelper {
    public static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference newsReference = FirebaseDatabase.getInstance().getReference("News");
    public static DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");
    public static DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
    public static StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public static void isUserStored(final String token, final FirebaseLoginCallback callback){
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(token).exists()){
                    //if user is not stored in database
                    callback.onCallback(true);
                } else {
                    callback.onCallback(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void addUser(final User user){
        userReference.child(user.getToken()).setValue(user);
    }

    public static void addNews(final News news){
        newsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsReference.push().setValue(news);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void addEvent (final String username, final Event event){
        eventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsReference.child(username).push().setValue(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void addNotification(final String username, final Notification notification){
        notificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = notificationsReference.child(username).push().getKey();
                notification.setId(key);
                notificationsReference.child(username).child(key).setValue(notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void deleteNotification(final String username, final Notification notification){
        notificationsReference.child(username).child(notification.getId()).removeValue();
    }

    public static void uploadImage(String name, Uri imagePath){
        storageReference.child(name).putFile(imagePath);
    }
}
