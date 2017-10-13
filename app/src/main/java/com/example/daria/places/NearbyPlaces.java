package com.example.daria.places;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.util.Log.d;
import static com.example.daria.places.MainActivity.places;

public class NearbyPlaces extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MyAwesomeApp #PHOTO";

    SharedPreferences mSettings;
    public static final String APP_PREFERENCES_RADIUS = "max_count";
    private int radiusMAX;

    private ActionBar ab;
    private ProgressBar progressBar;
    private GridView gridView;

    private DisplayMetrics metrics;
    private int screenWidth, screenHeight;

    private GoogleApiClient mGoogleApiClient;
    public static ArrayList<PhotoTask.AttributedPhoto> photos;

    private static final String PROGRESS_STATUS_KEY = "progress_status";
    private static final String PROGRESS_VALUE_KEY = "progress_value";
    public static int progressStatus = 0;
    public static int progressValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d(TAG, "Method onCreate() is called!");
        setContentView(R.layout.activity_places);

        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.action_bar_title);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //go to PagerActivity
                if(progressStatus==places.size()){
                    Intent intent = new Intent(NearbyPlaces.this, PagerActivity.class);
                    intent.putExtra("number", position);
                    startActivity(intent);
                }
            }
        });

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        getDisplayMetrics();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getDisplayMetrics() {
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();
        radiusMAX = Integer.valueOf(mSettings.getString(APP_PREFERENCES_RADIUS, "25"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(progressStatus!=places.size()){
            menu.findItem(R.id.itemSettings).setVisible(false);
        }
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
        if (savedInstanceState != null) {
            Log.i(TAG, "Restart app. Return progress status");
            if (savedInstanceState.keySet().contains(PROGRESS_STATUS_KEY)) {
                progressStatus = savedInstanceState.getInt(PROGRESS_STATUS_KEY);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        progressStatus = progressBar.getProgress();
        Log.i(TAG,"Save progress status: "+progressStatus);
        outState.putInt(PROGRESS_STATUS_KEY, progressStatus);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(places.size()==0){
            Toast.makeText(NearbyPlaces.this,R.string.not_photos_place,Toast.LENGTH_LONG);
            Log.w(TAG,"no places no photos");
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            getPhotoOfPlaces();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Play services connection suspended");
        Toast.makeText(this,R.string.on_connection_suspended,Toast.LENGTH_LONG).show();
        progressBar.setIndeterminate(true);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
        Toast.makeText(this,R.string.on_connection_failed,Toast.LENGTH_LONG).show();
        ab.setTitle(R.string.action_bar_title);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void getPhotoOfPlaces() {
        if (progressStatus==places.size()){
            progressBar.setVisibility(View.INVISIBLE);
            gridView.setAdapter(new MyImageAdapter(NearbyPlaces.this));
        }else {
            switch (progressStatus){
                case 0:
                    photos = new ArrayList<>();
                default:
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(places.size());
                    progressBar.setProgress(progressStatus);
                    for(int i=progressStatus;i<places.size();i++){
                        placePhotosTask(places.get(i).getId());
                    }
                    break;
            }
        }
        //Toast.makeText(NearbyPlaces.this, "Something wrong :/", Toast.LENGTH_LONG);
    }

    private void placePhotosTask(String placeId) {
        // Create a new AsyncTask that displays the bitmap once loaded.
        new PhotoTask(screenWidth, screenHeight) {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                super.onPostExecute(attributedPhoto);
                if(attributedPhoto!=null){
                    photos.add(attributedPhoto);
                    gridView.setAdapter(new MyImageAdapter(NearbyPlaces.this));
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressBar.setProgress(progressBar.getProgress()+values[0]);
                if(progressBar.getProgress()==places.size()){
                    progressBar.setVisibility(View.INVISIBLE);
                    ActivityCompat.invalidateOptionsMenu(NearbyPlaces.this);
                    if(photos.size()==0){
                        Toast.makeText(NearbyPlaces.this,R.string.not_photos_place,Toast.LENGTH_LONG);
                        Log.w(TAG,"just no photos");
                    }
                }
            }

        }.execute(placeId);
    }

    public abstract class PhotoTask extends AsyncTask<String, Integer, PhotoTask.AttributedPhoto> {

        private int mHeight;
        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected PhotoTask.AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();
            publishProgress(1);
            progressStatus++;

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the random bitmap and its attributions for this place.
                    int random=0;
                    if(photoMetadataBuffer.getCount()!=1){
                        random = new Random().nextInt(photoMetadataBuffer.getCount()-1);
                    }
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(random);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();
                    attributedPhoto = new AttributedPhoto(attribution, image, placeId);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;
            public final Bitmap bitmap;
            public final String placeId;
            public List<Integer> placeTypes;
            public CharSequence placeAttributions;
            public CharSequence address;
            public CharSequence name;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap, String placeId) {
                this.attribution = attribution;
                this.bitmap = bitmap;
                this.placeId = placeId;
                for(Place place : places){
                    if(place.getId()==placeId){
                        this.address = place.getAddress();
                        this.name = place.getName();
                        this.placeAttributions = place.getAttributions();
                        this.placeTypes = place.getPlaceTypes();
                    }
                }
            }

            public Bitmap getBitmap() {
                return bitmap;
            }

            public CharSequence getAddress() {
                return address;
            }

            public CharSequence getName() {
                return name;
            }

            @Override
            public String toString() {
                return "AttributedPhoto{" +
                        "name=" + name +
                        '}';
            }

            public CharSequence getAttribution() {
                return attribution;
            }

            public String getPlaceId() {
                return placeId;
            }

            public List<Integer> getPlaceTypes() {
                return placeTypes;
            }

            public CharSequence getPlaceAttributions() {
                return placeAttributions;
            }
        }
    }

}
