package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class ReplyListAdapter extends RecyclerView.Adapter<ReplyListAdapter.ListViewHolder>  {
    private Context context;
    private List<Comment> replies;

    public ReplyListAdapter(Context context, List<Comment> replies) {
        this.context = context;
        this.replies = replies;
    }

    @NonNull
    @Override
    public ReplyListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.comment_list_item, parent, false);
        listItem.findViewById(R.id.addComment).setVisibility(View.GONE);
        return new ListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyListAdapter.ListViewHolder holder, int position) {
        if (Utils.getCurrentUserToken(context).equals(replies.get(position).getAuthor())){
            holder.optionsImage.setVisibility(View.VISIBLE);
        }
        holder.date.setText(replies.get(position).getDate());
        holder.content.setText(replies.get(position).getContent());
        loadAuthorData(holder,position);
    }

    private void loadAuthorData(final ListViewHolder holder, int position) {
        DatabaseHelper.getUserData(replies.get(position).getAuthor(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User author = (User) object;
                holder.authorName.setText(author.getName());
            }
        });
        Utils.loadProfilePicture(context,holder.authorImage,replies.get(position).getAuthor(),100,100);
    }
    @Override
    public int getItemCount() {
        return replies.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage, optionsImage;
        private TextView authorName, date, content;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            date = itemView.findViewById(R.id.commentDate);
            content = itemView.findViewById(R.id.content);
            optionsImage = itemView.findViewById(R.id.moreOptions);

        }
    }
}
