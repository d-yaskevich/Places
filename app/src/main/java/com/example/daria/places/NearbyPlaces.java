package com.example.daria.places;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import static android.util.Log.d;

public class NearbyPlaces extends AppCompatActivity {

    private static final String TAG = "MyAwesomeApp #PHOTO";

    SharedPreferences mSettings;

    private ImageView img1, img2, img3, img4;
    private double lat, lng;
    private int radiusMAX;

    @Override
    protected void onResume() {
        super.onResume();
        radiusMAX = Integer.valueOf(mSettings.getString("max_radius","5000"));
        Toast.makeText(this,"radiusMAX="+radiusMAX,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d(TAG, "Method onCreate() is called!");
        setContentView(R.layout.activity_places);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.action_bar_title);

        img1 = (ImageView) findViewById(R.id.imageView1);
        img2 = (ImageView) findViewById(R.id.imageView2);
        img3 = (ImageView) findViewById(R.id.imageView3);
        img4 = (ImageView) findViewById(R.id.imageView4);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        Log.d(TAG, "lat,lng="+lat+","+lng);
        Toast.makeText(this,"lat,lng="+lat+","+lng,Toast.LENGTH_LONG).show();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //checkPermission

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {

    }
}
