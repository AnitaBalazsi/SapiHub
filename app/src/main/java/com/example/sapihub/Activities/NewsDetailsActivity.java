package com.example.sapihub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Adapters.CommentListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsDetailsActivity extends AppCompatActivity implements View.OnClickListener, CommentListAdapter.CommentClickListener {
    private News selectedNews;
    private TextView commentsText, commentsCounter;
    private ImageView sendComment;
    private EditText commentInput;
    private LinearLayout imageContainer, commentLayout, viewComments;
    private RecyclerView commentView;
    private CommentListAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private String newsKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        initializeVariables();

        loadData();
        loadImages();
        loadComments(new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                int counter = 0;
                for (Comment comment : commentList){
                    counter++;
                    if (comment.getReplies() != null){
                        counter += comment.getReplies().size();
                    }
                }
                commentsCounter.setText(String.valueOf(counter));
            }
        });
    }

    private void loadComments(final FirebaseCallback callback) {
        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                String newsKey = (String) object;
                DatabaseHelper.commentsReference.child(newsKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        commentList.clear();
                        for (DataSnapshot commentData : dataSnapshot.getChildren()){
                            commentList.add(commentData.getValue(Comment.class));
                            adapter.notifyDataSetChanged();
                        }
                        callback.onCallback(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

        commentLayout = findViewById(R.id.commentsLayout);
        commentsText = findViewById(R.id.commentsText);
        commentsCounter = findViewById(R.id.commentCounter);
        viewComments = findViewById(R.id.viewComments);
        viewComments.setOnClickListener(this);
        commentView = findViewById(R.id.commentList);
        commentInput = findViewById(R.id.commentInput);
        sendComment = findViewById(R.id.sendCommentButton);
        sendComment.setOnClickListener(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true); //show newest first
        commentView.setLayoutManager(layoutManager);
        adapter = new CommentListAdapter(this,commentList,this);
        commentView.setAdapter(adapter);

        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                newsKey = (String) object;
            }
        });
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
        switch (v.getId()){
            case R.id.viewComments:
                if (commentLayout.getVisibility() == View.VISIBLE){
                    commentLayout.setVisibility(View.GONE);
                    commentsText.setText(getResources().getString(R.string.viewComments));
                } else {
                    commentLayout.setVisibility(View.VISIBLE);
                    commentsText.setText(getResources().getString(R.string.hideComments));
                }
                break;
            case R.id.sendCommentButton:
                sendComment();
                break;
        }

    }

    private void sendComment() {
        if (commentInput.getText().toString().isEmpty()){
            commentInput.setError(getString(R.string.emptyField));
            commentInput.requestFocus();
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            final Comment comment = new Comment(Utils.getCurrentUserToken(this),date,commentInput.getText().toString().trim());
            DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {

                    DatabaseHelper.addComment((String) object,comment);
                    commentInput.setText(null);
                }
            });
        }
    }

    @Override
    public void onMoreOptionsClick(View itemView, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, itemView.findViewById(R.id.moreOptions));
        this.getMenuInflater().inflate(R.menu.comment_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (String.valueOf(item.getTitle()).equals(getString(R.string.deleteComment))){
                    showConfirmDeleteDialog(position);
                } else {
                    //todo
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onWriteReplyClick(View itemView, int position) {
        LinearLayout commentLayout = itemView.findViewById(R.id.commentLayout);
        EditText commentInput = itemView.findViewById(R.id.commentInput);
        if (commentLayout.getVisibility() == View.VISIBLE){
            commentLayout.setVisibility(View.GONE);
        } else {
            commentLayout.setVisibility(View.VISIBLE);
            commentInput.requestFocus();
        }
    }

    @Override
    public void onSendReplyClick(View itemView, final int position) {
        final EditText replyInput = itemView.findViewById(R.id.commentInput);
        if (replyInput.getText().toString().isEmpty()){
            replyInput.setError(getString(R.string.emptyField));
            replyInput.requestFocus();
        } else {
            sendReply(position,replyInput.getText().toString().trim());
            replyInput.setText(null);
        }
    }

    private void sendReply(final int position, final String content) {
        DatabaseHelper.getCommentKey(newsKey,commentList.get(position), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                String commentKey = (String) object;
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                final Comment reply = new Comment(Utils.getCurrentUserToken(getBaseContext()),date,content);

                commentList.get(position).addReply(reply);
                DatabaseHelper.addReply(newsKey,commentKey,commentList.get(position).getReplies());
            }
        });
    }

    private void showConfirmDeleteDialog(final int position) {
        new AlertDialog.Builder(this,R.style.AlertDialogTheme)
                .setTitle(getString(R.string.deleteComment))
                .setMessage(getString(R.string.confirmDeleteComment))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteComment(commentList.get(position));
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteComment(final Comment comment) {
        DatabaseHelper.deleteComment(newsKey, comment, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        commentList.remove(comment);
                        adapter.notifyDataSetChanged(); }
        });
    }
}
