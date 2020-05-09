package com.example.sapihub.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Adapters.ChatAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.FCMAPI;
import com.example.sapihub.Helpers.RetrofitClient;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, ChatAdapter.OnSharedPostClickListener {
    private TextView username, isTyping;
    private String userId, currentUserId, chatId;
    private ImageView profilePicture, sendMessage, imageFromCamera, imageFromGallery, onlineIcon;
    private EditText messageInput;
    private List<Message> messageList = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private RecyclerView chatView;
    private ChatAdapter adapter;
    private ValueEventListener eventListener;
    private Uri cameraImageUri;
    private Chat chatRoom;
    private FCMAPI api;
    private boolean notify = false;

    private static int FROM_GALLERY = 1;
    private static int IMAGE_FROM_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeVariables();

        users.add(currentUserId); users.add(userId);
        chatRoom = new Chat(users);
        DatabaseHelper.createChat(chatRoom, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                chatId = (String) object;
                getUserData();
                getMessages();
                chatRoom.setMessages(messageList);
                checkIfSeen();
            }
        });
    }


    private void checkIfSeen() {
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageData : dataSnapshot.getChildren()){
                    Message message = messageData.getValue(Message.class);
                    if (message.getReceiver().equals(currentUserId) && message.getSender().equals(userId)){
                        //set message as seen in database and list
                        messageData.getRef().child("seen").setValue(true);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DatabaseHelper.chatReference.child(chatId).child("messages").addValueEventListener(eventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        DatabaseHelper.changeTypingTo(currentUserId,null);
        DatabaseHelper.chatReference.child(chatId).child("messages").removeEventListener(eventListener);
    }

    private void getUserData() {
        DatabaseHelper.getUserData(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                username.setText(user.getName());
                if (user.getTypingTo() != null && user.getTypingTo().equals(currentUserId)){
                    isTyping.setText(getString(R.string.isTyping));
                } else {
                    isTyping.setText(null);
                }

                if (user.getStatus().equals(getString(R.string.online))){
                    onlineIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        Utils.loadProfilePicture(this,profilePicture,userId,100,100);
    }

    private void initializeVariables() {
        userId = getIntent().getStringExtra("userId");
        currentUserId = Utils.getCurrentUserToken(this);

        username = findViewById(R.id.userName);
        isTyping = findViewById(R.id.isTyping);
        profilePicture = findViewById(R.id.profilePicture);
        onlineIcon = findViewById(R.id.onlineIcon);
        messageInput = findViewById(R.id.messageInput);
        messageInput.addTextChangedListener(this);
        imageFromGallery = findViewById(R.id.attachImageFromGallery);
        imageFromGallery.setOnClickListener(this);
        imageFromCamera = findViewById(R.id.attachImageFromCamera);
        imageFromCamera.setOnClickListener(this);
        sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(this);

        chatView = findViewById(R.id.chatView);
        chatView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(this,messageList,this);
        chatView.setAdapter(adapter);

        api = RetrofitClient.getRetrofit("https://fcm.googleapis.com/").create(FCMAPI.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendMessage:
                sendTextMessage();
                break;
            case R.id.attachImageFromGallery:
                uploadFromGallery();
                break;
            case R.id.attachImageFromCamera:
                askForPermissions();
                break;
        }
    }

    private void askForPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getImageFromCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getImageFromCamera();
            }
        }
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        cameraImageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri);
        startActivityForResult(intent,IMAGE_FROM_CAMERA);
    }

    private void uploadFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, FROM_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == FROM_GALLERY){
                cameraImageUri = data.getData();
            }
            if (requestCode == IMAGE_FROM_CAMERA){
                uploadData(cameraImageUri);
            }
        }
    }

    private void uploadData(Uri data) {
        final String type;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        if (getContentResolver().getType(data).contains("video")){
            type = "video";
        } else {
            type = "image";
        }
        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DatabaseHelper.addChatImage(currentUserId.concat(date), data, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    String downloadUri = object.toString();
                    chatRoom.addMessage(new Message(currentUserId,userId,downloadUri,date,type,false)); //todo
                    DatabaseHelper.addMessage(chatId,chatRoom.getMessages());
                    progressDialog.dismiss();
                }
            }
        });

        notify = true;
        DatabaseHelper.getUserData(currentUserId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                if (notify){
                    NotificationData notificationData = new NotificationData(userId,"New message",user.getName()+" fenykepett kuldott",R.mipmap.ic_launcher);
                    sendNotification(userId,notificationData); //todo
                }
                notify = false;
            }
        });
    }

    private void sendTextMessage() {
        notify = true;
        final String message = messageInput.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (message.isEmpty()){
            messageInput.setError(getString(R.string.emptyField));
            messageInput.requestFocus();
        } else {
            chatRoom.addMessage(new Message(currentUserId,userId,message,date,"text",false));
            DatabaseHelper.addMessage(chatId,chatRoom.getMessages()); //todo
            adapter.notifyDataSetChanged();

            DatabaseHelper.getUserData(currentUserId, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    User user = (User) object;
                    if (notify){
                        NotificationData notificationData = new NotificationData(userId,"New message",user.getName()+": "+message,R.drawable.ic_launcher_background);
                        sendNotification(userId,notificationData);
                    }
                    notify = false;
                }
            });
        }

        messageInput.setText(null);
    }

    private void sendNotification(final String userId, final NotificationData notificationData) {
        /*DatabaseHelper.tokensReference.orderByKey().equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot tokenData : dataSnapshot.getChildren()){
                    FCMToken token = tokenData.getValue(FCMToken.class);
                    NotificationSender sender = new NotificationSender(notificationData,token.getToken());
                    api.sendNotification(sender).enqueue(new Callback<NotificationResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {

                        }

                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void getMessages(){
        DatabaseHelper.chatReference.child(chatId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot messageData : dataSnapshot.getChildren()){
                    Message message = messageData.getValue(Message.class);
                    messageList.add(message);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() > 0){
            DatabaseHelper.changeTypingTo(currentUserId,userId);
        } else {
            DatabaseHelper.changeTypingTo(currentUserId,null);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onSharedPostClick(int position) {
        String postId = messageList.get(position).getContent();
        DatabaseHelper.newsReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                News news = dataSnapshot.getValue(News.class);
                Intent openDetails = new Intent(getBaseContext(), NewsDetailsActivity.class);
                openDetails.putExtra("selectedNews", news);
                startActivity(openDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
