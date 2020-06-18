package com.example.sapihub.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Activities.ChatActivity;
import com.example.sapihub.Activities.UserProfileActivity;
import com.example.sapihub.Helpers.Adapters.UserListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatUsersFragment extends Fragment implements UserListAdapter.UserClickListener, SearchView.OnQueryTextListener {
    private RecyclerView recyclerView;
    private List<User> userList = new ArrayList<>();
    private UserListAdapter adapter;
    private SearchView searchView;
    private static Context context;

    public ChatUsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        initializeVariables();
        getUsers();
    }

    private void getUsers() {
        DatabaseHelper.userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userData : dataSnapshot.getChildren()){
                    User user = userData.getValue(User.class);
                    if (!user.getUserId().getToken().equals(Utils.getCurrentUserToken(context))){
                        userList.add(userData.getValue(User.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeVariables() {
        recyclerView = getView().findViewById(R.id.userListView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new UserListAdapter(context,userList,this);
        recyclerView.setAdapter(adapter);

        searchView = getView().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onUserClick(int position) {
        Intent profileIntent = new Intent(getActivity(), ChatActivity.class);
        profileIntent.putExtra("userId",userList.get(position).getUserId().getToken());
        startActivity(profileIntent);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        if (!query.isEmpty()){
            userList.clear();
            DatabaseHelper.userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userData : dataSnapshot.getChildren()){
                        User user = userData.getValue(User.class);
                        if (!user.getUserId().getToken().equals(Utils.getCurrentUserToken(context)) && user.getName().contains(query)){
                            userList.add(userData.getValue(User.class));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
           getUsers();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (newText.length() > 0) {
            userList.clear();
            DatabaseHelper.userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userData : dataSnapshot.getChildren()){
                     User user = userData.getValue(User.class);
                     if (!user.getUserId().getToken().equals(Utils.getCurrentUserToken(context)) && user.getName().contains(newText)){
                        userList.add(userData.getValue(User.class));
                     }
                     adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            getUsers();
        }
        return true;
    }
}
