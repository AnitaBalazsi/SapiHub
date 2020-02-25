package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sapihub.Helpers.Adapters.ImageListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNewsActivity extends AppCompatActivity implements View.OnClickListener, ImageListAdapter.ListViewHolder.ImageClickListener {
    private EditText title, content;
    private Button addImageButton, sendButton;
    private List<Uri> imageList = new ArrayList<>();
    private RecyclerView imageListView;
    private ImageListAdapter imageListAdapter;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);
        initializeVariables();
    }

    private void initializeVariables() {
        title = findViewById(R.id.newsTitle);
        content = findViewById(R.id.newsContent);
        addImageButton = findViewById(R.id.addImageButton);
        sendButton = findViewById(R.id.sendButton);
        imageListView = findViewById(R.id.imageList);
        imageListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));

        imageListAdapter = new ImageListAdapter(imageList, this, this);
        imageListView.setAdapter(imageListAdapter);

        addImageButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        loadingDialog = new ProgressDialog(this, R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.uploading));
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
            loadingDialog.show();
            String newsTitle = title.getText().toString().trim();
            String newsContent = content.getText().toString().trim();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (imageListAdapter.getItemCount() > 0){
                List<String> images = new ArrayList<>();
                DatabaseHelper.addNews(new News(newsTitle, date, newsContent, Utils.getCurrentUserToken(this), images));
                for (Uri imageUri : imageList){
                    images.add(Utils.imageNameFromUri(this,imageUri));
                    DatabaseHelper.uploadNewsImage(this, newsTitle, date, imageUri, new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            if ((Boolean) object){
                                loadingDialog.dismiss();
                                finish();
                            }
                        }
                    });
                }
            } else {
                DatabaseHelper.addNews(new News(newsTitle, date, newsContent, Utils.getCurrentUserToken(this), null));
                finish();
            }
        }
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
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoPickerIntent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            if (data.getClipData() != null){ //multiple images are selected
                int totalImages = data.getClipData().getItemCount();
                for (int i = 0; i < totalImages; ++i){
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageList.add(imageUri);
                    imageListAdapter.notifyDataSetChanged();
                }

            } else if (data.getData() != null) { //one image is selected
                imageList.add(data.getData());
                imageListAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onDeleteImage(int position) {
        imageList.remove(imageList.get(position));
        imageListAdapter.notifyDataSetChanged();
    }
}
