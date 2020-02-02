package com.example.sapihub.Helpers;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ListViewHolder> {
    private List<News> newsList;
    private Context context;


    public NewsListAdapter(List<News> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.news_list_item, parent, false);
        return new ListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.title.setText(newsList.get(position).getTitle());

        String content = newsList.get(position).getContent();
        if (content.length() > 100){
            holder.content.setText(content.substring(0,100).concat("..."));
        } else {
            holder.content.setText(content);
        }
        loadImage(newsList.get(position).getImageName(), holder.image);
    }

    private void loadImage(String imageName, final ImageView imageView) {
        //if there is an image attached
        if (imageName != null){
            DatabaseHelper.storageReference.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri.toString())//.apply(new RequestOptions().placeholder(R.drawable.image_placeholder))
                            .apply(new RequestOptions().override(1000, 600))
                            .into(imageView);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, content;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            this.image = itemView.findViewById(R.id.image);
            this.title = itemView.findViewById(R.id.title);
            this.content = itemView.findViewById(R.id.content);

        }
    }
}
