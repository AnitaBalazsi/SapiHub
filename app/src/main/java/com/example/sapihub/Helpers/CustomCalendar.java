package com.example.sapihub.Helpers;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final View dialogView = inflater.inflate(R.layout.add_event_layout,null);
                builder.setView(dialogView);

                final TextView selectedDate = dialogView.findViewById(R.id.selectedDate);
                selectedDate.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(dates.get(position)));

                Button sendButton = dialogView.findViewById(R.id.sendButton);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);
                final EditText eventMessage = dialogView.findViewById(R.id.eventMessage);
                ImageView timePicker = dialogView.findViewById(R.id.timePicker);
                final TextView selectedTime = dialogView.findViewById(R.id.selectedTime);

                RecyclerView listView = dialogView.findViewById(R.id.eventList);
                createEventList(listView, dates.get(position));

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
                        //add event to database and set alarm for notification
                       Event event = new Event(eventMessage.getText().toString().trim(),Utils.dateToString(dates.get(position)));
                       DatabaseHelper.addEvent(Utils.getCurrentUserName(context), event);

                       Calendar eventDateCalendar = Calendar.getInstance();

                       //send notification in event time, one our and one day before
                        Notification notification = new Notification(getResources().getString(R.string.upcomingEvent),event.getMessage() + " " + event.getDate(),null);
                       eventDateCalendar.setTime(dates.get(position));
                       setAlarmForNotification(eventDateCalendar,notification);

                       eventDateCalendar.add(Calendar.DATE,-1);
                       setAlarmForNotification(eventDateCalendar,notification);

                       eventDateCalendar.add(Calendar.DATE,1);
                       eventDateCalendar.add(Calendar.HOUR,-1);
                       setAlarmForNotification(eventDateCalendar,notification);

                       addEventDialog.dismiss();
                    }
                });

                timePicker.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar calendar = Calendar.getInstance();
                        final int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minutes = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(dialogView.getContext(), R.style.Theme_AppCompat_Dialog,
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
                        timePickerDialog.show();
                    }
                });
            }
        });
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
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserName(getContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event event = eventData.getValue(Event.class);
                    try {
                        Date eventDate = Utils.stringToDate(event.getDate());
                        if (Utils.getZeroTimeDate(eventDate).equals(date)){
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

    private void setAlarmForNotification(Calendar calendar, Notification notification) {
        notification.setDate(Utils.dateToString(calendar.getTime()));

        Intent intent = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent.putExtra("notificationTitle",notification.getTitle());
        intent.putExtra("notificationMessage",notification.getMessage());
        intent.putExtra("notificationDate",notification.getDate());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

    }

    private void getEvents(final FirebaseCallback callback){
        loadingDialog.show();
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserName(context)).addValueEventListener(new ValueEventListener() {
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
