package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ListViewHolder> {
    private String TAG;
    private List<News> newsList;
    private Context context;
    private ListViewHolder.NewsClickListener newsClickListener;


    public NewsListAdapter(String TAG, List<News> newsList, Context context, ListViewHolder.NewsClickListener newsClickListener) {
        this.TAG = TAG;
        this.newsList = newsList;
        this.context = context;
        this.newsClickListener = newsClickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.news_list_item, parent, false);
        return new ListViewHolder(listItem, newsClickListener, TAG);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.imageContainer.removeAllViews();
        if (!newsList.get(position).getAuthor().equals(Utils.getCurrentUserToken(context))){
            holder.moreOptionsImage.setVisibility(View.INVISIBLE);
        } else {
            holder.moreOptionsImage.setVisibility(View.VISIBLE);
        }

        holder.title.setText(newsList.get(position).getTitle());
        holder.date.setText(newsList.get(position).getDate());
        String content = newsList.get(position).getContent();
        if (content.length() > 400){
            holder.content.setText(content.substring(0,400).concat("..."));
        } else {
            holder.content.setText(content);
        }

        if (newsList.get(position).getImages() != null){ // images are attached
            for (String imageName : newsList.get(position).getImages()){
                loadImage(newsList.get(position),imageName, holder.imageContainer);
            }
        }
        loadAuthorData(holder, newsList.get(position).getAuthor());
        loadCurrentUserImage(holder);
        checkIfSaved(holder.savePostImage, newsList.get(position));
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
                loadAuthorImage(holder,authorToken);
            }
        });
    }

    private void loadAuthorImage(final ListViewHolder holder, String authorToken) {
        DatabaseHelper.getProfilePicture(authorToken, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(context).load(imageUri.toString())
                            .circleCrop()
                            .apply(new RequestOptions().override(150, 150))
                            .into(holder.authorImage);
                }
            }
        });
    }

    private void loadImage(News news, String imageName, final LinearLayout linearLayout) {
        final ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 10, 0);
        imageView.setLayoutParams(params);
        linearLayout.addView(imageView);

        //if there is an image attached
        if (imageName != null){
            DatabaseHelper.getNewsImage(news, imageName, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    if (object != null){
                        Uri uri = (Uri) object;
                        Glide.with(context).load(uri.toString())
                                .apply(new RequestOptions().override(500, 300))
                                .centerCrop()
                                .into(imageView);
                    }
                }
            });
        }
    }

    private void loadCurrentUserImage(final ListViewHolder holder){
        DatabaseHelper.getProfilePicture(Utils.getCurrentUserToken(context), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(context).load(imageUri.toString())
                            .circleCrop()
                            .apply(new RequestOptions().override(100, 100))
                            .into(holder.currentUserImage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout imageContainer, newsLayout;
        private TextView title, content, date, author;
        private ImageView authorImage, moreOptionsImage, savePostImage, writeCommentImage, sendComment, currentUserImage;

        private ListViewHolder(@NonNull final View itemView, final NewsClickListener newsClickListener, final String TAG) {
            super(itemView);
            this.imageContainer = itemView.findViewById(R.id.imageContainer);
            this.title = itemView.findViewById(R.id.title);
            this.content = itemView.findViewById(R.id.content);
            this.date = itemView.findViewById(R.id.date);
            this.author = itemView.findViewById(R.id.authorName);
            this.authorImage = itemView.findViewById(R.id.authorProfilePicture);
            this.moreOptionsImage = itemView.findViewById(R.id.moreImage);
            this.savePostImage = itemView.findViewById(R.id.savePost);
            this.writeCommentImage = itemView.findViewById(R.id.addComment);
            this.sendComment = itemView.findViewById(R.id.sendCommentButton);
            this.currentUserImage = itemView.findViewById(R.id.currentUserImage);

            this.newsLayout = itemView.findViewById(R.id.newsLayout);
            newsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onNewsClick(getAdapterPosition(), TAG);
                }
            });
            authorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onProfileClick(getAdapterPosition());
                }
            });
            moreOptionsImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onMoreOptionsClick(itemView,getAdapterPosition());
                }
            });
            savePostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onSavePost(itemView,getAdapterPosition());
                }
            });
            writeCommentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onWriteComment(itemView,getAdapterPosition());
                }
            });
            sendComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsClickListener.onSendComment(itemView,getAdapterPosition(),TAG);
                }
            });
        }


        public interface NewsClickListener{
            void onNewsClick(int position, String tag);
            void onProfileClick(int position);
            void onMoreOptionsClick(View itemView, int position);
            void onSavePost(View itemView, int position);
            void onWriteComment(View itemView, int position);
            void onSendComment(View itemView, int position, String tag);
        }

    }
}
