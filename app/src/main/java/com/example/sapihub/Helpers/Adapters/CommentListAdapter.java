package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ListViewHolder>  {
    private Context context;
    private List<Comment> comments;
    private CommentClickListener commentClickListener;

    public CommentListAdapter(Context context, List<Comment> comments, CommentClickListener commentClickListener) {
        this.context = context;
        this.comments = comments;
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
        if (Utils.getCurrentUserToken(context).equals(comments.get(position).getAuthor())){
            holder.optionsImage.setVisibility(View.VISIBLE);
        }
        holder.date.setText(comments.get(position).getDate());
        holder.content.setText(comments.get(position).getContent());

        holder.replyListView.setAdapter(new ReplyListAdapter(context,comments.get(position).getReplies()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        holder.replyListView.setLayoutManager(layoutManager);

        loadAuthorData(holder,position);
        Utils.loadProfilePicture(context,holder.currentUserImage,Utils.getCurrentUserToken(context),100,100);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void loadAuthorData(final ListViewHolder holder, int position) {
        DatabaseHelper.getUserData(comments.get(position).getAuthor(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User author = (User) object;
                holder.authorName.setText(author.getName());
            }
        });
        Utils.loadProfilePicture(context,holder.authorImage,comments.get(position).getAuthor(),100,100);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    public class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage, optionsImage, writeComment, sendComment, currentUserImage;
        private TextView authorName, date, content;
        private RecyclerView replyListView;

        public ListViewHolder(@NonNull final View itemView, final CommentClickListener commentClickListener) {
            super(itemView);

            replyListView = itemView.findViewById(R.id.replyList);
            currentUserImage = itemView.findViewById(R.id.currentUserImage);
            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            date = itemView.findViewById(R.id.commentDate);
            content = itemView.findViewById(R.id.content);
            optionsImage = itemView.findViewById(R.id.moreOptions);
            optionsImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onMoreOptionsClick(itemView,getAdapterPosition());
                }
            });
            writeComment = itemView.findViewById(R.id.addComment);
            writeComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onWriteReplyClick(itemView,getAdapterPosition());
                }
            });
            sendComment = itemView.findViewById(R.id.sendComment);
            sendComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onSendReplyClick(itemView,getAdapterPosition());
                }
            });

        }
    }

    public interface CommentClickListener{
        void onMoreOptionsClick(View itemView, int position);
        void onWriteReplyClick(View itemView, int position);
        void onSendReplyClick(View itemView, int position);
    }
}
