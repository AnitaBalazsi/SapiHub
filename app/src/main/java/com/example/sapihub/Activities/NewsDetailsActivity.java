package com.example.sapihub.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Adapters.CommentListAdapter;
import com.example.sapihub.Helpers.Adapters.FileListAdapter;
import com.example.sapihub.Helpers.Adapters.ImageListAdapter;
import com.example.sapihub.Helpers.Adapters.PollListAdapter;
import com.example.sapihub.Helpers.Adapters.UserListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;

public class NewsDetailsActivity extends AppCompatActivity implements View.OnClickListener, CommentListAdapter.ListViewHolder.CommentClickListener, View.OnTouchListener {
    private News selectedNews;
    private TextView commentsText, commentsCounter;
    private ImageView sendComment, attachImage;
    private EditText commentInput;
    private LinearLayout imageContainer, commentLayout, viewComments;
    private RecyclerView commentView, pollView, fileView, commentImages;
    private CommentListAdapter adapter;
    private ArrayList<Comment> commentList = new ArrayList<>();
    private ArrayList<Uri> commentImageList = new ArrayList<>();
    private String newsKey;
    private ProgressBar loadComments;
    private RichLinkView linkView;
    private float touchPos = 0, releasePos = 0;
    private long totalComment = 0;
    private int loadCounter = 5;

    private final int GALLERY_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        initializeVariables();

