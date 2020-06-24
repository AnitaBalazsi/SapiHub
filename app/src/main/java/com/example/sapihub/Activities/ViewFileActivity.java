package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.sapihub.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class ViewFileActivity extends AppCompatActivity {
    private PDFView pdfView;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        fileName = getIntent().getStringExtra("fileName");
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

        pdfView = findViewById(R.id.pdfView);
        pdfView.fromFile(file).load();
    }
}
