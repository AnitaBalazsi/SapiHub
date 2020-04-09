package com.example.sapihub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Adapters.ChatAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
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

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private TextView username, isTyping;
    private String userId, currentUserId, chatId;
    private ImageView profilePicture, sendMessage, imageFromCamera, imageFromGallery;
    private EditText messageInput;
    private List<Message> messageList = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private RecyclerView chatView;
    private ChatAdapter adapter;
    private ValueEventListener eventListener;
    private Uri cameraImageUri;
    private Chat chatRoom;

    private static int IMAGE_FROM_GALLERY = 1;
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
            }
        });

        DatabaseHelper.getProfilePicture(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(getBaseContext()).load(imageUri.toString())
                            .apply(new RequestOptions().override(100, 100))
                            .circleCrop()
                            .into(profilePicture);
                }
            }
        });
    }

    private void initializeVariables() {
        userId = getIntent().getStringExtra("userId");
        currentUserId = Utils.getCurrentUserToken(this);

        username = findViewById(R.id.userName);
        isTyping = findViewById(R.id.isTyping);
        profilePicture = findViewById(R.id.profilePicture);
        messageInput = findViewById(R.id.messageInput);
        messageInput.addTextChangedListener(this);
        imageFromGallery = findViewById(R.id.attachImageFromGallery);
        imageFromGallery.setOnClickListener(this);
        imageFromCamera = findViewById(R.id.attachImageFromCamera);
        imageFromCamera.setOnClickListener(this);
        sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(this);

        chatView = findViewById(R.id.chatView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(this,messageList);
        chatView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendMessage:
                sendTextMessage();
                break;
            case R.id.attachImageFromGallery:
                getImageFromGallery();
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
        Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri);
        startActivityForResult(photoPickerIntent,IMAGE_FROM_CAMERA);
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoPickerIntent, IMAGE_FROM_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_FROM_GALLERY){
                uploadImage(data.getData());
            }
            if (requestCode == IMAGE_FROM_CAMERA){
                uploadImage(cameraImageUri);
            }
        }
    }

    private void uploadImage(Uri data) {
        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DatabaseHelper.addChatImage(currentUserId.concat(date), data, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    String downloadUri = object.toString();
                    chatRoom.addMessage(new Message(currentUserId,userId,downloadUri,date,"image",false));
                    DatabaseHelper.addMessage(chatId,chatRoom.getMessages());
                }
            }
        });
    }

    private void sendTextMessage() {
        String message = messageInput.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (message.isEmpty()){
            messageInput.setError(getString(R.string.emptyField));
            messageInput.requestFocus();
        } else {
            chatRoom.addMessage(new Message(currentUserId,userId,message,date,"text",false));
            DatabaseHelper.addMessage(chatId,chatRoom.getMessages()); //todo hardcoded string
            messageInput.setText(null);
            adapter.notifyDataSetChanged();
        }
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
}