        loadData();
        loadImages();
        loadComments(loadCounter);
    }

    private void loadComments(final int counter) {
        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                String newsKey = (String) object;
                getTotalCommentNumber(newsKey);
                DatabaseHelper.commentsReference.child(newsKey).limitToLast(counter).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        commentList.clear();
                        for (DataSnapshot commentData : dataSnapshot.getChildren()){
                            commentList.add(commentData.getValue(Comment.class));
                            adapter.notifyDataSetChanged();
                        }
                        loadComments.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("LoadNewsComments",databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void getTotalCommentNumber(String newsKey) {
        DatabaseHelper.commentsReference.child(newsKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalComment = dataSnapshot.getChildrenCount();
                commentsCounter.setText(String.valueOf(totalComment));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadImages() {
        if (selectedNews.getImages() != null){
            for (String imageName : selectedNews.getImages()){
                DatabaseHelper.getNewsAttachment(selectedNews, imageName, new FirebaseCallback() {
                    @Override
                    public void onCallback(final Object object) {
                        if (object != null){
                            ImageView imageView = addImageView();
                            Utils.loadImage(NewsDetailsActivity.this,(Uri) object, imageView,700,500);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Utils.showImageDialog(NewsDetailsActivity.this, (Uri) object);
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

        loadComments = findViewById(R.id.loadComments);
        linkView = findViewById(R.id.linkView);
        commentLayout = findViewById(R.id.commentsLayout);
        commentsText = findViewById(R.id.commentsText);
        commentsCounter = findViewById(R.id.commentCounter);
        viewComments = findViewById(R.id.viewComments);
        viewComments.setOnClickListener(this);
        commentView = findViewById(R.id.commentList);
        commentInput = findViewById(R.id.commentInput);
        sendComment = findViewById(R.id.sendCommentButton);
        sendComment.setOnClickListener(this);
        pollView = findViewById(R.id.pollView);
        fileView = findViewById(R.id.fileAttachments);
        attachImage = findViewById(R.id.attachImage);
        attachImage.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        commentView.setLayoutManager(layoutManager);
        adapter = new CommentListAdapter(this,commentList,this);
        commentView.setAdapter(adapter);
        commentView.setOnTouchListener(this);

        commentImages = findViewById(R.id.commentImages);
        commentImages.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
        commentImages.setAdapter(new ImageListAdapter(commentImageList, this, new ImageListAdapter.ListViewHolder.ImageClickListener() {
            @Override
            public void onDeleteImage(int position) {
                commentImageList.remove(commentImageList.get(position));
                commentImages.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onViewImage(int position) {
                Utils.showImageDialog(NewsDetailsActivity.this,commentImageList.get(position));
            }
        }));

        pollView.setLayoutManager(new LinearLayoutManager(this));
        if (selectedNews.getPolls() != null){
            pollView.setAdapter(new PollListAdapter(this, selectedNews.getPolls(), Utils.VIEW_POLL, selectedNews));
            pollView.setVisibility(View.VISIBLE);
        } else {
            pollView.setVisibility(View.GONE);
        }

        fileView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false));
        if (selectedNews.getFiles() != null){
            final List<Uri> files = new ArrayList<>();
            for (String fileUri : selectedNews.getFiles()){
                files.add(Uri.parse(fileUri));
            }
            fileView.setAdapter(new FileListAdapter(files, this, null, new FileListAdapter.ListViewHolder.FileClickListener() {
                @Override
                public void onDeleteFile(int position) {
                }

                @Override
                public void onFileClick(final int position) {
                    StorageReference ref = DatabaseHelper.newsAttachmentsRef.child(selectedNews.getTitle().concat(selectedNews.getDate())).child(selectedNews.getFiles().get(position));
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Utils.downloadFile(NewsDetailsActivity.this,selectedNews.getFiles().get(position),uri);
                        }
                    });
                }
            }));
        }

        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                newsKey = (String) object;
            }
        });
    }

    private void loadData() {
        TextView title = findViewById(R.id.title);
        TextView content = findViewById(R.id.content);

        title.setText(selectedNews.getTitle());
        content.setText(selectedNews.getContent());
        for (String word : selectedNews.getContent().split(" ")){
            if (Patterns.WEB_URL.matcher(word).matches()){
                if (word != null){
                    linkView.setLink(word, new ViewListener() {
                        @Override
                        public void onSuccess(boolean status) {
                            linkView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            }
        }

    }

    private ImageView addImageView() {
        final ImageView imageView = new ImageView(this);
        imageContainer.addView(imageView);
        return imageView;
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
                scrollToBottom();

                break;
            case R.id.sendCommentButton:
                sendComment();
                break;
            case R.id.attachImage:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, GALLERY_RESULT);
                break;
        }

    }

    private void scrollToBottom() {
        final ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_RESULT){
            commentImages.setVisibility(View.VISIBLE);
            scrollToBottom();
            if (data.getClipData() != null){ //multiple images are selected
                int totalImages = data.getClipData().getItemCount();
                for (int i = 0; i < totalImages; ++i){
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    commentImageList.add(imageUri);
                    commentImages.getAdapter().notifyDataSetChanged();
                }

            } else if (data.getData() != null) { //one image is selected
                commentImageList.add(data.getData());
                commentImages.getAdapter().notifyDataSetChanged();
            }
            commentImages.requestFocus();
        }
    }

    private void sendComment() {
        if (commentInput.getText().toString().isEmpty() && commentImageList.size() == 0){
            commentInput.setError(getString(R.string.emptyField));
            commentInput.requestFocus();
        } else {
            List<String> images = new ArrayList<>();
            final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            for (int i = 0; i < commentImageList.size(); i++){
                Uri imageUri = commentImageList.get(i);
                images.add(Utils.fileNameFromUri(this,imageUri));
                DatabaseHelper.uploadCommentAttachment(this, Utils.getCurrentUserToken(this).concat(date), imageUri, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            final Comment comment = new Comment(Utils.getCurrentUserToken(this),date,commentInput.getText().toString().trim(),images);
            DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    String key = (String) object;
                    DatabaseHelper.addComment(key,comment);

                    commentInput.setText(null);
                    commentImageList.clear();
                    commentImages.getAdapter().notifyDataSetChanged();
                }
            });

            sendNotification();
        }

    }

    private void sendNotification() {
        DatabaseHelper.getUserData(Utils.getCurrentUserToken(this), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                boolean containsAuthor = false;
                for (Comment c : commentList){
                    if (!c.getAuthor().equals(user.getUserId().getToken())){
                        //send notification to other commenters
                        NotificationData notificationData = new NotificationData(c.getAuthor(),selectedNews.getTitle(),user.getName().concat(" ").concat(getString(R.string.commentNotification)),Utils.dateToString(Calendar.getInstance().getTime()));
                        DatabaseHelper.sendNotification(notificationData);
                        if (c.getAuthor().equals(selectedNews.getAuthor())){
                            containsAuthor = true;
                        }
                    }
                }

                //send notification to author (if the author not commented)
                if (!containsAuthor){
                    NotificationData notificationData = new NotificationData(selectedNews.getAuthor(),selectedNews.getTitle(),user.getName().concat(" ").concat(getString(R.string.commentNotification)),Utils.dateToString(Calendar.getInstance().getTime()));
                    DatabaseHelper.sendNotification(notificationData);
                }
            }
        });
    }

    @Override
    public void onAuthorClick(int position) {
        Intent intent = new Intent(NewsDetailsActivity.this, UserProfileActivity.class);
        intent.putExtra("userId", commentList.get(position).getAuthor());
        startActivity(intent);
    }

    @Override
    public void onMoreOptionsClick(View itemView, final int position) {
        PopupMenu popupMenu = new PopupMenu(this, itemView, Gravity.END);
        popupMenu.inflate(R.menu.comment_options_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (String.valueOf(item.getTitle()).equals(getString(R.string.deleteComment))){
                    showConfirmDeleteDialog(commentList.get(position));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void showConfirmDeleteDialog(final Comment model) {
        new AlertDialog.Builder(this,R.style.AlertDialogTheme)
                .setTitle(getString(R.string.deleteComment))
                .setMessage(getString(R.string.confirmDeleteComment))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteComment(model);
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteComment(final Comment comment) {
        DatabaseHelper.deleteComment(newsKey, comment);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLikeCommentClick(View itemView, final int position) {
        String currentUser = Utils.getCurrentUserToken(this);
        ImageView imageView = (ImageView) itemView;

        Comment comment = commentList.get(position);
        if (comment.getLikes().contains(currentUser)){
            comment.removeLike(currentUser);
            imageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        } else {
            comment.addLike(currentUser);
            imageView.setImageResource(R.drawable.ic_favorite_24dp);

            //send notification to comment author
            if (!commentList.get(position).getAuthor().equals(currentUser)){
                DatabaseHelper.getUserData(currentUser, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        User user = (User) object;
                        NotificationData notificationData = new NotificationData(commentList.get(position).getAuthor(),selectedNews.getTitle(), user.getName().concat(" ").concat(getString(R.string.likeNotification)),Utils.dateToString(Calendar.getInstance().getTime()));
                        DatabaseHelper.sendNotification(notificationData);
                    }
                });
            }
        }

        DatabaseHelper.getCommentKey(newsKey, commentList.get(position), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                String key = (String) object;
                DatabaseHelper.addLike(newsKey,key,commentList.get(position).getLikes());
            }
        });
    }

    @Override
    public void onSeeLikesClick(final int position) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.BottomSheetDialog);
        bottomSheetDialog.setContentView(R.layout.user_list_dialog);

        final RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.userList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SearchView searchView = bottomSheetDialog.findViewById(R.id.searchView);

        final List<User> users = new ArrayList<>();
        final UserListAdapter adapter = new UserListAdapter(this, users, new UserListAdapter.UserClickListener() {
            @Override
            public void onUserClick(final int position) {
                Intent intent = new Intent(NewsDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", users.get(position).getUserId().getToken());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        DatabaseHelper.getUsers(this, "", new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                for (User user : (List<User>) object) {
                    if (commentList.get(position).getLikes().contains(user.getUserId().getToken())) {
                        users.add(user);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                DatabaseHelper.getUsers(NewsDetailsActivity.this, query, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        users.clear();
                        users.addAll((List<User>) object);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                DatabaseHelper.getUsers(NewsDetailsActivity.this, newText, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        users.clear();
                        users.addAll((List<User>) object);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });

        bottomSheetDialog.show();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
       if (event.getAction() == MotionEvent.ACTION_DOWN ){
          touchPos = event.getY();
       }
       if (event.getAction() == MotionEvent.ACTION_UP){
           releasePos = event.getY();
       }

       if (touchPos < releasePos){
           if (loadCounter < totalComment){
               loadComments.setVisibility(View.VISIBLE);
               loadCounter += 5;
               loadComments(loadCounter);
           }
       }

       return super.onTouchEvent(event);
    }
}
