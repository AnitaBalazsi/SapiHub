package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ListViewHolder> {
    private Context context;
    private List<User> userList;
    private UserClickListener clickListener;

    public UserListAdapter(Context context, List<User> userList, UserClickListener clickListener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.user_list_item, parent, false);
        return new ListViewHolder(listItem,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Utils.loadProfilePicture(context,holder.profilePicture,userList.get(position).getToken(),100,100);
        holder.username.setText(userList.get(position).getName());
        if (userList.get(position).getDegree() != null){
            holder.degree.setText(userList.get(position).getDegree());
        } else {
            holder.degree.setText(userList.get(position).getDepartment());
        }
        if (userList.get(position).getStatus().equals(context.getString(R.string.online))){
            holder.onlineIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private ImageView profilePicture, onlineIcon;
        private TextView username, degree;

        public ListViewHolder(@NonNull View itemView, final UserClickListener clickListener) {
            super(itemView);

            layout = itemView.findViewById(R.id.linearLayout);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            onlineIcon = itemView.findViewById(R.id.onlineIcon);
            username = itemView.findViewById(R.id.userName);
            degree = itemView.findViewById(R.id.userDegree);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onUserClick(getAdapterPosition());
                }
            });
        }
    }

    public interface UserClickListener{
        void onUserClick(int position);
    }
}
