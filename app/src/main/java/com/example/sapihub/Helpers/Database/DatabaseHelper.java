package com.example.sapihub.Helpers.Database;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.FCMAPI;
import com.example.sapihub.Helpers.RetrofitClient;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.Event;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.FCMToken;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.Model.Notifications.NotificationResponse;
import com.example.sapihub.Model.Notifications.NotificationSender;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseHelper {
    public static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference newsReference = FirebaseDatabase.getInstance().getReference("News");
    public static DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");
    public static DatabaseReference savedPostsReference = FirebaseDatabase.getInstance().getReference("Saved Posts");
    public static DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
    public static DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    public static DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
    public static DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("ChatRooms");

    public static StorageReference profilePictureRef = FirebaseStorage.getInstance().getReference("Profile pictures");
    public static StorageReference newsAttachmentsRef = FirebaseStorage.getInstance().getReference("News attachments");
    public static StorageReference chatAttachments = FirebaseStorage.getInstance().getReference("Chat attachments");
    public static StorageReference commentAttachments = FirebaseStorage.getInstance().getReference("Comment attachments");

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
                Log.d("isUserStored",databaseError.getMessage());
            }
        });
    }

    public static void addUser(final User user){
        userReference.child(user.getUserId().getToken()).setValue(user);
    }

    public static void addNews(final News news){
        newsReference.push().setValue(news);
    }

    public static void modifyNews(String key, final News news){
        newsReference.child(key).setValue(news);
    }

    public static void addEvent (final String username, final Event event){
        eventsReference.child(username).push().setValue(event);
    }

    public static void uploadNewsAttachment(Context context, String newsName, String newsDate, Uri filePath, final FirebaseCallback callback){
        newsAttachmentsRef.child(newsName.concat(newsDate)).child(Utils.fileNameFromUri(context,filePath)).putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onCallback(true);
            }
        });
    }

    public static void uploadCommentAttachment(Context context, String commentId, Uri filePath, final FirebaseCallback callback){
        commentAttachments.child(commentId).child(Utils.fileNameFromUri(context,filePath)).putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                Log.d("getUserData",databaseError.getMessage());
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

    public static void getNewsId(final News news, final FirebaseCallback callback) {
        DatabaseHelper.newsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    if (newsData.getValue(News.class).equals(news)){
                        callback.onCallback(newsData.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getNewsId",databaseError.getMessage());
            }
        });
    }

    public static void getNewsAttachment(News news, String fileName, final FirebaseCallback callback){
        newsAttachmentsRef.child(news.getTitle().concat(news.getDate())).child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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

    public static void getCommentAttachment(Comment comment, String fileName, final FirebaseCallback callback){
        commentAttachments.child(comment.getAuthor().concat(comment.getDate())).child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
                Log.d("getNewsKey",databaseError.getMessage());
            }
        });
    }


    public static void getNews(final String title, final String author, final FirebaseCallback callback){
        newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    News news = newsData.getValue(News.class);
                    if (news.getTitle().equals(title) && news.getAuthor().equals(author)){
                        callback.onCallback(news);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getNews",databaseError.getMessage());
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
                Log.d("postIsSaved",databaseError.getMessage());
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

    public static void deleteEvent(final String userToken, final Event event){
        eventsReference.child(userToken).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event e = eventData.getValue(Event.class);
                    if (e.getMessage().equals(event.getMessage()) && e.getDate().equals(event.getDate())){
                        eventsReference.child(userToken).child(eventData.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("deleteEvent",databaseError.getMessage());
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
                Log.d("deleteNews",databaseError.getMessage());
            }
        });

        if (news.getImages() != null){
            for (String imageUrl : news.getImages()){
                newsAttachmentsRef.child(news.getTitle().concat(news.getDate())).child(imageUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onCallback(true);
                    }
                });
            }
        }
    }

    public static void addComment(final String newsKey, Comment comment){
        commentsReference.child(newsKey).push().setValue(comment);
    }


    public static void deleteComment(final String newsKey, final Comment comment){
        commentsReference.child(newsKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot commentData : dataSnapshot.getChildren()){
                    if (commentData.getValue(Comment.class).equals(comment)){
                        commentsReference.child(newsKey).child(commentData.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("deleteComment",databaseError.getMessage());
            }
        });

        if (comment.getImages() != null){
            for (String imageUrl : comment.getImages()){
                commentAttachments.child(comment.getAuthor().concat(comment.getDate())).child(imageUrl).delete();
            }
        }
    }

    public static void addLike(String newsKey, String commentKey, List<String> likes){
        commentsReference.child(newsKey).child(commentKey).child("likes").setValue(likes);
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
                Log.d("getCommentKey",databaseError.getMessage());
            }
        });
    }

    public static void addMessage(String chatId, List<Message> messages, final FirebaseCallback callback){
        chatReference.child(chatId).child("messages").setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onCallback(true);
            }
        });
    }

    public static void addChatAttachment(final String id, final String fileName, Uri path, final FirebaseCallback callback){
        chatAttachments.child(id).child(fileName).putFile(path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        chatAttachments.child(id).child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        final String user1 = chat.getUsers().get(0);
        final String user2 = chat.getUsers().get(1);
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isCreated = false;
                for (DataSnapshot chatData : dataSnapshot.getChildren()){
                    if (chatData.getKey().contains(user1) && chatData.getKey().contains(user2)){
                        isCreated = true;
                        callback.onCallback(chatData.getKey());
                    }
                }

                if (!isCreated){
                    chatReference.child(user1.concat(user2)).setValue(chat);
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("createChat",databaseError.getMessage());
            }
        });
    }

    public static void saveFCMToken(FCMToken token, String user){
        tokensReference.child(user).setValue(token);
    }

    public static void deleteFCMToken(String user){
        tokensReference.child(user).removeValue();
    }

    public static void changeStatus(String userId, String status){
        userReference.child(userId).child("status").setValue(status);
    }

    public static void getUsers(final Context context, final String query, final FirebaseCallback callback) {
        final List<User> userList = new ArrayList<>();
        DatabaseHelper.userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userData : dataSnapshot.getChildren()){
                    User user = userData.getValue(User.class);
                    if (!user.getUserId().getToken().equals(Utils.getCurrentUserToken(context)) && user.getName().contains(query)){
                        userList.add(user);
                    }
                }
                callback.onCallback(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getUsers",databaseError.getMessage());
            }
        });
    }

    public static void loadProfilePicture(final Context context, final ImageView imageView, String userId, final int width, final int height){
        if (context != null){
            DatabaseHelper.getProfilePicture(userId, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    if (object != null && !((Activity)context).isDestroyed()){
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
    }

    public static void sendNotification(final Context context, final NotificationData notificationData){
        DatabaseHelper.tokensReference.orderByKey().equalTo(notificationData.getUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot tokenData : dataSnapshot.getChildren()){
                    FCMToken token = tokenData.getValue(FCMToken.class);
                    NotificationSender sender = new NotificationSender(notificationData,token.getToken());
                    FCMAPI api = RetrofitClient.getRetrofit(FCMAPI.url).create(FCMAPI.class);
                    api.sendNotification(sender).enqueue(new Callback<NotificationResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                            if (!notificationData.getTitle().contains(context.getString(R.string.newMessage))){
                            addNotification(notificationData.getUser(),notificationData);}
                        }

                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {
                            Log.d("sendNotificationFailure",t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("sendNotification",databaseError.getMessage());
            }
        });
    }

    public static void addNotification(String user, NotificationData notificationData){
        notificationsReference.child(user).push().setValue(notificationData);
    }

    public static void removeNotification(final NotificationData notificationData){
        notificationsReference.child(notificationData.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    NotificationData notification = data.getValue(NotificationData.class);
                    if (notification.equals(notificationData)){
                        notificationsReference.child(notificationData.getUser()).child(data.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("removeNotification",databaseError.getMessage());
            }
        });
    }
}

