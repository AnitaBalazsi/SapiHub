package com.example.sapihub.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sapihub.Activities.HomeActivity;
import com.example.sapihub.Activities.LoginActivity;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private TextView username, department, degree, studyYear;
    private ImageView profilePicture;
    private ProgressDialog loadingDialog;
    private Uri imagePath;


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
    }

    private void getUserData() {
        loadingDialog.show();
        DatabaseHelper.getCurrentUserData(Utils.getCurrentUserToken(getContext()), new FirebaseCallback() {
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
                    Glide.with(getContext()).load(imageUri.toString()).apply(new RequestOptions().override(600, 600)).into(profilePicture);
                }
                loadingDialog.dismiss();
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

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setMessage(getString(R.string.loading));
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
                .setMessage(getString(R.string.confirmDeletion))
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
}
