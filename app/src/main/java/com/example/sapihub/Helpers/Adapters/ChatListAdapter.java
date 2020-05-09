package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ListViewHolder> {
    private List<Chat> chatList;
    private Context context;
    private ContactClickListener contactClickListener;
    private String userName;
    private String currentUser;

    public ChatListAdapter(List<Chat> chatList, Context context, ContactClickListener contactClickListener) {
        this.chatList = chatList;
        this.context = context;
        this.contactClickListener = contactClickListener;
        currentUser = Utils.getCurrentUserToken(context);
    }

    @NonNull
    @Override
    public ChatListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.chatroom_list_item, parent, false);
        return new ListViewHolder(listItem,contactClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListAdapter.ListViewHolder holder, int position) {
        Chat chatRoom = chatList.get(position);
        final Message lastMessage = chatRoom.getMessages().get(chatRoom.getMessages().size() - 1);

        String userId = getUserId(position);
        getUser(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                holder.userName.setText(user.getName());
                loadLastMessage(lastMessage,holder);
                if (user.getStatus().equals(context.getString(R.string.online))){
                    holder.onlineIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        Utils.loadProfilePicture(context,holder.userProfile,userId,150,150);

        holder.messageDate.setText(lastMessage.getDate());  //todo format date
        if (lastMessage.getSender().equals(currentUser) && lastMessage.isSeen()){
            //appears receiver's image when seen
            Utils.loadProfilePicture(context,holder.notificationImage,userId,50,50);
        }

        if (!lastMessage.getSender().equals(currentUser) && !lastMessage.isSeen()){
            //appears green dot to notify user of new message
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
            holder.messageDate.setTypeface(null,Typeface.BOLD);
            holder.notificationImage.setImageResource(R.color.colorGreen);
            holder.notificationImage.setLayoutParams(new FrameLayout.LayoutParams(30,30));
        } else {
            holder.lastMessage.setTypeface(null,Typeface.NORMAL);
            holder.messageDate.setTypeface(null,Typeface.NORMAL);
            holder.notificationImage.setImageResource(0);
        }
    }

    private void loadLastMessage(Message lastMessage, ListViewHolder holder) {
        switch (lastMessage.getType()){
            case "text":
                holder.lastMessage.setText(lastMessage.getContent());
                break;
            case "image":
                if (lastMessage.getSender().equals(currentUser)){
                    holder.lastMessage.setText(context.getString(R.string.imageSent));
                } else {
                    holder.lastMessage.setText(userName.concat(" ").concat(context.getString(R.string.imageReceived)));
                }
                break;
            case "video":
                if (lastMessage.getSender().equals(currentUser)){
                    holder.lastMessage.setText(context.getString(R.string.videoSent));
                } else {
                    holder.lastMessage.setText(userName.concat(" ").concat(context.getString(R.string.videoReceived)));
                }
                break;
            case "sharedPost":
                if (lastMessage.getSender().equals(currentUser)){
                    holder.lastMessage.setText(context.getString(R.string.postSent));
                } else {
                    holder.lastMessage.setText(userName.concat(" ").concat(context.getString(R.string.postReceived)));
                }
                break;
        }
    }

    private String getUserId(int position) {
        for (String user : chatList.get(position).getUsers()){
            if (!user.equals(Utils.getCurrentUserToken(context))){
                return user;
            }
        }
        return null;
    }

    private void getUser(String userId, final FirebaseCallback callback) {
        DatabaseHelper.getUserData(userId, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                callback.onCallback(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView userName, lastMessage, messageDate;
        ImageView userProfile, notificationImage, onlineIcon;
        public ListViewHolder(@NonNull View itemView, final ContactClickListener contactClickListener) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            onlineIcon = itemView.findViewById(R.id.onlineIcon);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            userProfile = itemView.findViewById(R.id.profilePicture);
            messageDate = itemView.findViewById(R.id.messageDate);
            notificationImage = itemView.findViewById(R.id.notificationImage);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactClickListener.onContactClick(getAdapterPosition());
                }
            });
        }
    }

    public interface ContactClickListener{
        void onContactClick(int position);
    }
}
