package com.example.sapihub.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sapihub.R;

public class SplashScreen extends AppCompatActivity {
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);
        displaySplashScreen();
    }

    private void checkUserData() {
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if (sharedPreferences.getString("username", null) != null){
           Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void displaySplashScreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i += 10){
                    try{
                        Thread.sleep(200);
                        progressBar.setProgress(i);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                checkUserData();
                finish();
            }
        }).start();
    }
}
