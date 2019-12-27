package com.example.sapihub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sapihub.R;

public class SplashScreen extends AppCompatActivity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);
        displaySplashScreen();
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
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
