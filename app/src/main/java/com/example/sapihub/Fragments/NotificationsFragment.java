package com.example.sapihub.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Activities.DeadlinesActivity;
import com.example.sapihub.Activities.NewsDetailsActivity;
import com.example.sapihub.Helpers.Adapters.NotificationListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Notifications.NotificationData;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment implements NotificationListAdapter.ListViewHolder.NotificationClickListener {
    private ArrayList<NotificationData> notificationList = new ArrayList<>();
    private String user;
    private NotificationListAdapter adapter;

    private static Context context;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        initializeVariables();
        getData();
    }

    private void initializeVariables() {
        RecyclerView listView = getView().findViewById(R.id.notificationList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        listView.setLayoutManager(layoutManager);

        adapter = new NotificationListAdapter(context, notificationList,this);
        listView.setAdapter(adapter);

        new ItemTouchHelper(itemTouch).attachToRecyclerView(listView);
    }

    private void getData(){
        user = Utils.getCurrentUserToken(context);
        DatabaseHelper.notificationsReference.child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    NotificationData notificationData = data.getValue(NotificationData.class);
                    if (checkNotification(notificationData)){
                        notificationList.add(notificationData);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getNotifications",databaseError.getMessage());
            }
        });
    }

    private boolean checkNotification(NotificationData notificationData) {
        String notificationText = notificationData.getTitle();
        if (notificationText.contains(context.getString(R.string.newPostNotification))){
            if (Utils.checkIfNotificationEnabled(context.getString(R.string.postNotification),context)){
                return true;
            }
        }

        if (notificationData.getBody().contains(context.getString(R.string.commentNotification)) ||
            notificationData.getBody().contains(getString(R.string.likeNotification))){
            if (Utils.checkIfNotificationEnabled(context.getString(R.string.commentNotifSetting),context)){
                return true;
            }
        }

        if (notificationText.contains(context.getString(R.string.upcomingEvent))){
            if (Utils.checkIfNotificationEnabled(context.getString(R.string.eventNotification),context)){
                return true;
            }
        }

        return false;
    }

    ItemTouchHelper.SimpleCallback itemTouch = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT ) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DatabaseHelper.removeNotification(notificationList.get(viewHolder.getAdapterPosition()));
            notificationList.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onNotificationClick(int position) {
        NotificationData notificationData = notificationList.get(position);
        if (notificationData.getTitle().contains(context.getString(R.string.upcomingEvent))){
            startActivity(new Intent(context, DeadlinesActivity.class));
        }

        if (notificationData.getBody().contains(context.getString(R.string.commentNotification)) ||
                notificationData.getBody().contains(getString(R.string.likeNotification)) ||
                notificationData.getTitle().contains(context.getString(R.string.newPostNotification))){
            DatabaseHelper.getNews(notificationData.getTitle(), Utils.getCurrentUserToken(context), new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    News news = (News) object;
                    Intent openDetails = new Intent(context, NewsDetailsActivity.class);
                    openDetails.putExtra("selectedNews", news);
                    context.startActivity(openDetails);
                }
            });
        }
    }
}
