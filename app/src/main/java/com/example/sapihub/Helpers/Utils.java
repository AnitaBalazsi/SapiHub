package com.example.sapihub.Helpers;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class Utils {
    public static void showSnackbar (View view, String text, int color){
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }
}
