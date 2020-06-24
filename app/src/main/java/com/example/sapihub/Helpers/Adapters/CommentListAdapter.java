package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.text.ParseException;
import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ListViewHolder> {
    private Context context;
    private List<Comment> commentList;
    private ListViewHolder.CommentClickListener commentClickListener;

    public CommentListAdapter(Context context, List<Comment> commentList, ListViewHolder.CommentClickListener commentClickListener) {
        this.context = context;
        this.commentList = commentList;
        this.commentClickListener = commentClickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.comment_list_item, parent, false);
        return new ListViewHolder(listItem,commentClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Comment model = commentList.get(position);
        if (Utils.getCurrentUserToken(context).equals(model.getAuthor())){
            holder.optionsImage.setVisibility(View.VISIBLE);
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

        if (model.getLikes() == null){
            holder.likeCounter.setText("0");
        } else {
            if (model.getLikes().contains(Utils.getCurrentUserToken(context))){
                holder.likeComment.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_24dp));
            }
            holder.likeCounter.setText(String.valueOf(model.getLikes().size()));
        }

        loadAuthorData(holder,model);

        if (model.getImages() != null){
            holder.commentImages.setVisibility(View.VISIBLE);
            holder.commentImages.removeAllViews();
            for (String image : model.getImages()){
                loadImages(holder.commentImages,model, image);
            }
        }
    }

    private void loadImages(LinearLayout commentImages, Comment comment, String imageName) {
        final ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 10, 0);
        imageView.setLayoutParams(params);
        commentImages.addView(imageView);

        //if there is an image attached
        DatabaseHelper.getCommentAttachment(comment, imageName, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    final Uri uri = (Uri) object;
                    Glide.with(context).load(uri.toString())
                            .apply(new RequestOptions().override(150, 150))
                            .centerCrop()
                            .into(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.showImageDialog(context,null);
                        }
                    });
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void loadAuthorData(final ListViewHolder holder, Comment model) {
        DatabaseHelper.getUserData(model.getAuthor(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User author = (User) object;
                holder.authorName.setText(author.getName());
            }
        });
        Utils.loadProfilePicture(context,holder.authorImage,model.getAuthor(),100,100);
    }


    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage, optionsImage, likeComment;
        private TextView authorName, date, content, likeCounter;
        private LinearLayout commentImages;

        public ListViewHolder(@NonNull final View itemView, final CommentClickListener commentClickListener) {
            super(itemView);

            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            date = itemView.findViewById(R.id.commentDate);
            content = itemView.findViewById(R.id.content);
            optionsImage = itemView.findViewById(R.id.moreOptions);
            commentImages = itemView.findViewById(R.id.commentImages);
            likeCounter = itemView.findViewById(R.id.likeCounter);
            likeComment = itemView.findViewById(R.id.likeComment);

            authorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onAuthorClick(getAdapterPosition());
                }
            });
            optionsImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onMoreOptionsClick(optionsImage,getAdapterPosition());
                }
            });
            likeComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onLikeCommentClick(likeComment,getAdapterPosition());
                }
            });
            likeCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onSeeLikesClick(getAdapterPosition());
                }
            });
        }

        public interface CommentClickListener{
            void onAuthorClick(int position);
            void onMoreOptionsClick(View itemView, int position);
            void onLikeCommentClick(View itemView, int position);
            void onSeeLikesClick(int position);
        }
    }

}
