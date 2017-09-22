package com.example.daria.places;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.title_settings);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.settingsContent, new SettingsFragment())
                .commit();
    }
    public static class SettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            //Change value "Search radius"
            ListPreference radius = (ListPreference)this.findPreference("max_radius");
            radius.setSummary(radius.getEntry());
            radius.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = (String) newValue;
            if(value=="500"){
                preference.setSummary(value+" m");
            }else {
                int v = Integer.valueOf(value)/1000;
                preference.setSummary(String.valueOf(v)+" km");
            }
            return true;
        }

    }
}
