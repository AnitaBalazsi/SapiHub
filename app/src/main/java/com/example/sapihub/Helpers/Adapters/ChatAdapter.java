package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;

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
    public long getItemId(int position) {
        return position;
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
                        Utils.showImageDialog(context, null);
                    }
                });
                loadImageMessage(holder.imageMessage,model.getContent());
                break;
            case "file":
                StorageReference ref = DatabaseHelper.chatAttachments.child(model.getSender().concat(model.getDate()));
                ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (final StorageReference file : listResult.getItems()){
                            holder.fileName.setText(file.getName());
                            if (getItemViewType(position) == user_type_receiver){
                                holder.fileName.setTextColor(context.getColor(R.color.colorWhite));
                            }
                            holder.fileLayout.setVisibility(View.VISIBLE);
                            holder.fileAttachment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Utils.downloadFile(context,model.getContent(),uri);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

                break;
            case "sharedPost":
                loadPost(model, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        News news = (News) object;
                        Utils.loadProfilePicture(context,holder.authorImage,news.getAuthor(),100,100);
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

        loadProfileImage(holder,model);
        loadUrlPreview(holder, model);
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
                Log.d("ChatLoadAuthor",databaseError.getMessage());
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
                Log.d("ChatLoadPost",databaseError.getMessage());
            }
        });
    }

    private void loadProfileImage(ListViewHolder holder, Message message) {
        Utils.loadProfilePicture(context,holder.profilePicture,message.getSender(),100,100);

    }

    private void loadUrlPreview(final ListViewHolder holder, final Message message) {
        String url = checkIfContainsUrl(message.getContent());
        if (url != null){
            holder.richLinkView.setLink(url, new ViewListener() {
                @Override
                public void onSuccess(boolean status) {
                    holder.richLinkView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    Log.d("LoadLinkView",e.getMessage());
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


    private void loadImageMessage(ImageView imageView, String imageUri){
        Glide.with(context).load(imageUri)
                .apply(new RequestOptions().override(300, 300))
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(15)))
                .into(imageView);
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView message, messageDate, isSeen, date, postAuthorName, postTitle, postDate, fileName;
        private ImageView profilePicture, imageMessage, authorImage;
        private RichLinkView richLinkView;
        private LinearLayout sharedPost, fileLayout;
        private View fileAttachment;

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
            fileLayout = itemView.findViewById(R.id.fileLayout);
            fileAttachment = itemView.findViewById(R.id.fileAttachment);
            fileAttachment.findViewById(R.id.deleteFile).setVisibility(View.GONE);
            fileName = fileAttachment.findViewById(R.id.fileName);

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
