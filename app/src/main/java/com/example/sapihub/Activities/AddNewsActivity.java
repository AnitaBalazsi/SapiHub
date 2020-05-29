package com.example.sapihub.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Helpers.Adapters.FileListAdapter;
import com.example.sapihub.Helpers.Adapters.ImageListAdapter;
import com.example.sapihub.Helpers.Adapters.PollListAdapter;
import com.example.sapihub.Helpers.Adapters.PollOptionsAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.Poll;
import com.example.sapihub.Model.PollAnswer;
import com.example.sapihub.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNewsActivity extends AppCompatActivity implements View.OnClickListener, ImageListAdapter.ListViewHolder.ImageClickListener, FileListAdapter.ListViewHolder.FileClickListener, PollListAdapter.ListViewHolder.PollClickListener {
    private EditText title, content;
    private Button sendButton;
    private ImageView imageFromCamera, imageFromGallery, backImage;
    private LinearLayout createPoll, attachFile, captionContainer;
    private List<Uri> imageList = new ArrayList<>();
    private List<Uri> fileList = new ArrayList<>();
    private RecyclerView imageListView, fileListView, pollListView;
    private ProgressDialog loadingDialog;
    private News selectedNews;
    private String newsId;
    private Uri cameraImageUri;
    private List<Poll> polls = new ArrayList<>();
    private List<String> captionList = new ArrayList<>();

    private final int GALLERY_RESULT = 1;
    private final int CAMERA_RESULT = 2;
    private final int FILE_RESULT = 3;
    private final int FILE_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        initializeVariables();
        loadCaptions();

        if (getIntent().hasExtra("selectedPost")){
            selectedNews = (News) getIntent().getSerializableExtra("selectedPost");
            loadSelectedPost(selectedNews);
        }
    }

    private void loadSelectedPost(News news) {
        title.setText(news.getTitle());
        content.setText(news.getContent());

        if (news.getPolls() != null){
            polls.addAll(news.getPolls());
            pollListView.setVisibility(View.VISIBLE);
            pollListView.getAdapter().notifyDataSetChanged();
        }
    }

    private void initializeVariables() {
        title = findViewById(R.id.newsTitle);
        content = findViewById(R.id.newsContent);
        imageFromCamera = findViewById(R.id.addImageFromCamera);
        imageFromGallery = findViewById(R.id.addImageFromGallery);
        attachFile = findViewById(R.id.attachFile);
        backImage = findViewById(R.id.previousPage);
        createPoll = findViewById(R.id.createPoll);
        sendButton = findViewById(R.id.sendButton);
        imageListView = findViewById(R.id.imageList);
        fileListView = findViewById(R.id.fileAttachments);
        pollListView = findViewById(R.id.pollList);
        captionContainer = findViewById(R.id.captionContainer);

        imageListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        imageListView.setHasFixedSize(true);
        imageListView.setAdapter(new ImageListAdapter(imageList, this, this));

        fileListView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        fileListView.setAdapter(new FileListAdapter(fileList,this,Utils.ADD_POST,this));

        pollListView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        pollListView.setHasFixedSize(true);
        pollListView.setAdapter(new PollListAdapter(this,polls,this,Utils.ADD_POST));

        imageFromCamera.setOnClickListener(this);
        imageFromGallery.setOnClickListener(this);
        attachFile.setOnClickListener(this);
        backImage.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        createPoll.setOnClickListener(this);

        loadingDialog = new ProgressDialog(this, R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.uploading));
    }

    private void loadCaptions() {
        final List<String> captions = new ArrayList<>();
        captions.add(getString(R.string.publicCaption));
        captions.addAll(Arrays.asList(getResources().getStringArray(R.array.degreeList)));
        captions.addAll(Arrays.asList(getResources().getStringArray(R.array.departmentList)));

        for (final String caption : captions){
            final TextView textView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,5,10,0);
            textView.setLayoutParams(params);
            textView.setPadding(20,20,20,20);
            textView.setBackground(getDrawable(R.drawable.caption_background_white));
            textView.setText(caption);
            captionContainer.addView(textView);

            if (caption.equals(getString(R.string.publicCaption))){
                captionList.add(caption);
                textView.setTextColor(getResources().getColor(R.color.colorWhite));
                textView.setBackground(getDrawable(R.drawable.caption_background_green));
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (captionList.contains(caption)){
                        captionList.remove(caption);
                        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        textView.setBackground(getDrawable(R.drawable.caption_background_white));
                    } else {
                        captionList.add(caption);
                        textView.setTextColor(getResources().getColor(R.color.colorWhite));
                        textView.setBackground(getDrawable(R.drawable.caption_background_green));
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        exitPage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addImageFromCamera:
                getImageFromCamera();
                break;
            case R.id.addImageFromGallery:
                getImageFromGallery();
                break;
            case R.id.sendButton:
                if (selectedNews != null){
                    modifyPost();
                } else {
                    sendData();
                }
                break;
            case R.id.previousPage:
                exitPage();
                break;
            case R.id.createPoll:
                showCreatePollDialog(null);
                break;
            case R.id.attachFile:
                attachFile();
                break;
        }
    }

    private void exitPage() {
        if (title.getText().length() > 0 || content.getText().length() > 0 || imageList.size() > 0 || fileList.size() > 0){
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setMessage(R.string.exitAddNews)
                    .setPositiveButton(R.string.discardChanges, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            finish();
        }
    }

    private void attachFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
            filePicker.setType("application/*");
            startActivityForResult(filePicker, FILE_RESULT);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},FILE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FILE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            attachFile();
        }
    }

    private void showCreatePollDialog(Poll poll) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewsActivity.this,R.style.AlertDialogTheme);
        final View dialogView = this.getLayoutInflater().inflate(R.layout.poll_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.options);
        TextView addOption = dialogView.findViewById(R.id.addOption);

        final List<PollAnswer> options = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PollOptionsAdapter(this, options, new PollOptionsAdapter.ListViewHolder.PollOptionListener() {
            @Override
            public void onDeleteOption(int position) {
                options.remove(position);
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onOptionChange(int position, String s) {
                options.get(position).setOption(s);
            }

            @Override
            public void onOptionSelected(int position) {

            }
        }, Utils.ADD_POLL));

        if (poll == null){
            poll = new Poll(options);
        } else {
            //modify poll
            options.addAll(poll.getAnswers());
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options.add(new PollAnswer(getResources().getString(R.string.pollOption)));
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        final Poll finalPoll = poll;
        builder.setPositiveButton(getString(R.string.save), null);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { //prevents dialog automatically closing on save button click
            @Override
            public void onShow(final DialogInterface dialog) {
                Button saveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!validatePoll(options)){
                            Utils.showSnackbar(findViewById(R.id.addNewsContainer),getString(R.string.pollError),getColor(R.color.colorRed));
                        } else {
                            polls.remove(finalPoll); //when modified, prevents duplication

                            finalPoll.setAnswers(options);
                            polls.add(finalPoll);

                            pollListView.setVisibility(View.VISIBLE);
                            pollListView.getAdapter().notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private boolean validatePoll(List<PollAnswer> options) {
       if (options.size() < 2){//two options required
            return false;
        }

       for (PollAnswer answer : options){
           if (answer.getOption().isEmpty()){
               return false;
           }
       }
       return true;
    }

    private void modifyPost() {
        DatabaseHelper.getNewsId(selectedNews,new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                newsId = (String) object;
                sendData();
            }
        });
    }

    private void sendData() {
        if (validateInputs()){
            loadingDialog.show();
            final boolean[] isFinished = {false, false};
            String newsTitle = title.getText().toString().trim();
            String newsContent = content.getText().toString().trim();
            String date;
            List<String> images = new ArrayList<>();
            List<String> files = new ArrayList<>();

            //load selected news data (modify)
            if (selectedNews == null){
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            } else {
                date = selectedNews.getDate();
                if (selectedNews.getImages() != null){
                    images = selectedNews.getImages();
                }
                if (selectedNews.getFiles() != null){
                    files = selectedNews.getFiles();
                }
                if (selectedNews.getPolls() != null){
                    polls = selectedNews.getPolls();
                }
            }

            //upload files
            if (fileList.isEmpty()){isFinished[0] = true;}
            for (int i = 0; i < fileList.size(); i++){
                loadingDialog.show();
                Uri fileUri = fileList.get(i);
                files.add(Utils.fileNameFromUri(this,fileUri));
                final int finalI = i;
                DatabaseHelper.uploadNewsAttachment(this, newsTitle, date, fileUri, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        if ((Boolean) object && finalI == fileList.size() - 1){
                            loadingDialog.dismiss();
                            isFinished[0] = true;
                            if (isFinished[0] && isFinished[1]){
                                finish(); //if both file and image uploading is finished, back to prev. activity
                            }
                        }
                    }
                });
            }

            //upload images
            if (imageList.isEmpty()){isFinished[1] = true;}
            for (int i = 0; i < imageList.size(); i++){
                loadingDialog.show();
                Uri imageUri = imageList.get(i);
                images.add(Utils.fileNameFromUri(this,imageUri));
                final int finalI = i;
                DatabaseHelper.uploadNewsAttachment(this, newsTitle, date, imageUri, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        if ((Boolean) object && finalI == imageList.size() - 1){
                            loadingDialog.dismiss();
                            isFinished[1] = true;
                            if (isFinished[0] && isFinished[1]){
                                finish();
                            }
                        }
                    }
                });
            }

            if (newsId == null){
                DatabaseHelper.addNews(new News(newsTitle, date, newsContent, Utils.getCurrentUserToken(this), null,images,files,polls, captionList));
            } else {
                DatabaseHelper.modifyNews(newsId,new News(newsTitle, date, newsContent, Utils.getCurrentUserToken(this), null, images,files,polls, captionList));
            }

            if (isFinished[0] && isFinished[1]){
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
        if (captionList.size() < 1){//min one caption required
            Utils.showSnackbar(findViewById(R.id.addNewsContainer),getString(R.string.captionError),getColor(R.color.colorRed));
            return false;
        }

        return true;
    }

    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_RESULT);
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri);
        startActivityForResult(intent,CAMERA_RESULT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            switch (requestCode){
                case GALLERY_RESULT:
                    imageListView.setVisibility(View.VISIBLE);
                    displayImages(data);
                    break;
                case CAMERA_RESULT:
                    imageListView.setVisibility(View.VISIBLE);
                    imageList.add(cameraImageUri);
                    imageListView.getAdapter().notifyDataSetChanged();
                    break;
                case FILE_RESULT:
                    if (data != null){
                        Uri fileUri = data.getData();
                        fileList.add(fileUri);
                        fileListView.getAdapter().notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    private void displayImages(Intent data) {
        if (data.getClipData() != null){ //multiple images are selected
            int totalImages = data.getClipData().getItemCount();
            for (int i = 0; i < totalImages; ++i){
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                imageList.add(imageUri);
                imageListView.getAdapter().notifyDataSetChanged();
            }

        } else if (data.getData() != null) { //one image is selected
            imageList.add(data.getData());
            imageListView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteImage(int position) {
        imageList.remove(imageList.get(position));
        imageListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onViewImage(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        final ImageView imageView = new ImageView(this);

        Glide.with(this).load(imageList.get(position).toString())
                .apply(new RequestOptions().override(3500, 2000))
                .into(imageView);

        builder.setView(imageView);
        AlertDialog imageDialog = builder.create();
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        imageDialog.show();
    }

    @Override
    public void onDeleteFile(int position) {
        fileList.remove(position);
        fileListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onFileClick(int position) {

    }

    @Override
    public void onMoreOptionsClick(View itemView, final int adapterPosition) {
        PopupMenu popupMenu = new PopupMenu(this, itemView.findViewById(R.id.moreImage));
        this.getMenuInflater().inflate(R.menu.poll_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (String.valueOf(item.getTitle()).equals(getResources().getString(R.string.deletePoll))){
                    showConfirmDeleteDialog(adapterPosition);
                } else {
                    showCreatePollDialog(polls.get(adapterPosition));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void showConfirmDeleteDialog(final int position) {
        new AlertDialog.Builder(this,R.style.AlertDialogTheme)
                .setTitle(getString(R.string.deletePoll))
                .setMessage(getString(R.string.confirmPollDeletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        polls.remove(position);
                        pollListView.getAdapter().notifyDataSetChanged();
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
