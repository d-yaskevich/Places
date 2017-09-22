package com.example.daria.places;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static com.example.daria.places.R.id.map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MyAwesomeApp (: ";
    String str;

    //Constant used in requesting runtime permissions.
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    //
    private boolean mLocationResolutionDenied = false;

    //The desired interval for location updates.
    //Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    //The fastest rate for active location updates.
    //Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    //Stores parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;

    //Stores the types of location services the client is interested in using.
    //Used for checking settings to determine if the device has optimal location settings.
    private LocationSettingsRequest mLocationSettingsRequest;

    //Callback for Location events.
    private LocationCallback mLocationCallback;

    //Keys for storing activity state in the Bundle.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LAST_LOCATION = "last_location";
    private static final String KEY_CURRENT_LOCATION = "current_location";
    private static final String KEY_LOCATION_PERMISSION = "location_permission";
    private static final String KEY_LOCATION_RESOLUTION = "location_resolution";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    //Represents a geographical location.
    private Location mCurrentLocation;

    private Location mLastKnowLocation;
    private final LatLng mDefaultLocation = new LatLng(53.902301, 27.561903);

    private CameraPosition mCameraPosition;
    private static final int DEFAULT_ZOOM = 15;

    //Fab button for switching to child activity
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.action_button);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NearbyPlaces.class);
                intent.putExtra("lat", mCurrentLocation.getLatitude());
                intent.putExtra("lng", mCurrentLocation.getLongitude());
                //intent.putExtra("lat", 0);
                //intent.putExtra("lng", 0);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Log.i(TAG, "Restart app. Return of camera position and location.");
            str="updateValuesFromBundle()";
            // Update the value of mCurrentLocation, mLastKnowLocation, mCameraPosition
            // and mLocationPermissionGranted from the Bundle
            if (savedInstanceState.keySet().contains(KEY_CURRENT_LOCATION)) {
                //Since KEY_CURRENT_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                //is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_CURRENT_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_LOCATION)) {
                mLastKnowLocation = savedInstanceState.getParcelable(KEY_LAST_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_CAMERA_POSITION)) {
                mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION_PERMISSION)){
                mLocationPermissionGranted = savedInstanceState.getBoolean(KEY_LOCATION_PERMISSION);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION_RESOLUTION)){
                mLocationResolutionDenied = savedInstanceState.getBoolean(KEY_LOCATION_RESOLUTION);
            }
        }
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        Log.i(TAG,"createLocationCallback()");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                str="onLocationResult()";
                Log.i(TAG,"onLocationResult");
                if(mMap != null){
                    if(mCurrentLocation != null){
                        mLastKnowLocation = mCurrentLocation;
                    }
                    mCurrentLocation = locationResult.getLastLocation();
                    if(mCameraPosition!=null){
                        mCameraPosition = mMap.getCameraPosition();
                    }
                    updateLocation();
                }
            }
        };
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,"onActivityResult()");
        str="onActivityResult()";
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationUpdates() gets called in onResume() if permission granted.
                        mLocationResolutionDenied = false;
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mLocationResolutionDenied = true;
                        updateLocation();
                        break;
                }
                break;
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        Log.i(TAG,"startLocationUpdates()");
        str="startLocationUpdates()";
        // Begin by checking if the device has the necessary location settings.
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest)
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                                mLocationResolutionDenied = false;
                                try {
                                    if (mLocationPermissionGranted){
                                        LocationServices.FusedLocationApi
                                                .requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                                                mLocationCallback, Looper.myLooper());
                                    }
                                }catch (SecurityException e){
                                    Log.w(TAG, "Exception: "+e.getMessage());
                                }
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    Log.i(TAG,"RESOLUTION_REQUIRED");
                                    if(mLocationResolutionDenied){
                                        showSnackbar(R.string.location_off_mode, R.string.settings,
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Build intent that displays the App settings screen.
                                                        Intent intent = new Intent();
                                                        intent.setAction(
                                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                });
                                    } else {
                                        status.startResolutionForResult(
                                                MainActivity.this,
                                                REQUEST_CHECK_SETTINGS);
                                    }
                                } catch (IntentSender.SendIntentException e) {
                                    Log.w(TAG,"Exception: "+e.getMessage().toString());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        Log.i(TAG,"onResume()");
        str="onResume()";
        if (!checkPermissions()) {
            Log.i(TAG,"PERMISSION_GRANTED");
            mLocationPermissionGranted = true;
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart()");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop()");
    }

    /**
     * Save camera position and location.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG,"Save camera position and location.");
        if (mMap != null){
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LAST_LOCATION, mLastKnowLocation);
            outState.putParcelable(KEY_CURRENT_LOCATION, mCurrentLocation);
            outState.putBoolean(KEY_LOCATION_PERMISSION, mLocationPermissionGranted);
            outState.putBoolean(KEY_LOCATION_RESOLUTION, mLocationResolutionDenied);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Play services connection suspended");
        Toast.makeText(this,"Play services connection suspended",Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
        Toast.makeText(this,"Play services connection failed!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG,"Map is ready!");
        str="onMapReady()";
        mMap = googleMap;
        if (checkPermissions()) {
            Log.i(TAG,"PERMISSION_DENIED");
            requestPermissions();
            updateLocation();
        }

    }

    private void updateLocationUI() {
        Log.i(TAG, "Update Location UI");
        if (mMap != null){
            try{
                if (mLocationPermissionGranted
                        && !mLocationResolutionDenied){
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);//location button is visible
                } else {
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);//location button is not visible
                }
            }catch (SecurityException e){
                Log.w(TAG, "Exception "+e.getMessage());
            }
        }else {
            Toast.makeText(this,"Map is null",Toast.LENGTH_LONG).show();
            Log.i(TAG, "Map is null");
            return;
        }
    }

    public void updateLocation() {
        //Set the map's camera position to the current location of the device.
        Log.i(TAG, "Update Location from "+str);
        if (mCameraPosition != null){
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else {
            if (mCurrentLocation != null ){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude()),
                        DEFAULT_ZOOM));
                mCameraPosition = mMap.getCameraPosition();
            } else {
                if (mLastKnowLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnowLocation.getLatitude(),
                                    mLastKnowLocation.getLongitude()),
                            DEFAULT_ZOOM));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                }
            }
        }
        if(mCameraPosition != null){
            Log.e(TAG,"mCameraPosition = ["+mCameraPosition.target.latitude+","+mCameraPosition.target.longitude+"]");
        }else Log.e(TAG,"mCameraPosition is null.");
        if(mCurrentLocation != null){
            mLastKnowLocation = mCurrentLocation;
            Log.e(TAG,"mCurrent"+mCurrentLocation.toString());
        }else Log.e(TAG,"mCurrentLocation is null.");
        if(mLastKnowLocation != null){
            Log.e(TAG,"mLast"+mLastKnowLocation.toString());
        }else Log.e(TAG,"mLastKnowLocation is null.");
        updateLocationUI();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_DENIED ;
    }

    private void startLocationPermissionRequest() {
        Log.i(TAG,"startLocationPermissionRequest()");
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length <= 0) {
                //If user interaction was interrupted, the permission request is cancelled and you
                //receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
                mLocationPermissionGranted = false;
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                mLocationPermissionGranted = true;
            } else {
                /**
                 * Permission denied.
                 *
                 * Notify the user via a SnackBar that they have rejected a core permission for the
                 * app, which makes the Activity useless. In a real app, core permissions would
                 * typically be best requested during a welcome-screen flow.
                 *
                 * Additionally, it is important to remember that a permission might have been
                 * rejected without asking the user for permission (device policy or "Never ask
                 * again" prompts). Therefore, a user interface affordance is typically implemented
                 * when permissions are denied. Otherwise, your app could appear unresponsive to
                 * touches or interactions which have required permissions.
                 */
                Log.i(TAG,"Permission denied.");
                mLocationPermissionGranted=false;
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId,
                              final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
}
