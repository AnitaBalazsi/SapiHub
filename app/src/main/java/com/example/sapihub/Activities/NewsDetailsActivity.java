package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;

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
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;

public class NewsDetailsActivity extends AppCompatActivity {
    private News selectedNews;
    private LinearLayout imageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        selectedNews = (News) getIntent().getSerializableExtra("selectedNews");
        imageContainer = findViewById(R.id.imageContainer);
        imageContainer.removeAllViews();

        loadData();
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
}
