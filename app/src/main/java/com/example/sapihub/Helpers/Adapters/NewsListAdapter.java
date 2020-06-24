package com.example.sapihub.Helpers.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Activities.AddNewsActivity;
import com.example.sapihub.Activities.ChatActivity;
import com.example.sapihub.Activities.NewsDetailsActivity;
import com.example.sapihub.Activities.UserProfileActivity;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;

public class NewsListAdapter extends FirebaseRecyclerAdapter<News,NewsListAdapter.ListViewHolder> {
    private String TAG;
    private Context context;
    private String selectedCaption = "";
    private String searchQuery = "";
    private ArrayList<String> savedPostList;
    private String userToken;

    public NewsListAdapter(@NonNull FirebaseRecyclerOptions<News> options, ArrayList<String> savedPostList, Context context, String TAG) {
        super(options);
        this.context = context;
        this.savedPostList = savedPostList;
        this.TAG = TAG;
    }

    public NewsListAdapter(@NonNull FirebaseRecyclerOptions<News> options, Context context, String TAG, String userToken) {
        super(options);
        this.context = context;
        this.TAG = TAG;
        this.userToken = userToken;
    }

    public void changeCaption(String caption){
        selectedCaption = caption;
    }

    public void changeSearchQuery(String query){
        searchQuery = query;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.news_list_item, parent, false);
        return new ListViewHolder(listItem);
    }

    public void populateViewHolder(ListViewHolder holder, News model) {
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_animation));
        holder.imageContainer.removeAllViews();

        holder.title.setText(model.getTitle());

        if (!model.getAuthor().equals(Utils.getCurrentUserToken(context))){
            holder.moreOptionsImage.setVisibility(View.INVISIBLE);
        } else {
            holder.moreOptionsImage.setVisibility(View.VISIBLE);
        }

        try {
            holder.date.setText(Utils.getRelativeDate(Utils.stringToDate(model.getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (model.getContent().isEmpty()){
            holder.content.setVisibility(View.GONE);
        } else {
            holder.content.setText(model.getContent());
        }

        String url = checkIfContainsUrl(model.getContent());
        loadUrl(holder,url);

        if (model.getImages() != null){ // images are attached
            for (String imageName : model.getImages()){
                loadImage(model,imageName, holder.imageContainer);
            }
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        loadAuthorData(holder, model.getAuthor());
        Utils.loadProfilePicture(context,holder.currentUserImage,Utils.getCurrentUserToken(context),100,100);
        checkIfSaved(holder.savePostImage, model);

        if (model.getFiles() != null){
            loadFiles(holder,model);
        } else {
            holder.fileView.setVisibility(View.GONE);
        }

        setClickListeners(holder,model);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ListViewHolder holder, final int position, @NonNull final News model) {
        switch (TAG){
            case Utils.MY_POST:
                if (model.getAuthor().equals(Utils.getCurrentUserToken(context))){
                    populateViewHolder(holder,model);
                } else {
                    hideItem(holder);
                }
                break;
            case Utils.SAVED_POST:
                DatabaseHelper.getNewsKey(model, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        String key = (String) object;
                        if (savedPostList.contains(key)){
                            populateViewHolder(holder,model);
                        } else {
                            hideItem(holder);
                        }
                    }
                });
                break;
            case Utils.NEWS_FRAGMENT:
                if (selectedCaption.isEmpty()){
                    DatabaseHelper.getUserData(Utils.getCurrentUserToken(context), new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            User user = (User) object;
                            if (model.getCaptions() != null && checkIfRelevant(model,user)){
                                checkIfContainsQuery(holder, model);
                            } else {
                                hideItem(holder);
                            }
                        }
                    });
                } else {
                    if (model.getCaptions() != null && model.getCaptions().contains(selectedCaption)){
                        checkIfContainsQuery(holder, model);
                    } else {
                        hideItem(holder);
                    }
                }
                break;
            case Utils.PROFILE_FRAGMENT:
                if (model.getAuthor().equals(userToken)){
                    populateViewHolder(holder,model);
                } else {
                    hideItem(holder);
                }
        }
    }

    private boolean checkIfRelevant(News model, User user) {
        if (model.getAuthor().equals(user.getUserId().getToken())){
            return true;
        }

        if (model.getCaptions().contains(user.getDepartment())){
            return true;
        }

        if (model.getCaptions().contains(user.getDegree().concat(user.getStudyYear()))){
            return true;
        }

        if (model.getCaptions().contains(context.getString(R.string.publicCaption))){
            return true;
        }

        return false;
    }

    private void checkIfContainsQuery(ListViewHolder holder, News model){
        if (searchQuery.isEmpty()){
            populateViewHolder(holder,model);
        } else {
            if (model.getContent().contains(searchQuery) || model.getTitle().contains(searchQuery)){
                populateViewHolder(holder,model);
            } else {
                hideItem(holder);
            }
        }
    }

    private void hideItem(ListViewHolder holder) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width= 0;
        layoutParams.height= 0;
        holder.itemView.setLayoutParams(layoutParams);
    }

    private void loadFiles(ListViewHolder holder, final News model) {
        holder.fileView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false));
        holder.fileView.setHasFixedSize(true);

        final ArrayList<Uri> files = new ArrayList<>();
        for (String file : model.getFiles()){
            files.add(Uri.parse(file));
        }
        holder.fileView.setAdapter(new FileListAdapter(files, context, null, new FileListAdapter.ListViewHolder.FileClickListener() {
            @Override
            public void onDeleteFile(int position) {
            }

            @Override
            public void onFileClick(final int position) {
                StorageReference ref = DatabaseHelper.newsAttachmentsRef.child(model.getTitle().concat(model.getDate())).child(model.getFiles().get(position));
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Utils.downloadFile(context,model.getFiles().get(position),uri);
                    }
                });
            }
        }));
    }

    private void setClickListeners(final ListViewHolder holder, final News model) {
        holder.newsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openDetails = new Intent(context, NewsDetailsActivity.class);
                openDetails.putExtra("selectedNews", model);
                context.startActivity(openDetails);
            }
        });

        holder.authorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String author = model.getAuthor();
                if (!author.equals(Utils.getCurrentUserToken(context))){
                    Intent profileIntent = new Intent(context, UserProfileActivity.class);
                    profileIntent.putExtra("userId",model.getAuthor());
                    context.startActivity(profileIntent);
                }
            }
        });

        holder.moreOptionsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.moreOptionsImage);
                popupMenu.inflate(R.menu.post_options_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (String.valueOf(item.getTitle()).equals(context.getString(R.string.deletePost))){
                            showConfirmDeleteDialog(model);
                        } else {
                            modifyPost(model);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        holder.savePostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.getNewsKey(model, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        final String newsKey = (String) object;
                        DatabaseHelper.isSaved(Utils.getCurrentUserToken(context), newsKey, new FirebaseCallback() {
                            @Override
                            public void onCallback(Object object) {
                                if ((Boolean) object){
                                    //if saved
                                    deletePostFromSaved(holder.savePostImage,newsKey);
                                } else {
                                    addPostToSaved(holder.savePostImage,model, newsKey);
                                }
                            }
                        });
                    }
                });
            }
        });

        holder.writeCommentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.commentLayout.getVisibility() == View.VISIBLE){
                    holder.commentLayout.setVisibility(View.GONE);
                } else {
                    holder.commentLayout.setVisibility(View.VISIBLE);
                }
                holder.commentInput.requestFocus();
            }
        });

        holder.sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.commentInput.getText().toString().isEmpty()){
                    holder.commentInput.setError(context.getString(R.string.emptyField));
                    holder.commentInput.requestFocus();
                } else {
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    final Comment comment = new Comment(Utils.getCurrentUserToken(context),date,holder.commentInput.getText().toString().trim(),null);
                    DatabaseHelper.getNewsKey(model, new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            DatabaseHelper.addComment((String) object,comment);
                            sendNotification(model);

                            Toast.makeText(context,context.getString(R.string.commentSent),Toast.LENGTH_LONG).show();
                            holder.commentInput.setText(null);
                        }
                    });
                }
            }
        });

        holder.sharePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.getNewsId(model, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        String newsId = (String) object;
                        Utils.shareInChat(context, newsId, "sharedPost", model.getCaptions(), new FirebaseCallback() {
                            @Override
                            public void onCallback(Object object) {
                                //opens chat
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("userId", (String) object);
                                context.startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }

    private void sendNotification(final News selectedNews) {
        DatabaseHelper.getUserData(Utils.getCurrentUserToken(context), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                final User user = (User) object;
                getCommentList(selectedNews, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        List<Comment> commentList = (List<Comment>) object;
                        boolean containsAuthor = false;
                        for (Comment c : commentList){
                            if (!c.getAuthor().equals(user.getUserId().getToken())){
                                //send notification to other commenters
                                NotificationData notificationData = new NotificationData(c.getAuthor(),selectedNews.getTitle(),user.getName().concat(" ").concat(context.getString(R.string.commentNotification)),Utils.dateToString(Calendar.getInstance().getTime()));
                                DatabaseHelper.sendNotification(context,notificationData);
                                if (c.getAuthor().equals(selectedNews.getAuthor())){
                                    containsAuthor = true;
                                }
                            }
                        }

                        //send notification to author (if the author not commented)
                        if (!containsAuthor && !user.getUserId().getToken().equals(selectedNews.getAuthor())){
                            NotificationData notificationData = new NotificationData(selectedNews.getAuthor(),selectedNews.getTitle(),user.getName().concat(" ").concat(context.getString(R.string.commentNotification)),Utils.dateToString(Calendar.getInstance().getTime()));
                            DatabaseHelper.sendNotification(context,notificationData);
                        }
                    }
                });
            }
        });
    }

    private void getCommentList(News selectedNews, final FirebaseCallback callback) {
        DatabaseHelper.getNewsKey(selectedNews, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                final ArrayList<Comment> comments = new ArrayList<>();
                DatabaseHelper.commentsReference.child((String) object).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        comments.clear();
                        for (DataSnapshot comment : dataSnapshot.getChildren()){
                            comments.add(comment.getValue(Comment.class));
                        }
                        callback.onCallback(comments);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("getCommentList",databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void deletePostFromSaved(final ImageView savePostImage, String newsKey) {
        DatabaseHelper.deleteSavedPost(Utils.getCurrentUserToken(context), newsKey, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if ((Boolean) object){
                    savePostImage.setImageDrawable(context.getDrawable(R.drawable.ic_star_border));
                }
            }
        });
    }

    private void addPostToSaved(final ImageView savePostImage, News model, String newsKey) {
        DatabaseHelper.savePost(Utils.getCurrentUserToken(context), newsKey, model.getTitle(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if ((Boolean) object){
                    Toast.makeText(context,context.getString(R.string.postSaved),Toast.LENGTH_SHORT).show();
                    savePostImage.setImageDrawable(context.getDrawable(R.drawable.ic_star));
                }
            }
        });
    }

    private void modifyPost(News news) {
        Intent intent = new Intent(context, AddNewsActivity.class);
        intent.putExtra("selectedPost",news);
        context.startActivity(intent);
    }

    private void deletePost(News news) {
        DatabaseHelper.deleteNews(news, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if ((Boolean) object){
                }
            }
        });
    }

    private void showConfirmDeleteDialog(final News model) {
        new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                .setTitle(context.getString(R.string.deletePost))
                .setMessage(context.getString(R.string.confirmDeleteNews))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deletePost(model);
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void loadUrl(final ListViewHolder holder, String url) {
        if (url != null){
            holder.richLinkView.setLink(url, new ViewListener() {
                @Override
                public void onSuccess(boolean status) {
                    holder.richLinkView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("loadUrl",e.getMessage());
                }
            });
        }
    }

    private String checkIfContainsUrl(String content){
        for (String word : content.split(" ")){
            if (Patterns.WEB_URL.matcher(word).matches()){
                return word;
            }
        }
        return null;
    }

    private void checkIfSaved(final ImageView savePostImage, News news) {
        DatabaseHelper.getNewsKey(news, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                DatabaseHelper.isSaved(Utils.getCurrentUserToken(context), (String) object, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        if ((Boolean) object){
                            //if post is saved
                            savePostImage.setImageDrawable(context.getDrawable(R.drawable.ic_star));
                        } else {
                            savePostImage.setImageDrawable(context.getDrawable(R.drawable.ic_star_border));
                        }
                    }
                });
            }
        });
    }

    private void loadAuthorData(final ListViewHolder holder, final String authorToken) {
        DatabaseHelper.getUserData(authorToken, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User author = (User) object;
                holder.author.setText(author.getName());
                Utils.loadProfilePicture(context,holder.authorImage,authorToken,130,130);
            }
        });
    }

    private void loadImage(final News news, String imageName, final LinearLayout linearLayout) {
        final ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 10, 0);
        imageView.setLayoutParams(params);
        linearLayout.addView(imageView);

        //if there is an image attached
        if (imageName != null){
            DatabaseHelper.getNewsAttachment(news, imageName, new FirebaseCallback() {
                @Override
                public void onCallback(final Object object) {
                    if (object != null && !((Activity)context).isDestroyed()){
                        final Uri uri = (Uri) object;
                        Glide.with(context).load(uri.toString())
                                .apply(new RequestOptions().override(500, 300))
                                .centerCrop()
                                .into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utils.showImageDialog(context, (Uri) object);
                            }
                        });
                    }
                }
            });
        }
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout imageContainer, newsLayout;
        private TextView title, content, date, author;
        private ImageView authorImage, moreOptionsImage, savePostImage, writeCommentImage, sendComment, currentUserImage, sharePost;
        private RichLinkView richLinkView;
        private RecyclerView pollView, fileView;
        private EditText commentInput;
        private LinearLayout commentLayout;

        private ListViewHolder(@NonNull final View itemView) {
            super(itemView);

            this.imageContainer = itemView.findViewById(R.id.imageContainer);
            this.title = itemView.findViewById(R.id.title);
            this.content = itemView.findViewById(R.id.content);
            this.date = itemView.findViewById(R.id.date);
            this.author = itemView.findViewById(R.id.authorName);
            this.commentInput = itemView.findViewById(R.id.commentInput);
            this.authorImage = itemView.findViewById(R.id.authorProfilePicture);
            this.commentLayout = itemView.findViewById(R.id.commentLayout);
            this.moreOptionsImage = itemView.findViewById(R.id.moreImage);
            this.savePostImage = itemView.findViewById(R.id.savePost);
            this.writeCommentImage = itemView.findViewById(R.id.addComment);
            this.sendComment = itemView.findViewById(R.id.sendCommentButton);
            this.currentUserImage = itemView.findViewById(R.id.currentUserImage);
            this.sharePost = itemView.findViewById(R.id.sharePost);
            this.newsLayout = itemView.findViewById(R.id.newsLayout);
            this.richLinkView = itemView.findViewById(R.id.linkView);
            this.pollView = itemView.findViewById(R.id.pollView);
            this.fileView = itemView.findViewById(R.id.fileAttachments);
    }}
}
