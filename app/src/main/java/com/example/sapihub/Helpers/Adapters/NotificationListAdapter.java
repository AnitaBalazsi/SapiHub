package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.R;

import java.text.ParseException;
import java.util.ArrayList;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ListViewHolder> {
    private Context context;
    private ArrayList<NotificationData> notificationsList;
    private ListViewHolder.NotificationClickListener clickListener;

    public NotificationListAdapter(Context context, ArrayList<NotificationData> notificationsList, ListViewHolder.NotificationClickListener clickListener) {
        this.context = context;
        this.notificationsList = notificationsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.notification_list_item, parent, false);
        return new ListViewHolder(listItem,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        loadIcon(holder.icon, notificationsList.get(position));
        holder.text.setText(notificationsList.get(position).getTitle().concat(" ").concat(notificationsList.get(position).getBody()));

        try {
            holder.date.setText(Utils.getRelativeDate(Utils.stringToDate(notificationsList.get(position).getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadIcon(ImageView icon, NotificationData notificationData) {
        if (notificationData.getTitle().contains(context.getString(R.string.newPostNotification))){
            icon.setImageResource(R.drawable.ic_group_white);
        }

        if (notificationData.getBody().contains(context.getString(R.string.commentNotification)) || notificationData.getBody().contains(context.getString(R.string.likeNotification))){
            icon.setImageResource(R.drawable.ic_comment_white);
        }

        if (notificationData.getTitle().contains(context.getString(R.string.upcomingEvent))){
            icon.setImageResource(R.drawable.ic_event_white);
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView text, date;

        public ListViewHolder(@NonNull View itemView, final NotificationClickListener clickListener) {
            super(itemView);

            icon = itemView.findViewById(R.id.notificationIcon);
            text = itemView.findViewById(R.id.notificationText);
            date = itemView.findViewById(R.id.notificationDate);
            itemView.findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onNotificationClick(getAdapterPosition());
                }
            });
        }


        public interface NotificationClickListener{
            void onNotificationClick(int position);
        }
    }
}
