package com.example.daria.places;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by yakov on 24.09.2017.
 */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "MyAwesomeApp #SETTINGS";
    private static ListPreference radius;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setRetainInstance(true);
        //Change value "Search radius" in summary
        radius = (ListPreference)this.findPreference("max_count");
        radius.setSummary(radius.getEntry());
        radius.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = ((ListPreference)preference).findIndexOfValue(newValue.toString());
        CharSequence[] entries = ((ListPreference)preference).getEntries();
        preference.setSummary(entries[i]);
        return true;
    }

}
