package com.example.sapihub.Helpers.Database;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.Event;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notification;
import com.example.sapihub.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    public static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference newsReference = FirebaseDatabase.getInstance().getReference("News");
    public static DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");
    public static DatabaseReference savedPostsReference = FirebaseDatabase.getInstance().getReference("Saved Posts");
    public static DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
    public static DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    public static StorageReference profilePictureRef = FirebaseStorage.getInstance().getReference("Profile pictures");
    public static StorageReference newsPictureRef = FirebaseStorage.getInstance().getReference("News pictures");

    public static void isUserStored(final String token, final FirebaseCallback callback){
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

    public static void uploadNewsImage(Context context, String newsName, String newsDate, Uri imagePath, final FirebaseCallback callback){
        newsPictureRef.child(newsName.concat(newsDate)).child(Utils.imageNameFromUri(context,imagePath)).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onCallback(true);
            }
        });
    }

    public static void getUserData(String token, final FirebaseCallback callback){
        userReference.child(token).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onCallback(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void uploadProfilePicture(String userToken, Uri imagePath, final FirebaseCallback callback){
        profilePictureRef.child(userToken).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onCallback(null);
            }
        });
    }

    public static void getProfilePicture(String userToken, final FirebaseCallback callback){
        profilePictureRef.child(userToken).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                callback.onCallback(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    public static void deleteProfilePicture(String userToken, final FirebaseCallback callback){
        profilePictureRef.child(userToken).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onCallback(null);
            }
        });
    }

    public static void getNewsImage(News news, String imageName, final FirebaseCallback callback){
        newsPictureRef.child(news.getTitle().concat(news.getDate())).child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                callback.onCallback(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    public static void savePost (String userToken, String newsKey, String title, final FirebaseCallback callback){
        savedPostsReference.child(userToken).child(newsKey).setValue(title).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onCallback(true);
            }
        });
    }

    public static void getNewsKey(final News news, final FirebaseCallback callback){
        newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                   if (newsData.getValue(News.class).equals(news)) {
                       callback.onCallback(newsData.getKey());
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void isSaved(String userToken, final String newsKey, final FirebaseCallback callback){
        savedPostsReference.child(userToken).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(newsKey)){
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

    public static void deleteSavedPost(String userToken, String newsKey, final FirebaseCallback callback){
        savedPostsReference.child(userToken).child(newsKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onCallback(true);
            }
        });
    }

    public static void deleteNews(final News news, final FirebaseCallback callback){
        newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    if (newsData.getValue(News.class).equals(news)){
                        newsReference.child(newsData.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        for (String imageUrl : news.getImages()){
            newsPictureRef.child(news.getTitle().concat(news.getDate())).child(imageUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onCallback(true);
                }
            });
        }
    }

    public static void addComment(String newsKey, Comment comment){
        commentsReference.child(newsKey).push().setValue(comment);
    }

    public static void getComments(String newsKey, final FirebaseCallback callback){
        final List<Comment> commentList = new ArrayList<>();
        commentsReference.child(newsKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot commentData : dataSnapshot.getChildren()){
                    commentList.add(commentData.getValue(Comment.class));
                }
                callback.onCallback(commentList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

