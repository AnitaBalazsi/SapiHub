package com.example.sapihub.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Activities.NewsDetailsActivity;
import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener, NewsListAdapter.ListViewHolder.NewsClickListener {
    private TextView username, department, degree, studyYear, myNewsText, savedNewsText;
    private ImageView profilePicture;
    private ProgressDialog loadingDialog;
    private Uri imagePath;
    private NewsListAdapter newsAdapter, savedNewsAdapter;
    private RecyclerView newsListView, savedNewsListView;
    private List<News> newsList = new ArrayList<>(), savedNewsList = new ArrayList<>();
    private List<String> savedNewsKeys = new ArrayList<>();


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeVariables();
        getUserData();
        getSavedPostList(new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                getData();
            }
        });
    }

    private void getSavedPostList(final FirebaseCallback callback) {
        DatabaseHelper.savedPostsReference.child(Utils.getCurrentUserToken(getContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postData : dataSnapshot.getChildren()){
                    savedNewsKeys.add(postData.getKey());
                }
                callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData() {
        loadingDialog.show();
        DatabaseHelper.getUserData(Utils.getCurrentUserToken(getContext()), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                username.setText(user.getName());
                department.setText(user.getDepartment());
                if (user.getOccupation().equals(getString(R.string.student))){
                    degree.setVisibility(View.VISIBLE);
                    degree.setText(user.getDegree());
                    studyYear.setVisibility(View.VISIBLE);
                    studyYear.setText(user.getStudyYear());
                }
            }
        });

        DatabaseHelper.getProfilePicture(Utils.getCurrentUserToken(getContext()), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if (object != null){
                    Uri imageUri = (Uri) object;
                    Glide.with(getContext()).load(imageUri.toString())
                            .apply(new RequestOptions().override(600, 600))
                            .circleCrop()
                            .into(profilePicture);
                }
            }
        });
    }

    private void initializeVariables() {
        username = getView().findViewById(R.id.name);
        department = getView().findViewById(R.id.department);
        degree = getView().findViewById(R.id.degree);
        studyYear = getView().findViewById(R.id.year);
        profilePicture = getView().findViewById(R.id.profilePicture);
        profilePicture.setOnClickListener(this);
        newsListView = getView().findViewById(R.id.myPostList);
        savedNewsListView = getView().findViewById(R.id.savedPostList);
        myNewsText = getView().findViewById(R.id.myNewsText);
        myNewsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsListView.setVisibility(View.VISIBLE);
                savedNewsListView.setVisibility(View.GONE);
                myNewsText.setTypeface(myNewsText.getTypeface(), Typeface.BOLD);
                savedNewsText.setTypeface(null, Typeface.NORMAL);
            }
        });
        savedNewsText = getView().findViewById(R.id.savedNewsText);
        savedNewsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedNewsListView.setVisibility(View.VISIBLE);
                newsListView.setVisibility(View.GONE);
                savedNewsText.setTypeface(savedNewsText.getTypeface(), Typeface.BOLD);
                myNewsText.setTypeface(null, Typeface.NORMAL);
            }
        });

        LinearLayoutManager newsLayout = new LinearLayoutManager(getContext());
        newsLayout.setReverseLayout(true);
        newsLayout.setStackFromEnd(true);
        newsListView.setLayoutManager(newsLayout);
        newsAdapter = new NewsListAdapter("MyNews",newsList,getContext(),this);
        newsListView.setAdapter(newsAdapter);

        LinearLayoutManager savedNewsLayout = new LinearLayoutManager(getContext());
        savedNewsLayout.setReverseLayout(true);
        savedNewsLayout.setStackFromEnd(true);
        savedNewsListView.setLayoutManager(savedNewsLayout);
        savedNewsAdapter = new NewsListAdapter("SavedNews",savedNewsList,getContext(),this);
        savedNewsListView.setAdapter(savedNewsAdapter);

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.loading));
    }

    private void getData(){
        DatabaseHelper.newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsList.clear();
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    News news = newsData.getValue(News.class);
                    if (news.getAuthor().equals(Utils.getCurrentUserToken(getContext()))){
                        newsList.add(news);
                        newsAdapter.notifyDataSetChanged();
                    }
                    if (savedNewsKeys.contains(newsData.getKey())){
                        savedNewsList.add(news);
                        savedNewsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadingDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        showChangePictureDialog();
    }

    private void showChangePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String[] animals = {getString(R.string.deleteProfilePicture), getString(R.string.changeProfilePicture)};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showConfirmationDialog();
                        break;
                    case 1:
                        selectImage();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.deleteProfilePicture))
                .setMessage(getString(R.string.confirmImageDeletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseHelper.deleteProfilePicture(Utils.getCurrentUserToken(getContext()), new FirebaseCallback() {
                            @Override
                            public void onCallback(Object object) {
                                //delete image from imageview
                                profilePicture.setImageDrawable(getContext().getDrawable(R.drawable.ic_image_black_24dp));
                            }
                        });
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPicture)), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null){
            imagePath = data.getData();

            loadingDialog.setMessage(getString(R.string.uploadImage));
            loadingDialog.show();

            DatabaseHelper.uploadProfilePicture(Utils.getCurrentUserToken(getContext()), imagePath, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    loadingDialog.dismiss();
                    try {
                        // Setting image on image view using Bitmap
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagePath);
                        profilePicture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    public void onNewsClick(int position, String TAG) {
        News selectedNews = new News();
        switch (TAG){
            case "MyNews":
                selectedNews = newsList.get(position);
                break;
            case "SavedNews":
                selectedNews = savedNewsList.get(position);
                break;
        }

        Intent openDetails = new Intent(getActivity(), NewsDetailsActivity.class);
        openDetails.putExtra("selectedNews", selectedNews);
        startActivity(openDetails);
    }

    @Override
    public void onProfileClick(int position) {

    }

    @Override
    public void onMoreOptionsClick(View itemView, int position) {
        //todo options
    }

    @Override
    public void onSavePost(View itemView, int position) {
            //todo
    }

    @Override
    public void onWriteComment(View itemView, int position) {
            //todo
    }

    @Override
    public void onSendComment(View itemView, int position, String tag) {
        //todo
    }
}
