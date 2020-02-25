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
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Notification;
import com.example.sapihub.R;

import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ListViewHolder> {
    private List<Notification> notificationList;
    private Context context;

    public NotificationListAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }


    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.notification_list_item, parent, false);
        return new ListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.title.setText(notificationList.get(position).getTitle());
        holder.message.setText(notificationList.get(position).getMessage());
        holder.date.setText(notificationList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, message, date;
        private ImageView deleteNotification;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            date = itemView.findViewById(R.id.notificationDate);
            deleteNotification = itemView.findViewById(R.id.deleteNotification);
            deleteNotification.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // delete from db
            DatabaseHelper.deleteNotification(Utils.getCurrentUserToken(context),notificationList.get(getAdapterPosition()));

            // delete from list
            notificationList.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
            notifyItemRangeChanged(getAdapterPosition(),notificationList.size());
        }
    }
}
