package com.example.sapihub.Helpers.Database;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.Event;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.FCMToken;
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

import java.util.List;

public class DatabaseHelper {
    public static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference newsReference = FirebaseDatabase.getInstance().getReference("News");
    public static DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");
    public static DatabaseReference savedPostsReference = FirebaseDatabase.getInstance().getReference("Saved Posts");
    public static DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
    public static DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    public static StorageReference profilePictureRef = FirebaseStorage.getInstance().getReference("Profile pictures");
    public static StorageReference newsPictureRef = FirebaseStorage.getInstance().getReference("News pictures");
    public static StorageReference chatPictureRef = FirebaseStorage.getInstance().getReference("Chat pictures");
    public static DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("ChatRooms");

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
        newsReference.push().setValue(news);
    }

    public static void modifyNews(String key, final News news){
        newsReference.child(key).setValue(news);
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

    public static void uploadNewsImage(Context context, String newsName, String newsDate, Uri imagePath, final FirebaseCallback callback){
        newsPictureRef.child(newsName.concat(newsDate)).child(Utils.imageNameFromUri(context,imagePath)).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onCallback(true);
            }
        });
    }

    public static void getUserData(String token, final FirebaseCallback callback){
        userReference.child(token).addValueEventListener(new ValueEventListener() {
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

        if (news.getImages() != null){
            for (String imageUrl : news.getImages()){
                newsPictureRef.child(news.getTitle().concat(news.getDate())).child(imageUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onCallback(true);
                    }
                });
            }
        }
    }

    public static void addComment(String newsKey, Comment comment){
        commentsReference.child(newsKey).push().setValue(comment);
    }

    public static void deleteComment(final String newsKey, final Comment comment, final FirebaseCallback callback){
        commentsReference.child(newsKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot commentData : dataSnapshot.getChildren()){
                    if (commentData.getValue(Comment.class).equals(comment)){
                        commentsReference.child(newsKey).child(commentData.getKey()).removeValue();
                        callback.onCallback(null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void addReply(String newsKey, String commentKey, List<Comment> replies){
        commentsReference.child(newsKey).child(commentKey).child("replies").setValue(replies);
    }

    public static void getCommentKey(String newsKey, final Comment comment, final FirebaseCallback callback){
        commentsReference.child(newsKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot commentData : dataSnapshot.getChildren()){
                    Comment data = commentData.getValue(Comment.class);
                    if (data.equals(comment)){
                        callback.onCallback(commentData.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void addMessage(String chatId,List<Message> messages){
        chatReference.child(chatId).child("messages").setValue(messages);
    }

    public static void addChatImage(final String id, Uri imagePath, final FirebaseCallback callback){
        chatPictureRef.child(id).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                chatPictureRef.child(id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onCallback(uri);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    public static void changeTypingTo(String userId, String typingTo){
        userReference.child(userId).child("typingTo").setValue(typingTo);
    }

    public static void createChat(final Chat chat, final FirebaseCallback callback){
        final String[] key = new String[1];
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot chatData : dataSnapshot.getChildren()) {
                    if (chat.equals(chatData.getValue(Chat.class))) {
                        key[0] = chatData.getKey();
                    }
                }

                if (key[0] == null){
                    key[0] = chatReference.push().getKey();
                    chatReference.child(key[0]).setValue(chat);
                }
                callback.onCallback(key[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void saveFCMToken(FCMToken token, String user){
        tokensReference.child(user).setValue(token);
    }

    public static void changeStatus(String userId, String status){
        userReference.child(userId).child("status").setValue(status);
    }
}

