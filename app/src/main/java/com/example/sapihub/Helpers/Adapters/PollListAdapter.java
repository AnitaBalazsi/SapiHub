package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Poll;
import com.example.sapihub.R;

import java.util.List;

public class PollListAdapter extends RecyclerView.Adapter<PollListAdapter.ListViewHolder> {
    private Context context;
    private List<Poll> pollList;
    private ListViewHolder.PollClickListener pollClickListener;
    private String TAG;
    private News news;

    public PollListAdapter(Context context, List<Poll> pollList,String TAG, News news) {
        this.context = context;
        this.pollList = pollList;
        this.TAG = TAG;
        this.news = news;

    }

    public PollListAdapter(Context context, List<Poll> pollList, ListViewHolder.PollClickListener pollClickListener, String TAG) {
        this.context = context;
        this.pollList = pollList;
        this.pollClickListener = pollClickListener;
        this.TAG = TAG;
    }

    @NonNull
    @Override
    public PollListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.poll_list_item, parent, false);
        return new PollListAdapter.ListViewHolder(listItem,pollClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final PollListAdapter.ListViewHolder holder, final int position) {
        holder.options.setLayoutManager(new LinearLayoutManager(context));
        holder.options.setHasFixedSize(true);
        holder.options.setAdapter(new PollOptionsAdapter(context, pollList.get(position).getAnswers(), new PollOptionsAdapter.ListViewHolder.PollOptionListener() {
            @Override
            public void onDeleteOption(int position) {

            }

            @Override
            public void onOptionChange(int position, String s) {

            }

            @Override
            public void onOptionSelected(int option) {
                if (TAG.equals(Utils.VIEW_POLL)){
                    String userId = Utils.getCurrentUserToken(context);
                    if (pollList.get(position).getAnswers().get(option).getUsers().contains(userId)){
                        pollList.get(position).getAnswers().get(option).removeUserAnswer(userId);
                    } else {
                        pollList.get(position).getAnswers().get(option).addUserAnswer(userId);
                    }

                    DatabaseHelper.getNewsId(news, new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            DatabaseHelper.modifyNews((String) object,news);
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }, TAG));
        if (TAG.equals(Utils.VIEW_POLL)){
            holder.moreImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pollList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView options;
        private ImageView moreImage;

        public ListViewHolder(@NonNull final View itemView, final PollClickListener pollClickListener) {
            super(itemView);

            options = itemView.findViewById(R.id.options);
            moreImage = itemView.findViewById(R.id.moreImage);
            moreImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pollClickListener.onMoreOptionsClick(itemView,getAdapterPosition());
                }
            });
        }

        public interface PollClickListener{
            void onMoreOptionsClick(View itemView, int adapterPosition);
        }
    }
}
