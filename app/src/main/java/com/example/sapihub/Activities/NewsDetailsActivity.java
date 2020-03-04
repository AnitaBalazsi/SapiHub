package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Adapters.CommentListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;

import java.util.ArrayList;
import java.util.List;

public class NewsDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private News selectedNews;
    private TextView commentsText;
    private ImageView arrowImage;
    private LinearLayout imageContainer, viewComments;
    private RecyclerView commentView;
    private CommentListAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        initializeVariables();

        loadData();
        loadImages();
        loadComments();
    }

    private void loadComments() {
        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                String newsKey = (String) object;
                DatabaseHelper.getComments(newsKey, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        commentList.addAll((List<Comment>) object);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void loadImages() {
        if (selectedNews.getImages() != null){
            for (String imageName : selectedNews.getImages()){
                DatabaseHelper.getNewsImage(selectedNews, imageName, new FirebaseCallback() {
                    @Override
                    public void onCallback(final Object object) {
                        if (object != null){
                            ImageView imageView = addImageView();
                            loadImage((Uri) object, imageView,700,500);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showImageDialog((Uri) object);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void initializeVariables() {
        selectedNews = (News) getIntent().getSerializableExtra("selectedNews");
        imageContainer = findViewById(R.id.imageContainer);
        imageContainer.removeAllViews();
        commentsText = findViewById(R.id.commentsText);
        arrowImage = findViewById(R.id.arrowImage);
        viewComments = findViewById(R.id.viewComments);
        viewComments.setOnClickListener(this);
        commentView = findViewById(R.id.commentList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true); //show newest first

        commentView.setLayoutManager(layoutManager);

        adapter = new CommentListAdapter(this,commentList);
        commentView.setAdapter(adapter);

    }

    private void showImageDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailsActivity.this);
        final ImageView imageView = new ImageView(NewsDetailsActivity.this);

        loadImage(imageUri,imageView,3500,2000);

        builder.setView(imageView);
        AlertDialog imageDialog = builder.create();
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.show();
    }

    private void loadData() {
        TextView title = findViewById(R.id.title);
        TextView content = findViewById(R.id.content);

        title.setText(selectedNews.getTitle());
        content.setText(selectedNews.getContent());

    }

    private ImageView addImageView() {
        final ImageView imageView = new ImageView(this);
        imageContainer.addView(imageView);
        return imageView;
    }

    private void loadImage(Uri uri, ImageView imageView, int width, int height){
        Glide.with(this).load(uri.toString())
                .apply(new RequestOptions().override(width, height))
                .into(imageView);
    }

    @Override
    public void onClick(View v) {
        if (commentView.getVisibility() == View.VISIBLE){
            commentView.setVisibility(View.GONE);
            commentsText.setText(getResources().getString(R.string.viewComments));
            arrowImage.setRotation(180);
        } else {
            commentView.setVisibility(View.VISIBLE);
            commentsText.setText(getResources().getString(R.string.hideComments));
            arrowImage.setRotation(0);
        }

    }
}
