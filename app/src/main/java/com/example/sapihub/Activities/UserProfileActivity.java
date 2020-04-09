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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView username, department, degree, studyYear, sendMessage;
    private ImageView profilePicture;
    private ProgressDialog loadingDialog;
    private String userId;
    private NewsListAdapter newsAdapter;
    private RecyclerView newsListView;
    private List<News> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initializeVariables();
        getUserData();
        getData();
    }

    private void getUserData() {
        loadingDialog.show();
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

        DatabaseHelper.getProfilePicture(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(getBaseContext()).load(imageUri.toString())
                            .apply(new RequestOptions().override(600, 600))
                            .circleCrop()
                            .into(profilePicture);
                }
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
        newsAdapter = new NewsListAdapter("MyNews",newsList,this,null);
        newsListView.setAdapter(newsAdapter);

        loadingDialog = new ProgressDialog(this, R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.loading));
    }

    private void getData(){
        DatabaseHelper.newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsList.clear();
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    News news = newsData.getValue(News.class);
                    if (news.getAuthor().equals(userId)){
                        newsList.add(news);
                        newsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadingDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        //sendmessage
        Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
        intent.putExtra("userId",userId);
        startActivity(intent);
    }
}

