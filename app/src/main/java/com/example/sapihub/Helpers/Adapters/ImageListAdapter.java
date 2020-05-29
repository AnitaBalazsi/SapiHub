package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.R;

import java.io.IOException;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ListViewHolder> {
    private List<Uri> images;
    private Context context;
    private ListViewHolder.ImageClickListener imageClickListener;


    public ImageListAdapter(List<Uri> images, Context context, ListViewHolder.ImageClickListener imageClickListener) {
        this.images = images;
        this.context = context;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.image_list_item, parent, false);
        return new ImageListAdapter.ListViewHolder(listItem,imageClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Glide.with(context).load(images.get(position).toString())
                    .apply(new RequestOptions().override(500, 300))
                    .centerCrop()
                    .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image, deleteImage;
        private ImageClickListener imageClickListener;

        public ListViewHolder(@NonNull View itemView, ImageClickListener imageClickListener) {
            super(itemView);

            this.imageClickListener = imageClickListener;
            image = itemView.findViewById(R.id.image);
            deleteImage = itemView.findViewById(R.id.deleteImage);
            deleteImage.setOnClickListener(this);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.deleteImage:
                    imageClickListener.onDeleteImage(getAdapterPosition());
                    break;
                case R.id.image:
                    imageClickListener.onViewImage(getAdapterPosition());
                    break;
            }

        }

        public interface ImageClickListener{
            void onDeleteImage(int position);
            void onViewImage(int position);
        }
    }
}
