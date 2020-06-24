package com.example.sapihub.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView username, department, degree, studyYear;
    private ImageView profilePicture, sendMessage;
    private String userId;
    private RecyclerView newsListView;
    private FirebaseRecyclerOptions newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initializeVariables();
        getUserData();
        loadData(DatabaseHelper.newsReference);
    }

    private void getUserData() {
        DatabaseHelper.getUserData(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                username.setText(user.getName());
                department.setText(user.getDepartment());
                if (user.getOccupation().equals(getString(R.string.student))){
                    degree.setVisibility(View.VISIBLE);
                    degree.setText(user.getDegree());
                    studyYear.setVisibility(View.VISIBLE);
                    studyYear.setText(user.getStudyYear());
                }
            }
        });

        Utils.loadProfilePicture(this,profilePicture,userId,350,350);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.getProfilePicture(userId, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        if (object != null){
                            Utils.showImageDialog(UserProfileActivity.this,(Uri) object);
                        }
                    }
                });
            }
        });
    }

    private void initializeVariables() {
        userId = getIntent().getStringExtra("userId");

        username = findViewById(R.id.name);
        department = findViewById(R.id.department);
        degree = findViewById(R.id.degree);
        studyYear = findViewById(R.id.year);
        profilePicture = findViewById(R.id.profilePicture);
        newsListView = findViewById(R.id.postList);
        sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(this);

        LinearLayoutManager newsLayout = new LinearLayoutManager(this);
        newsLayout.setReverseLayout(true);
        newsLayout.setStackFromEnd(true);
        newsListView.setLayoutManager(newsLayout);
    }

    private void loadData(Query q){
        newsList = new FirebaseRecyclerOptions.Builder<News>().setQuery(q, News.class).build();
        NewsListAdapter adapter = new NewsListAdapter(newsList,this,Utils.PROFILE_FRAGMENT,userId);
        newsListView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        //send message
        Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
        intent.putExtra("userId",userId);
        startActivity(intent);
    }
}

