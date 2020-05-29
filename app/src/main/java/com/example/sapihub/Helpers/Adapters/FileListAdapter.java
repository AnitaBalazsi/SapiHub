package com.example.sapihub.Helpers.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.R;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ListViewHolder> {
    private List<Uri> files;
    private Context context;
    private String TAG;
    private ListViewHolder.FileClickListener fileClickListener;

    public FileListAdapter(List<Uri> files, Context context, String TAG, ListViewHolder.FileClickListener fileClickListener) {
        this.files = files;
        this.TAG = TAG;
        this.context = context;
        this.fileClickListener = fileClickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.file_list_item, parent, false);
        return new ListViewHolder(listItem,fileClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.fileName.setText(Utils.fileNameFromUri(context,files.get(position)));
        if (TAG != null && TAG.equals(Utils.ADD_POST)){
            holder.deleteFile.setVisibility(View.VISIBLE);
        } else {
            holder.deleteFile.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView fileName;
        private ImageView deleteFile;

        public ListViewHolder(@NonNull View itemView, final FileClickListener fileClickListener) {
            super(itemView);

            fileName = itemView.findViewById(R.id.fileName);
            deleteFile = itemView.findViewById(R.id.deleteFile);
            deleteFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileClickListener.onDeleteFile(getAdapterPosition());
                }
            });
            fileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileClickListener.onFileClick(getAdapterPosition());
                }
            });

        }

        public interface FileClickListener{
            void onDeleteFile(int position);
            void onFileClick(int position);
        }
    }


}
