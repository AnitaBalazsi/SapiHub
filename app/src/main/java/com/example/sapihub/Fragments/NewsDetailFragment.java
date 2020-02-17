package com.example.sapihub.Fragments;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsDetailFragment extends Fragment {
    private News selectedNews;

    public NewsDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


       selectedNews = (News) getArguments().getSerializable("selectedNews");
       loadData();
       loadImage();
    }

    private void loadData() {
        TextView title = getView().findViewById(R.id.title);
        TextView content = getView().findViewById(R.id.content);

        title.setText(selectedNews.getTitle());
        content.setText(selectedNews.getContent());

    }

    private void loadImage() {
        //if there is an image attached
        final ImageView imageView = getView().findViewById(R.id.image);
        if (selectedNews.getImageName() != null){
            DatabaseHelper.storageReference.child(selectedNews.getImageName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getContext()).load(uri.toString())//.apply(new RequestOptions().placeholder(R.drawable.image_placeholder))
                            .apply(new RequestOptions().override(1000, 600))
                            .into(imageView);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_detail, container, false);

    }

}
