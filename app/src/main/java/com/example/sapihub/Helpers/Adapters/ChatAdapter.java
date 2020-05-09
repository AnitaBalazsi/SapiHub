package com.example.sapihub.Helpers.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ListViewHolder> {
    private static int user_type_sender = 0;
    private static int user_type_receiver = 1;
    private Context context;
    private List<Message> messageList;
    private OnSharedPostClickListener clickListener;

    public ChatAdapter(Context context, List<Message> messageList, OnSharedPostClickListener clickListener) {
        this.context = context;
        this.messageList = messageList;
        this.clickListener = clickListener;
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
        return new ListViewHolder(listItem,clickListener);
    }

    @Override
    public int getItemViewType(int position) {
        if (Utils.getCurrentUserToken(context).equals(messageList.get(position).getSender())){
            //current user is sender
            return user_type_sender;
        } else {
            return user_type_receiver;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, final int position) {
        switch (messageList.get(position).getType()){
            case "text":
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(messageList.get(position).getContent());
                break;
            case "image":
                holder.imageMessage.setVisibility(View.VISIBLE);
                holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImage(holder,position);
                    }
                });
                loadImageMessage(holder.imageMessage,messageList.get(position).getContent());
                break;
            case "video":
                holder.videoMessage.setVisibility(View.VISIBLE);
                loadVideoMessage(holder,messageList.get(position).getContent());
            case "sharedPost":
                loadPost(position, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        News news = (News) object;
                        Utils.loadProfilePicture(context,holder.authorImage,news.getAuthor(),100,100);
                        holder.postContent.setText(news.getContent());
                        loadAuthorName(holder.postAuthorName,news.getAuthor());
                        holder.sharedPost.setVisibility(View.VISIBLE);
                    }
                });
                break;
        }

        if (position == messageList.size()-1 && getItemViewType(position) == user_type_sender){
            if (messageList.get(position).isSeen()){
                holder.isSeen.setText(context.getString(R.string.messageSeen));
            } else {
                holder.isSeen.setText(context.getString(R.string.messageDelivered));
            }
            holder.isSeen.setVisibility(View.VISIBLE);
        } else {
            holder.isSeen.setVisibility(View.GONE);
        }

        holder.messageDate.setText(messageList.get(position).getDate()); //todo parse

        loadProfileImage(holder,position);
        loadUrlPreview(holder,position);
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

    private void loadPost(final int position, final FirebaseCallback callback) {
        DatabaseHelper.newsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    if (newsData.getKey().equals(messageList.get(position).getContent())){
                        callback.onCallback(newsData.getValue(News.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadProfileImage(ListViewHolder holder, int position) {
        if (position > 0){
            long diff = Utils.differenceBetweenDates(messageList.get(position).getDate(),messageList.get(position - 1).getDate());
            if (diff > 200 || !messageList.get(position).getSender().equals(messageList.get(position-1).getSender())){
                Utils.loadProfilePicture(context,holder.profilePicture,messageList.get(position).getSender(),100,100);
                holder.date.setText(messageList.get(position).getDate());
                holder.date.setVisibility(View.VISIBLE);
            } else {
                holder.profilePicture.setImageDrawable(context.getDrawable(R.color.colorWhite));
                holder.profilePicture.getLayoutParams().height = 100;
                holder.profilePicture.getLayoutParams().width = 100;
            }
        } else {
            Utils.loadProfilePicture(context,holder.profilePicture,messageList.get(position).getSender(),100,100);
        }
    }

    private void loadUrlPreview(final ListViewHolder holder, int position) {
        if (android.util.Patterns.WEB_URL.matcher(messageList.get(position).getContent()).matches()){
            String url = messageList.get(position).getContent();
            holder.message.setLinksClickable(true);

            if (!messageList.get(position).getContent().startsWith("http")){
                url = "http://".concat(messageList.get(position).getContent());
            }

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

    private void loadVideoMessage(final ListViewHolder holder, final String videoUri){
        holder.videoProgress.setVisibility(View.VISIBLE);
        holder.videoMessage.setVideoPath(videoUri);
        holder.videoMessage.seekTo(1);
        holder.videoMessage.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.videoProgress.setVisibility(View.GONE);
            }
        });
        holder.videoMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideo(videoUri,holder);
            }
        });
    }

    private void showImage(ListViewHolder holder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        final ImageView imageView = new ImageView(context);

        loadImageMessage(holder.imageMessage,messageList.get(position).getContent());

        builder.setView(imageView);
        AlertDialog imageDialog = builder.create();
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.show();
    }

    private void showVideo(String videoUri, final ListViewHolder holder) {
        holder.videoProgress.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(context,android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        final VideoView videoView = new VideoView(context);
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);

        videoView.setVideoPath(videoUri);
        videoView.setMediaController(mediaController);
        videoView.seekTo(1); //sets preview to first frame
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.videoProgress.setVisibility(View.GONE);
            }
        });
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()){
                    videoView.pause();
                } else {
                    videoView.start();
                }
            }
        });

        builder.setView(videoView);
        AlertDialog imageDialog = builder.create();
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.show();
        videoView.start();
    }

    private void loadImageMessage(ImageView imageView, String imageUri){
        Glide.with(context).load(imageUri)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView message, messageDate, isSeen, date, postAuthorName, postContent;
        private ImageView profilePicture, imageMessage, authorImage;
        private RichLinkView richLinkView;
        private LinearLayout sharedPost;
        private VideoView videoMessage;
        private ProgressBar videoProgress;

        public ListViewHolder(@NonNull View itemView, final OnSharedPostClickListener clickListener) {
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
            postContent = itemView.findViewById(R.id.postContent);
            authorImage = itemView.findViewById(R.id.postAuthor);
            videoMessage = itemView.findViewById(R.id.messageVideo);
            videoProgress = itemView.findViewById(R.id.progressBar);

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
            sharedPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onSharedPostClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnSharedPostClickListener{
        void onSharedPostClick(int position);
    }
}
