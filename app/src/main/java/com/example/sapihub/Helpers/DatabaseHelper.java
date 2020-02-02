package com.example.sapihub.Helpers;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseHelper {
    public static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference newsReference = FirebaseDatabase.getInstance().getReference("News");
    public static StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public static void addUser(final User user){
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(user.getToken()).exists()){
                    //if user is not stored in database
                    userReference.child(user.getToken()).setValue(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void addNews(final News news){
        newsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsReference.push().setValue(news);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void uploadImage(String name, Uri imagePath){
        storageReference.child(name).putFile(imagePath);
    }
}
