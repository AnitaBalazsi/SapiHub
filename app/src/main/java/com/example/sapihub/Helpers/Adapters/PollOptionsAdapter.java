package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.PollAnswer;
import com.example.sapihub.R;

import java.util.List;

public class PollOptionsAdapter extends RecyclerView.Adapter<PollOptionsAdapter.ListViewHolder> {
    private Context context;
    private List<PollAnswer> answerList;
    private ListViewHolder.PollOptionListener pollOptionListener;
    private String TAG;

    public PollOptionsAdapter(Context context, List<PollAnswer> answerList, ListViewHolder.PollOptionListener pollOptionListener, String TAG) {
        this.context = context;
        this.answerList = answerList;
        this.pollOptionListener = pollOptionListener;
        this.TAG = TAG;
    }

    @NonNull
    @Override
    public PollOptionsAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.poll_option, parent, false);
        return new ListViewHolder(listItem,pollOptionListener,TAG);
    }

    @Override
    public void onBindViewHolder(@NonNull PollOptionsAdapter.ListViewHolder holder, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,5,0,0);
        switch (TAG){
            case Utils.ADD_POST:
                holder.deleteOption.setVisibility(View.GONE);
                holder.container.setLayoutParams(params);
                break;
            case Utils.VIEW_POLL:
                holder.deleteOption.setVisibility(View.GONE);
                holder.percentageBar.setVisibility(View.VISIBLE);
                holder.optionText.setInputType(InputType.TYPE_NULL);
                holder.radioButton.setVisibility(View.VISIBLE);
                holder.userLayout.setVisibility(View.VISIBLE);
                break;
            case Utils.ADD_POLL:
                holder.deleteOption.setVisibility(View.VISIBLE);
                holder.container.setLayoutParams(params);
                break;
        }

        if (answerList.get(position).getOption() != context.getResources().getString(R.string.pollOption)){
            holder.optionText.setText(answerList.get(position).getOption());
        }

        if (answerList.get(position).getUsers().contains(Utils.getCurrentUserToken(context))){
            holder.radioButton.setChecked(true);
        } else {
            holder.radioButton.setChecked(false);
        }

        if (answerList.get(position).getUsers().size() > 0){
            loadUserImages(holder.userLayout, answerList.get(position).getUsers());
        }
        holder.percentageBar.getLayoutParams().width = calculatePercentage(answerList.get(position).getUsers()) * 10;
    }

    private int calculatePercentage(List<String> users) {
        int sum = 0;
        for (PollAnswer pollAnswer : answerList){
            sum += pollAnswer.getUsers().size();
        }

        if (sum == 0){
            return 0;
        }
        return ((users.size() * 100) / sum);
    }

    private void loadUserImages(LinearLayout userLayout, List<String> users) {
        int size = 3;
        if (users.size() < 3){
            size = users.size();
        }

        for (int i = 0; i < size; i++){
            ImageView imageView = new ImageView(context);
            userLayout.addView(imageView);
            Utils.loadProfilePicture(context,imageView,users.get(i),50,50);
        }

        if (users.size() > 3){
            TextView counter = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,5,0,0);
            counter.setLayoutParams(params);
            counter.setText("+".concat(String.valueOf(users.size() - 3)));
            userLayout.addView(counter);
        }
    }

    @Override
    public int getItemCount() {
        return answerList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private EditText optionText;
        private ImageView deleteOption;
        private View percentageBar;
        private LinearLayout userLayout;
        private RadioButton radioButton;
        private LinearLayout container;

        public ListViewHolder(@NonNull View itemView, final PollOptionListener pollOptionListener, final String TAG) {
            super(itemView);
            optionText = itemView.findViewById(R.id.optionText);
            deleteOption = itemView.findViewById(R.id.deleteOption);
            percentageBar = itemView.findViewById(R.id.percentageBar);
            radioButton = itemView.findViewById(R.id.radioButton);
            userLayout = itemView.findViewById(R.id.users);
            container = itemView.findViewById(R.id.container);

            deleteOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pollOptionListener.onDeleteOption(getAdapterPosition());
                }
            });

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TAG.equals(Utils.VIEW_POLL)){
                        pollOptionListener.onOptionSelected(getAdapterPosition());
                    }
                }
            });

            optionText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TAG.equals("ADD_POLL")){
                        pollOptionListener.onOptionChange(getAdapterPosition(),s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        public interface PollOptionListener{
            void onDeleteOption(int position);
            void onOptionChange(int position, String s);
            void onOptionSelected(int position);
        }
    }
}
