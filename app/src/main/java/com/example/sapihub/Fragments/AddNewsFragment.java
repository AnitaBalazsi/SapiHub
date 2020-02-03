package com.example.sapihub.Fragments;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sapihub.Helpers.DatabaseHelper;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewsFragment extends Fragment implements View.OnClickListener {
    private EditText title, content;
    private TextView imageName;
    private Uri imagePath;
    private Button addImageButton, sendButton;


    public AddNewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeVariables();
    }

    private void initializeVariables() {
        title = getView().findViewById(R.id.newsTitle);
        content = getView().findViewById(R.id.newsContent);
        addImageButton = getView().findViewById(R.id.addImageButton);
        sendButton = getView().findViewById(R.id.sendButton);
        imageName = getView().findViewById(R.id.imageName);

        addImageButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addImageButton:
                getImageFromGallery();
                break;
            case R.id.sendButton:
                sendData();
                break;
        }
    }

    private void sendData() {
        if (validateInputs()){
            String newsTitle = title.getText().toString().trim();
            String newsContent = content.getText().toString().trim();
            String image = imageName.getText().toString().trim();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (!image.isEmpty()){
                DatabaseHelper.uploadImage(image,imagePath);
                DatabaseHelper.addNews(new News(newsTitle,date,newsContent,image));
            } else {
                DatabaseHelper.addNews(new News(newsTitle,date,newsContent,null));
            }
        }

        //load previous fragment
        getFragmentManager().popBackStack();
    }

    private boolean validateInputs() {
        if (title.getText().toString().isEmpty()){
            title.setError(getString(R.string.emptyField));
            title.requestFocus();
            return false;
        }
        if (content.getText().toString().isEmpty()){
            content.setError(getString(R.string.emptyField));
            content.requestFocus();
            return false;
        }
        return true;
    }


    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            imagePath = data.getData();

            //gets name of image
            Cursor cursor = getActivity().getContentResolver().query(imagePath, null, null, null, null);
            cursor.moveToFirst();
            imageName.setText(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
        }
    }

}
