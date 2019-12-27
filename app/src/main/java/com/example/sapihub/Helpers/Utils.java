package com.example.sapihub.Helpers;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Utils {
    private static final Utils instance = new Utils();

    public static Utils getInstance() {
        return instance;
    }

    public void showSnackbar (View view, String text, int color){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }
}
