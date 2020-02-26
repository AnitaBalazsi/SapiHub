package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sapihub.Model.Event;
import com.example.sapihub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GridAdapter extends ArrayAdapter {
    private List<Date> monthlyDates;
    private Calendar selectedDate;
    private List<Event> eventsList;
    private LayoutInflater inflater;

    public GridAdapter(@NonNull Context context, List<Date> dateList, Calendar selectedDate, List<Event> eventsList) {
        super(context, R.layout.calendar_cell_layout);

        this.monthlyDates = dateList;
        this.selectedDate = selectedDate;
        this.eventsList = eventsList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.calendar_cell_layout,parent,false);
        }

        //display days
        TextView dayCell = convertView.findViewById(R.id.day);
        Date mDate = monthlyDates.get(position);

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH)+1;
        int currentYear = calendar.get(Calendar.YEAR);

        calendar.setTime(mDate);
        int displayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int displayMonth = calendar.get(Calendar.MONTH)+1;
        int displayYear = calendar.get(Calendar.YEAR);

        dayCell.setText(String.valueOf(displayDay));

        //highlight current day
        if (displayDay == currentDay && displayMonth == currentMonth && displayYear == currentYear){
            dayCell.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
            dayCell.setTypeface(null,Typeface.BOLD);
        }

        //hide days from other months
        int selectedMonth = selectedDate.get(Calendar.MONTH)+1;
        if (displayMonth != selectedMonth){
            dayCell.setTextColor(ContextCompat.getColor(getContext(),R.color.colorLightGray));
        }

        //display events
        Calendar eventCalendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd.");
        for (Event event : eventsList){
            try {
                Date eventDate = format.parse(event.getDate());
                eventCalendar.setTime(eventDate);

                int eventDay = eventCalendar.get(Calendar.DAY_OF_MONTH);
                int eventMonth = eventCalendar.get(Calendar.MONTH)+1;
                int eventYear = eventCalendar.get(Calendar.YEAR);
                if (displayDay == eventDay && displayMonth == eventMonth && displayYear == eventYear){
                    dayCell.setTextColor(ContextCompat.getColor(getContext(),R.color.colorRed));
                    dayCell.setTypeface(null, Typeface.BOLD);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return monthlyDates.size();
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return monthlyDates.indexOf(item);
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return monthlyDates.get(position);
    }
}
