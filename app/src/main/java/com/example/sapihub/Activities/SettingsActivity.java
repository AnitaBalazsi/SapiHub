package com.example.sapihub.Activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.sapihub.R;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addPreferencesFromResource(R.xml.settings_pref);

    }
}
