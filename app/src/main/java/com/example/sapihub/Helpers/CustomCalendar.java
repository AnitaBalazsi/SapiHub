package com.example.sapihub.Helpers;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Adapters.EventListAdapter;
import com.example.sapihub.Helpers.Adapters.GridAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Model.Event;
import com.example.sapihub.Model.Notification;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CustomCalendar extends LinearLayout implements View.OnClickListener {
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
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

        getEvents(new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                initializeCalendarView();
                setupCalendar();
                loadingDialog.dismiss();
            }
        });
    }

    public CustomCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                if (view.getVisibility() == View.VISIBLE){
                    showDialog(position);
                }
            }
        });
    }

    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View dialogView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.add_event_layout,null);
        builder.setView(dialogView);

        final TextView selectedDate = dialogView.findViewById(R.id.selectedDate);
        Button sendButton = dialogView.findViewById(R.id.sendButton);
        final EditText eventMessage = dialogView.findViewById(R.id.eventMessage);
        final ImageView timePicker = dialogView.findViewById(R.id.timePicker);
        final TextView selectedTime = dialogView.findViewById(R.id.selectedTime);
        final LinearLayout seeEvents = dialogView.findViewById(R.id.seeEvents);

        selectedDate.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(dates.get(position)));
        final RecyclerView listView = dialogView.findViewById(R.id.eventList);
        createEventList(listView, dates.get(position));

        final AlertDialog addEventDialog = builder.create();
        addEventDialog.show();

        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventMessage.getText().toString().isEmpty()){
                    eventMessage.setError(getResources().getString(R.string.emptyField));
                    eventMessage.requestFocus();
                } else {
                    sendEvent(eventMessage,position);
                    addEventDialog.dismiss();
                }
            }
        });
        seeEvents.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView arrowIcon = dialogView.findViewById(R.id.arrowIcon);
                TextView seeEventsText = dialogView.findViewById(R.id.seeEventsText);

                if (listView.getVisibility() == View.GONE){
                    listView.setVisibility(View.VISIBLE);
                    arrowIcon.setRotation(270);
                    seeEventsText.setText(R.string.hideEvents);
                } else {
                    listView.setVisibility(View.GONE);
                    arrowIcon.setRotation(90);
                    seeEventsText.setText(R.string.seeEvents);
                }
            }
        });
        timePicker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                final int hours = calendar.get(Calendar.HOUR_OF_DAY);
                final int minutes = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(dialogView.getContext(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.setTimeZone(TimeZone.getDefault());
                                selectedTime.setText(hourOfDay + ":" + minute);

                                dates.get(position).setHours(hourOfDay);
                                dates.get(position).setMinutes(minute);
                            }
                        },hours,minutes,false);
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.show();
            }
        });
    }

    private void sendEvent(EditText eventMessage, int position) {
        Event event = new Event(eventMessage.getText().toString().trim(),Utils.dateToString(dates.get(position)));
        DatabaseHelper.addEvent(Utils.getCurrentUserToken(context), event);

        Calendar eventDateCalendar = Calendar.getInstance();
        //send notification in event time, one our and one day before

        Notification notification = new Notification(getResources().getString(R.string.upcomingEvent),event.getMessage() + " " + event.getDate(),null);
        notification.setDate(Utils.dateToString(eventDateCalendar.getTime()));

        eventDateCalendar.setTime(dates.get(position));
        Utils.setAlarmForNotification(context,eventDateCalendar,notification);

        eventDateCalendar.add(Calendar.DATE,-1);
        Utils.setAlarmForNotification(context,eventDateCalendar,notification);

        eventDateCalendar.add(Calendar.DATE,1);
        eventDateCalendar.add(Calendar.HOUR,-1);
        Utils.setAlarmForNotification(context,eventDateCalendar,notification);
    }

    private void createEventList(RecyclerView listView, Date date) {
        List<Event> eventList = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        EventListAdapter eventListAdapter = new EventListAdapter(eventList);
        listView.setAdapter(eventListAdapter);

        getEventForSpecificDate(eventList, Utils.getZeroTimeDate(date));
    }

    private void getEventForSpecificDate(final List<Event> eventList, final Date date) {
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserToken(getContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()) {
                    Event event = eventData.getValue(Event.class);
                    try {
                        Date eventDate = Utils.stringToDate(event.getDate());
                        if (Utils.getZeroTimeDate(eventDate).equals(date)) {
                            eventList.add(event);
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

    private void getEvents(final FirebaseCallback callback){
        loadingDialog.show();
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserToken(context)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event event = eventData.getValue(Event.class);
                    eventsList.add(event);
                }
                callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupCalendar(){
        dates.clear();

        //calculate dates for calendar
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);

        int firstDay = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
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
