package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        try {
            holder.image.setImageBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), images.get(position)));

        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.imageName.setText(Utils.imageNameFromUri(context,images.get(position)));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image, deleteImage;
        private TextView imageName;
        private ImageClickListener imageClickListener;

        public ListViewHolder(@NonNull View itemView, ImageClickListener imageClickListener) {
            super(itemView);

            this.imageClickListener = imageClickListener;
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.imageName);
            deleteImage = itemView.findViewById(R.id.deleteImage);
            deleteImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            imageClickListener.onDeleteImage(getAdapterPosition());
        }

        public interface ImageClickListener{
            void onDeleteImage(int position);
        }
    }
}
