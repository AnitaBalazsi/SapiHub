package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Message;
import com.example.sapihub.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ListViewHolder> {
    private static int user_type_sender = 0;
    private static int user_type_receiver = 1;
    Context context;
    List<Message> messageList;

    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
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
        if (Utils.getCurrentUserToken(context).equals(messageList.get(position).getSender())){
            //current user is sender
            return user_type_sender;
        } else {
            return user_type_receiver;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        if (messageList.get(position).getType().equals("text")){ //todo
            holder.message.setVisibility(View.VISIBLE);
            holder.imageMessage.setVisibility(View.GONE);
            holder.message.setText(messageList.get(position).getContent());
        } else {
            holder.message.setVisibility(View.GONE);
            holder.imageMessage.setVisibility(View.VISIBLE);
            loadImageMessage(holder.imageMessage,messageList.get(position).getContent());
        }

        holder.date.setText(messageList.get(position).getDate());
        loadProfileImage(holder,position);

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
    }

    private void loadImageMessage(ImageView imageView, String imageUri){
        Glide.with(context).load(imageUri)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView);
    }

    private void loadProfileImage(final ListViewHolder holder, int position){
        DatabaseHelper.getProfilePicture(messageList.get(position).getSender(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(context).load(imageUri.toString())
                            .circleCrop()
                            .apply(new RequestOptions().override(100, 100))
                            .into(holder.profilePicture);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView message, date, isSeen;
        private ImageView profilePicture, imageMessage;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            imageMessage = itemView.findViewById(R.id.messageImage);
            date = itemView.findViewById(R.id.messageDate);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            isSeen = itemView.findViewById(R.id.isSeen);

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (date.getVisibility() == View.VISIBLE){
                        date.setVisibility(View.GONE);
                    } else {
                        date.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }
}
