package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class PopUpDialogListAdapter extends RecyclerView.Adapter<PopUpDialogListAdapter.ListViewHolder> {
    private Context context;
    private List<User> userList;
    private UserClickListener clickListener;

    public PopUpDialogListAdapter(Context context, List<User> userList, UserClickListener clickListener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.popup_users, parent, false);
        return new ListViewHolder(listItem,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Utils.loadProfilePicture(context,holder.profilePicture,userList.get(position).getToken(),100,100);
        holder.username.setText(userList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout container;
        private ImageView profilePicture;
        private TextView username;

        public ListViewHolder(@NonNull View itemView, final UserClickListener clickListener) {
            super(itemView);

            container = itemView.findViewById(R.id.container);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            username = itemView.findViewById(R.id.userName);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onUserClick(getAdapterPosition());
                }
            });
        }
    }

    public interface UserClickListener {
        void onUserClick(int position);
    }
}
