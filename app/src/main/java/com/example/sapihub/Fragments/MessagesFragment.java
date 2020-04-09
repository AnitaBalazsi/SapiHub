package com.example.sapihub.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Activities.ChatActivity;
import com.example.sapihub.Activities.UserProfileActivity;
import com.example.sapihub.Helpers.Adapters.ChatListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Chat;
import com.example.sapihub.Model.Message;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment implements ChatListAdapter.ContactClickListener {
    private RecyclerView chatListView;
    private String currentUser;
    private List<Chat> chatList = new ArrayList<>();
    private ChatListAdapter adapter;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeVariables();
        getMessageList(new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                Collections.sort(chatList, Collections.reverseOrder(new Comparator<Chat>() {
                    @Override
                    public int compare(Chat o1, Chat o2) {
                        Message l1 = o1.getMessages().get(o1.getMessages().size() - 1);
                        Message l2 = o2.getMessages().get(o2.getMessages().size() - 1);
                        try {
                            return Utils.stringToDate(l1.getDate()).compareTo(Utils.stringToDate(l2.getDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                }));
                adapter.notifyDataSetChanged();
                //TODO COMPARE FIX
            }
        });
    }

    private void initializeVariables() {
        currentUser = Utils.getCurrentUserToken(getContext());

        chatListView = getView().findViewById(R.id.chatListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatListView.setLayoutManager(layoutManager);
        chatListView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        adapter = new ChatListAdapter(chatList,getContext(),this);
        chatListView.setAdapter(adapter);
    }

    private void getMessageList(final FirebaseCallback callback){
        DatabaseHelper.chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot chatData : dataSnapshot.getChildren()){
                    Chat chat = chatData.getValue(Chat.class);
                    if (chat.getUsers().contains(currentUser)){
                        chatList.add(chat);
                    }
                }
                callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onContactClick(int position) {
        for (String user : chatList.get(position).getUsers()){
            if (!user.equals(Utils.getCurrentUserToken(getContext()))){
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("userId",user);
                startActivity(intent);
            }
        }

    }
}
