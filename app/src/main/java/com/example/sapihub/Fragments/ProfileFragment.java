package com.example.sapihub.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private TextView username, department, degree, studyYear, myNewsText, savedNewsText;
    private ImageView profilePicture;
    private ProgressDialog loadingDialog;
    private NewsListAdapter newsAdapter, savedNewsAdapter;
    private RecyclerView newsListView, savedNewsListView;
    private FirebaseRecyclerOptions<News> newsList;
    private ArrayList<String> savedNewsList = new ArrayList<>();
    private static Context context;

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

        context = getContext();
        initializeVariables();
        getUserData();
        getData(DatabaseHelper.newsReference);
        getSavedPosts(new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                savedNewsAdapter = new NewsListAdapter(newsList,savedNewsList,context, Utils.SAVED_POST);
                savedNewsListView.setAdapter(savedNewsAdapter);
                savedNewsAdapter.startListening();
            }
        });
    }

    private void getSavedPosts(final FirebaseCallback callback) {
        DatabaseHelper.savedPostsReference.child(Utils.getCurrentUserToken(context)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedNewsList.clear();
                for (DataSnapshot post : dataSnapshot.getChildren()){
                    savedNewsList.add(post.getKey());
                }
                callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getSavedPosts",databaseError.getMessage());
            }
        });
    }

    private void getUserData() {
        final String student = getActivity().getResources().getString(R.string.student);
        DatabaseHelper.getUserData(Utils.getCurrentUserToken(getActivity()), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                User user = (User) object;
                username.setText(user.getName());
                department.setText(user.getDepartment());

                if (user.getOccupation().equals(student)){
                    degree.setVisibility(View.VISIBLE);
                    degree.setText(user.getDegree());
                    studyYear.setVisibility(View.VISIBLE);
                    studyYear.setText(user.getStudyYear());
                }
            }
        });

        Utils.loadProfilePicture(context,profilePicture,Utils.getCurrentUserToken(context),350,350);
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

        LinearLayoutManager newsLayout = new LinearLayoutManager(context);
        newsLayout.setReverseLayout(true);
        newsLayout.setStackFromEnd(true);
        newsListView.setLayoutManager(newsLayout);

        LinearLayoutManager savedNewsLayout = new LinearLayoutManager(context);
        savedNewsLayout.setReverseLayout(true);
        savedNewsLayout.setStackFromEnd(true);
        savedNewsListView.setLayoutManager(savedNewsLayout);

        loadingDialog = new ProgressDialog(context, R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.loading));
    }

    private void getData(Query q){
        newsList = new FirebaseRecyclerOptions.Builder<News>().setQuery(q, News.class).build();
        newsAdapter = new NewsListAdapter(newsList,null,context, Utils.MY_POST);
        newsListView.setAdapter(newsAdapter);
        newsAdapter.startListening();
    }

    @Override
    public void onClick(View v) {
        showChangePictureDialog();
    }

    private void showChangePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
        String[] options = {getString(R.string.deleteProfilePicture), getString(R.string.changeProfilePicture)};
        builder.setItems(options, new DialogInterface.OnClickListener() {
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
        new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                .setTitle(getString(R.string.deleteProfilePicture))
                .setMessage(getString(R.string.confirmImageDeletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseHelper.deleteProfilePicture(Utils.getCurrentUserToken(context), new FirebaseCallback() {
                            @Override
                            public void onCallback(Object object) {
                                //refresh imageview
                                Utils.loadProfilePicture(context,profilePicture,Utils.getCurrentUserToken(context),350,350);
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
            Uri imagePath = data.getData();

            loadingDialog.setMessage(getString(R.string.uploadImage));
            loadingDialog.show();

            DatabaseHelper.uploadProfilePicture(Utils.getCurrentUserToken(context), imagePath, new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    loadingDialog.dismiss();
                    Utils.loadProfilePicture(context,profilePicture,Utils.getCurrentUserToken(context),400,400);
                }
            });

        }
    }

}
