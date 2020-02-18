package com.example.sapihub.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Adapters.NotificationListAdapter;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Notification;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {
    private NotificationListAdapter adapter;
    private ProgressDialog loadingDialog;
    private ArrayList<Notification> notificationList = new ArrayList<>();

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
        initializeVariables();
        loadingDialog.show();
        getData();
    }

    private void initializeVariables() {
        RecyclerView listView = getView().findViewById(R.id.notificationList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        listView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        adapter = new NotificationListAdapter(notificationList, getContext());
        listView.setAdapter(adapter);

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setMessage(getString(R.string.loading));
    }

    private void getData(){
        DatabaseHelper.notificationsReference.child(Utils.getCurrentUserName(getContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    notificationList.add(newsData.getValue(Notification.class));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        loadingDialog.dismiss();
    }
}
