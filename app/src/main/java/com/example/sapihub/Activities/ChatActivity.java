package com.example.sapihub.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private TextView username, isTyping;
    private String userId, currentUserId, chatId;
    private ImageView profilePicture, sendMessage, imageFromCamera, imageFromGallery, onlineIcon, attachFile, showOptions;
    private EditText messageInput;
    private FirebaseRecyclerOptions<Message> messageList;
    private List<String> users = new ArrayList<>();
    private RecyclerView chatView;
    private ChatAdapter adapter;
    private ValueEventListener eventListener;
    private Uri cameraImageUri;
    private Chat chatRoom;

    private static int FROM_GALLERY = 1;
    private static int IMAGE_FROM_CAMERA = 2;
    private static int FILE_RESULT = 3;

    private static int FILE_PERMISSION = 0;
    private static int IMAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeVariables();

        users.add(currentUserId); users.add(userId);
        chatId = currentUserId.concat(userId);
        chatRoom = new Chat(users);

        DatabaseHelper.createChat(chatRoom, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    chatId = (String) object;
                    DatabaseHelper.chatReference.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            chatRoom.setMessages(chat.getMessages());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("ChatActivity",databaseError.getMessage());
                        }
                    });
                }
                getUserData();
                getMessages();
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
                        //set message as seen
                        messageData.getRef().child("seen").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("checkIfSeen",databaseError.getMessage());
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
        profilePicture.setOnClickListener(this);
        onlineIcon = findViewById(R.id.onlineIcon);
        messageInput = findViewById(R.id.messageInput);
        messageInput.addTextChangedListener(this);
        imageFromGallery = findViewById(R.id.attachImageFromGallery);
        imageFromGallery.setOnClickListener(this);
        imageFromCamera = findViewById(R.id.attachImageFromCamera);
        imageFromCamera.setOnClickListener(this);
        sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(this);
        attachFile = findViewById(R.id.attachFile);
        attachFile.setOnClickListener(this);
        showOptions = findViewById(R.id.showOptions);
        showOptions.setOnClickListener(this);

        chatView = findViewById(R.id.chatView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatView.setLayoutManager(layoutManager);
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
            case R.id.attachFile:
                attachFile();
                break;
            case R.id.showOptions:
                LinearLayout layout = findViewById(R.id.attachmentOptions);
                if (layout.getVisibility() == View.VISIBLE){
                    layout.setVisibility(View.GONE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.profilePicture:
                Intent profileIntent = new Intent(this, UserProfileActivity.class);
                profileIntent.putExtra("userId",userId);
                startActivity(profileIntent);
                break;
        }
    }

    private void askForPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMAGE_PERMISSION);
        } else {
            getImageFromCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_PERMISSION){
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getImageFromCamera();
            }
        }

        if (requestCode == FILE_PERMISSION){
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                attachFile();
            }
        }
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri);
        startActivityForResult(intent, IMAGE_FROM_CAMERA);
    }

    private void uploadFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, FROM_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_FROM_CAMERA){
                uploadData(cameraImageUri, "image");
            }
            if (requestCode == FROM_GALLERY){
                uploadData(data.getData(), "image");
            }
            if (requestCode == FILE_RESULT){
                uploadData(data.getData(), "file");
            }
        }
    }

    private String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String name = cursor.getString(nameIndex);
        cursor.close();
        return name;
    }

    private void uploadData(Uri data, final String type) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DatabaseHelper.addChatAttachment(currentUserId.concat(date), getFileName(data), data, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    String downloadUri = object.toString();
                    chatRoom.addMessage(new Message(currentUserId,userId,downloadUri,date,type,false));
                    DatabaseHelper.addMessage(chatId, chatRoom.getMessages(), new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            scrollToBottom();
                        }
                    });
                    progressDialog.dismiss();
                }
            }
        });

        sendNotification();
    }

    private void sendTextMessage() {
        final String message = messageInput.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (message.isEmpty()){
            messageInput.setError(getString(R.string.emptyField));
            messageInput.requestFocus();
        } else {
            chatRoom.addMessage(new Message(currentUserId,userId,message,date,"text",false));
            DatabaseHelper.addMessage(chatId, chatRoom.getMessages(), new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    scrollToBottom();
                }
            });

            sendNotification();
        }

        messageInput.setText(null);
    }

    private void sendNotification() {
        DatabaseHelper.getUserData(currentUserId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                NotificationData notificationData = new NotificationData(userId,getString(R.string.newMessage),user.getName().concat(" ").concat(getString(R.string.sentAMessage)), Utils.dateToString(Calendar.getInstance().getTime()));
                DatabaseHelper.sendNotification(ChatActivity.this,notificationData);
            }
        });
    }

    private void getMessages(){
        Query q = DatabaseHelper.chatReference.child(chatId).child("messages");
        messageList = new FirebaseRecyclerOptions.Builder<Message>().setQuery(q, Message.class).build();
        adapter = new ChatAdapter(messageList,this);
        chatView.setAdapter(adapter);
        adapter.startListening();

        scrollToBottom();
    }

    private void scrollToBottom() {
        chatView.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatView.scrollToPosition(adapter.getItemCount() - 1);
            }
        },200);
    }


    private void attachFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
            filePicker.setType("application/*");
            startActivityForResult(filePicker, FILE_RESULT);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},FILE_PERMISSION);
        }
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
