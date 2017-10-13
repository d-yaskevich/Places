package com.example.daria.places;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "MyAwesomeApp #SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.title_settings);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContent, new SettingsFragment())
                .commit();
    }

}


