package com.example.sapihub.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.sapihub.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimetableFragment extends Fragment {
    private final String BASE_URL = "https://sapientia-emte.edupage.org/timetable/view.php?num=126";
    private WebView webView;

    public TimetableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = getView().findViewById(R.id.mWebView);
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
            }
        });
        webView.loadUrl(BASE_URL);
    }
}
