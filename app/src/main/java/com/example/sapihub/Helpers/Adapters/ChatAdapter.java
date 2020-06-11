package com.example.sapihub.Helpers.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.List;

import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;

public class ChatAdapter extends FirebaseRecyclerAdapter<Message, ChatAdapter.ListViewHolder> {
    private static int user_type_sender = 0;
    private static int user_type_receiver = 1;
    private Context context;

    public ChatAdapter(@NonNull FirebaseRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
    }


    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem;
        if (viewType == user_type_sender){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            listItem = layoutInflater.inflate(R.layout.chat_item_sender, parent, false);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            listItem = layoutInflater.inflate(R.layout.chat_item_receiver, parent, false);
        }
        return new ListViewHolder(listItem);
    }

    @Override
    public int getItemViewType(int position) {
        if (Utils.getCurrentUserToken(context).equals(getItem(position).getSender())){
            //current user is sender
            return user_type_sender;
        } else {
            return user_type_receiver;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final ListViewHolder holder, final int position, @NonNull final Message model) {
        switch (model.getType()){
            case "text":
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(model.getContent());
                break;
            case "image":
                holder.imageMessage.setVisibility(View.VISIBLE);
                holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       Utils.showImageDialog(context, Uri.parse(model.getContent()));
                    }
                });
                loadImageMessage(holder.imageMessage,model.getContent());
                break;
            case "sharedPost":
                loadPost(model, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        News news = (News) object;
                        DatabaseHelper.loadProfilePicture(context,holder.authorImage,news.getAuthor(),100,100);
                        holder.postTitle.setText(news.getTitle());
                        try {
                            holder.postDate.setText(Utils.getRelativeDate(Utils.stringToDate(news.getDate())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        loadAuthorName(holder.postAuthorName,news.getAuthor());
                        holder.sharedPost.setVisibility(View.VISIBLE);
                    }
                });
                break;
        }

        if (position == getItemCount()-1 && getItemViewType(position) == user_type_sender){
            if (model.isSeen()){
                holder.isSeen.setText(context.getString(R.string.messageSeen));
            } else {
                holder.isSeen.setText(context.getString(R.string.messageDelivered));
            }
            holder.isSeen.setVisibility(View.VISIBLE);
        } else {
            holder.isSeen.setVisibility(View.GONE);
        }

        try {
            holder.messageDate.setText(Utils.getRelativeDate(Utils.stringToDate(model.getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        loadProfileImage(holder,position,model);
        loadUrlPreview(holder,model);
    }

    private void loadAuthorName(final TextView postAuthorName, String author) {
        DatabaseHelper.userReference.child(author).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                postAuthorName.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPost(final Message message, final FirebaseCallback callback) {
        DatabaseHelper.newsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    if (newsData.getKey().equals(message.getContent())){
                            callback.onCallback(newsData.getValue(News.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadProfileImage(ListViewHolder holder, int position,  Message message) {
        if (message.equals(getItem(position))){
            DatabaseHelper.loadProfilePicture(context,holder.profilePicture,getItem(position).getSender(),100,100);
        }

    }

    private void loadUrlPreview(final ListViewHolder holder, Message model) {
        if (android.util.Patterns.WEB_URL.matcher(model.getContent()).matches()){
            String url = model.getContent();
            holder.message.setLinksClickable(true);

            if (!model.getContent().startsWith("http")){
                url = "http://".concat(model.getContent());
            } // todo

            holder.richLinkView.setLink(url, new ViewListener() {
                @Override
                public void onSuccess(boolean status) {
                    holder.richLinkView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                }
            });
        } else {
            holder.richLinkView.setVisibility(View.GONE);
        }
    }


    private void loadImageMessage(ImageView imageView, String imageUri){
        Glide.with(context).load(imageUri)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView);
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView message, messageDate, isSeen, date, postAuthorName, postTitle, postDate;
        private ImageView profilePicture, imageMessage, authorImage;
        private RichLinkView richLinkView;
        private LinearLayout sharedPost;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            imageMessage = itemView.findViewById(R.id.messageImage);
            messageDate = itemView.findViewById(R.id.messageDate);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            richLinkView = itemView.findViewById(R.id.richLinkView);
            isSeen = itemView.findViewById(R.id.isSeen);

            sharedPost = itemView.findViewById(R.id.sharedPostLayout);
            postAuthorName = itemView.findViewById(R.id.postAuthorName);
            postTitle = itemView.findViewById(R.id.postTitle);
            authorImage = itemView.findViewById(R.id.postAuthor);
            postDate = itemView.findViewById(R.id.postDate);

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageDate.getVisibility() == View.VISIBLE) {
                        messageDate.setVisibility(View.GONE);
                    } else {
                        messageDate.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
    }
}
