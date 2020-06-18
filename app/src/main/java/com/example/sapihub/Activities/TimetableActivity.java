package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.sapihub.R;

public class TimetableActivity extends AppCompatActivity {
    private final String BASE_URL = "https://sapientia-emte.edupage.org/timetable/view.php?num=126";
    private WebView webView;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        webView = findViewById(R.id.mWebView);
        loading = findViewById(R.id.loadingBar);
        loadTimeTable();
    }

    private void loadTimeTable() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setEnabled(false);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:(function() { " +
                        "document.getElementsByTagName('header')[0].style.display='none'; " +
                        "document.getElementById('skin_Div_10').style.display='none'; " +
                        "document.getElementById('skin_Div_11').style.display='none'; " +
                        "})()");
                webView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE );
            }
        });
        webView.loadUrl(BASE_URL);
    }
}
