package com.example.sapihub.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Adapters.EventListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Event;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeadlinesFragment extends Fragment {
    private RecyclerView listView;
    private RecyclerView.LayoutManager layoutManager;
    private EventListAdapter eventListAdapter;
    private List<Event> eventList = new ArrayList<>();

    public DeadlinesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deadlines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeVariables();
        getData();
    }

    private void getData() {
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserToken(getContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event event = eventData.getValue(Event.class);

                    //shows only events that are not over yet
                    try {
                        Date todayDate = new Date();
                        Date eventDate = Utils.stringToDate(event.getDate());
                        if (eventDate.after(todayDate)){
                            eventList.add(event);
                            eventListAdapter.notifyDataSetChanged();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeVariables() {
        listView = getView().findViewById(R.id.eventList);

        layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        eventListAdapter = new EventListAdapter(eventList);
        listView.setAdapter(eventListAdapter);
    }


}
