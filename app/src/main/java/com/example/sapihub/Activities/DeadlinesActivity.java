package com.example.sapihub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.sapihub.Helpers.Adapters.EventListAdapter;
import com.example.sapihub.Helpers.AlarmReceiver;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Event;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DeadlinesActivity extends AppCompatActivity implements OnDayClickListener, EventListAdapter.ListViewHolder.EventClickListener {
    private List<EventDay> eventDayList = new ArrayList<>();
    private CalendarView calendarView;
    private Date eventDate;
    private RecyclerView recyclerView;
    private EventListAdapter eventListAdapter;
    private List<Event> eventList = new ArrayList<>();
    private AlertDialog dialog;
    private Intent receiverIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadlines);

        initializeVariables();
        getData(null);
    }

    private void getData(final Date date) {
        DatabaseHelper.eventsReference.child(Utils.getCurrentUserToken(this)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventDayList.clear();
                eventList.clear();
                for (DataSnapshot eventData : dataSnapshot.getChildren()){
                    Event event = eventData.getValue(Event.class);

                    //shows only events that are not over yet
                    Date eventDate = null;
                    try {
                        eventDate = Utils.stringToDate(event.getDate());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(eventDate);
                        eventDayList.add(new EventDay(calendar,R.drawable.ic_event));
                        calendarView.setEvents(eventDayList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (date != null){
                        Calendar calendarSelected = Calendar.getInstance();
                        calendarSelected.setTime(date);
                        Calendar calendarEvent = Calendar.getInstance();
                        calendarEvent.setTime(eventDate);

                        if (calendarSelected.get(Calendar.YEAR) == calendarEvent.get(Calendar.YEAR)
                                && calendarSelected.get(Calendar.MONTH) == calendarEvent.get(Calendar.MONTH)
                                && calendarSelected.get(Calendar.DAY_OF_MONTH) == calendarEvent.get(Calendar.DAY_OF_MONTH)){
                            eventList.add(event);
                            eventListAdapter.notifyDataSetChanged();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DeadlinesActivity",databaseError.getMessage());
            }
        });
    }

    private void initializeVariables() {
        calendarView = findViewById(R.id.calendar);

        Calendar calendar = Calendar.getInstance();
        calendarView.setDate(calendar.getTime());
        calendarView.setOnDayClickListener(this);

        eventListAdapter = new EventListAdapter(eventList,this);
        receiverIntent = new Intent(this, AlarmReceiver.class);
    }

    @Override
    public void onDayClick(EventDay eventDay) {
        showDialog(eventDay);
    }

    private void showDialog(final EventDay eventDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
        final View dialogView = this.getLayoutInflater().inflate(R.layout.add_event_layout,null);
        builder.setView(dialogView);
        dialog = builder.create();

        final TextView selectedDate = dialogView.findViewById(R.id.selectedDate);
        selectedDate.setText(Utils.dateToString(eventDay.getCalendar().getTime()));
        eventDate = eventDay.getCalendar().getTime();

        getData(eventDate);
        recyclerView = dialogView.findViewById(R.id.eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventListAdapter);

        final EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        TextView sendEvent = dialogView.findViewById(R.id.sendEvent);
        ImageView addTime = dialogView.findViewById(R.id.eventTimePicker);
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                final int hours = calendar.get(Calendar.HOUR_OF_DAY);
                final int minutes = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(dialogView.getContext(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.setTime(eventDay.getCalendar().getTime());
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.setTimeZone(TimeZone.getDefault());

                                selectedDate.setText(Utils.dateToString(calendar.getTime()));
                                eventDate = calendar.getTime();
                            }
                        },hours,minutes,false);
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.show();
            }
        });

        sendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventTitle.getText().toString().isEmpty()){
                    eventTitle.setError(getResources().getString(R.string.emptyField));
                    eventTitle.requestFocus();
                } else {
                    sendEvent(eventTitle,eventDate);
                    eventDayList.add(new EventDay(eventDay.getCalendar(), R.drawable.ic_account_circle, R.color.colorGreen));
                    calendarView.setEvents(eventDayList);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void sendEvent(EditText eventTitle, Date eventDate) {
        Event event = new Event(eventTitle.getText().toString().trim(),Utils.dateToString(eventDate));
        DatabaseHelper.addEvent(Utils.getCurrentUserToken(this), event);
        eventListAdapter.notifyDataSetChanged();

        sendNotification(event);
    }

    private void sendNotification(Event event) {
        Calendar calendar = Calendar.getInstance();

        //one hour before
        calendar.setTime(eventDate);
        calendar.add(Calendar.HOUR,-1);
        setAlarm(event, calendar);

        //one day before
        calendar.setTime(eventDate);
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        setAlarm(event, calendar);

        //5 minutes before
        calendar.setTime(eventDate);
        calendar.add(Calendar.MINUTE,-5);
        setAlarm(event, calendar);
    }

    private void setAlarm(Event event, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        receiverIntent.putExtra("user",Utils.getCurrentUserToken(this));
        receiverIntent.putExtra("event",event.getMessage());
        receiverIntent.putExtra("date",Utils.dateToString(calendar.getTime()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) calendar.getTimeInMillis(),receiverIntent,0);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
        }
    }

    @Override
    public void onDeleteEvent(final int position) {
        DatabaseHelper.deleteEvent(Utils.getCurrentUserToken(this), eventList.get(position));
        dialog.dismiss();
        deleteNotificationSchedule();
        getData(null);
    }

    private void deleteNotificationSchedule() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(eventDate);
        calendar.add(Calendar.HOUR,-1);
        deleteAlarm(calendar.getTimeInMillis());

        calendar.setTime(eventDate);
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        deleteAlarm(calendar.getTimeInMillis());

        calendar.setTime(eventDate);
        calendar.add(Calendar.MINUTE,-5);
        deleteAlarm(calendar.getTimeInMillis());
    }

    private void deleteAlarm(long time){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) time,receiverIntent,0);
        alarmManager.cancel(pendingIntent);
    }

}
