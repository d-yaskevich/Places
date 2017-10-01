package com.example.daria.places;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.util.Log.d;
import static com.example.daria.places.R.id.imageView1;
import static com.example.daria.places.R.id.imageView2;
import static com.example.daria.places.R.id.imageView3;
import static com.example.daria.places.R.id.imageView4;

public class NearbyPlaces extends AppCompatActivity {

    private static final String TAG = "MyAwesomeApp #PHOTO";

    SharedPreferences mSettings;

    private ImageView img1, img2, img3, img4;

    private double lat, lng;
    private int radiusMAX;
    private ArrayList<Integer> places;

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

        img1 = (ImageView) findViewById(imageView1);
        img2 = (ImageView) findViewById(imageView2);
        img3 = (ImageView) findViewById(imageView3);
        img4 = (ImageView) findViewById(R.id.imageView4);

        places = new ArrayList<>();
        places.add(R.drawable.ic_add_48px);
        places.add(R.drawable.ic_file_download_24dp);
        places.add(R.drawable.ic_location_on_black_24dp);
        places.add(R.drawable.ic_settings_black_24dp);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        Toast.makeText(this,"lat,lng="+lat+","+lng,Toast.LENGTH_LONG).show();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //checkPermission??????

        setImage(places);
    }

    private void setImage(ArrayList<Integer> places) {
        img1.setImageResource(places.get(0));
        img2.setImageResource(places.get(1));
        img3.setImageResource(places.get(2));
        img4.setImageResource(places.get(3));
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

    public void imageClick(View v){
        ImageView image = (ImageView) v;
        int number = 0;
        switch (image.getId()){
            case imageView1: number = 0;
                break;
            case imageView2: number = 1;
                break;
            case imageView3: number = 2;
                break;
            case imageView4: number = 3;
                break;
        }
        //go to PagerActivity
        Intent intent = new Intent(NearbyPlaces.this, PagerActivity.class);
        intent.putExtra("number", number);
        intent.putExtra("array_photo", places);
        setResult(RESULT_OK, intent);
        startActivity(intent);
    }
}
