package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ListViewHolder> {
    private Context context;
    private List<Comment> comments;

    public CommentListAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.comment_list_item, parent, false);
        return new ListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.date.setText(comments.get(position).getDate());
        holder.content.setText(comments.get(position).getContent());
        loadAuthorData(holder,position);
    }

    private void loadAuthorData(final ListViewHolder holder, int position) {
        DatabaseHelper.getUserData(comments.get(position).getAuthor(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User author = (User) object;
                holder.authorName.setText(author.getName());
            }
        });
        DatabaseHelper.getProfilePicture(comments.get(position).getAuthor(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(context).load(imageUri.toString())
                            .circleCrop()
                            .into(holder.authorImage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage;
        private TextView authorName, date, content;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            date = itemView.findViewById(R.id.commentDate);
            content = itemView.findViewById(R.id.content);
        }
    }
}
