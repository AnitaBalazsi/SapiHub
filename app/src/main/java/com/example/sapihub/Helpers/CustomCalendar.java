package com.example.sapihub.Helpers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sapihub.Model.Event;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CustomCalendar extends LinearLayout implements View.OnClickListener {
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
    private String username;
    private ProgressDialog loadingDialog;

    private GridView gridView;
    private TextView selectedDate;
    private ImageView previousMonth, nextMonth;
    private GridAdapter gridAdapter;
    private Context context;

    private List<Date> dates = new ArrayList<>();
    private List<Event> eventsList = new ArrayList<>();

    public CustomCalendar(Context context) {
        super(context);
    }

    public CustomCalendar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setMessage(getContext().getString(R.string.loading));

        getCurrentUser();
        getEvents(new FirebaseCallback() {
            @Override
            public void onCallback() {
                initializeCalendarView();
                setupCalendar();
                loadingDialog.dismiss();
            }
        });
    }

    public CustomCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void getCurrentUser(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        username = username.replace("."," "); //firebase path cant contain '.' character
    }

    private void initializeCalendarView(){
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar_layout,this);

        gridView = view.findViewById(R.id.gridLayout);
        selectedDate = view.findViewById(R.id.selectedDate);
        previousMonth = view.findViewById(R.id.previous);
        nextMonth = view.findViewById(R.id.next);

        previousMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = inflater.inflate(R.layout.add_event_layout,null);
                builder.setView(dialogView);

                TextView selectedDate = dialogView.findViewById(R.id.selectedDate);
                Button sendButton = dialogView.findViewById(R.id.sendButton);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);
                final EditText eventMessage = dialogView.findViewById(R.id.eventMessage);

                final String date = DateFormat.getDateInstance(DateFormat.SHORT).format(dates.get(position));
                selectedDate.setText(date);

                final AlertDialog addEventDialog = builder.create();
                addEventDialog.show();


                cancelButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addEventDialog.dismiss();
                    }
                });
                sendButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message = eventMessage.getText().toString().trim();
                        DatabaseHelper.addEvent(username, new Event(message,date));
                        addEventDialog.dismiss();
                    }
                });
            }
        });
    }

    private void getEvents(final FirebaseCallback callback){
        loadingDialog.show();
        DatabaseHelper.eventsReference.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event event = eventData.getValue(Event.class);
                    eventsList.add(event);
                }
                callback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private interface FirebaseCallback{
        void onCallback();
    }

    private void setupCalendar(){
        dates.clear();

        //calculate dates for calendar
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);

        int firstDay = monthCalendar.get(Calendar.DAY_OF_WEEK) - 2;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDay);

        while (dates.size() < 42){
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);
        }

        gridAdapter = new GridAdapter(context,dates,calendar,eventsList);
        gridView.setAdapter(gridAdapter);

        selectedDate.setText(formatter.format(calendar.getTime()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.previous:
                calendar.add(Calendar.MONTH,-1);
                setupCalendar();
            break;
            case R.id.next:
                calendar.add(Calendar.MONTH,1);
                setupCalendar();
                break;
        }
    }
}
