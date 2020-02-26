package com.example.sapihub.Helpers.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Model.Event;
import com.example.sapihub.R;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ListViewHolder> {
    private List<Event> eventList;

    public EventListAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.event_list_item, parent, false);
        return new ListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.eventMessage.setText(eventList.get(position).getMessage());
        holder.eventDate.setText(eventList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout deleteEvent;
        TextView eventMessage, eventDate;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            eventMessage = itemView.findViewById(R.id.message);
            eventDate = itemView.findViewById(R.id.date);
            deleteEvent = itemView.findViewById(R.id.deleteEvent);
        }
    }
}
