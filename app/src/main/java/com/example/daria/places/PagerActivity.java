package com.example.daria.places;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.daria.places.NearbyPlaces.photos;

public class PagerActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener{

    private static final String TAG = "MyAwesomeApp #PAGER";
    private static final File SAVE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    private int number;
    ActionBar actionBar;
    Toolbar toolbar;

    LayoutInflater inflater;
    List<View> pages;

    MyPagerAdapter pagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");

        number = getIntent().getIntExtra("number",0);

        inflater = getLayoutInflater();
        pages = new ArrayList<>();

        for(NearbyPlaces.PhotoTask.AttributedPhoto photo : photos){
            addPage(photo.getBitmap());
        }
        setContentView(R.layout.activity_pager);
        //add first actionbar
        actionBar = getSupportActionBar();
        actionBar.setTitle(photos.get(number).getName().toString());
        actionBar.setSubtitle((number+1)+" "+getResources().getString(R.string.of)+" "+pages.size());
        actionBar.setDisplayHomeAsUpEnabled(true);

        //add toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_pager);
        toolbar.setLogo(R.drawable.ic_location_on_black_24dp);
        toolbar.setSubtitle(photos.get(number).getAddress().toString());

        pagerAdapter = new MyPagerAdapter(pages);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(number);

        viewPager.addOnPageChangeListener(this);
    }

    private void addPage(Bitmap bitmap) {
        View page = inflater.inflate(R.layout.page,null);
        ImageView imgView = (ImageView) page.findViewById(R.id.image_view);
        imgView.setImageBitmap(bitmap);
        pages.add(page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSave:
                if(checkWriteStoragePermissions()){
                    requestPermissions();
                }else savePhoto();
                break;
            case R.id.itemInfo:
                showDialog(R.string.dialog_title,
                        makeDialogText(),
                        R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePhoto() {
        Toast.makeText(this,"save photo...",Toast.LENGTH_LONG).show();
        UUID id = UUID.randomUUID();
        String filename = id.toString().replaceAll("-","")+".jpg";
        File file = new File(SAVE_PATH,filename);
        FileOutputStream fileOutStream = null;
        try{
            fileOutStream = new FileOutputStream(file);
            Bitmap bitmap = photos.get(viewPager.getCurrentItem()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream); // сохранять картинку в jpeg-формате с 100% сжатия.
        }catch (Exception e){
            Log.e(TAG,"Error:"+e.getMessage().toString());
        }finally {
            try {
                if (fileOutStream!=null){
                    fileOutStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG,"Error:"+e.getMessage().toString());
            }
        }
    }

    private boolean checkWriteStoragePermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_DENIED ;
    }

    private void startWriteStoragePermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (shouldProvideRationale) {
            showPermissionDialog();
        } else {
            startWriteStoragePermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePhoto();
            } else {
                Log.i(TAG,"Permission denied.");
                showPermissionDialog();
            }
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PagerActivity.this);
        builder.setMessage(R.string.permission_storage_denied)
                .setTitle(R.string.dialog_perm_title)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private String makeDialogText() {
        NearbyPlaces.PhotoTask.AttributedPhoto photo = photos.get(viewPager.getCurrentItem());
        String placeInfo="";
        if(photo.getPlaceAttributions() != null){
            placeInfo = photo.getPlaceAttributions().toString();
        }
        if(photo.getAttribution() != null){
            placeInfo+="\n"+photo.getAttribution();
        }
        placeInfo = placeInfo.replaceAll("a href=", "");
        placeInfo =placeInfo.replaceAll(">","");
        placeInfo =placeInfo.replaceAll("<","");
        placeInfo =placeInfo.replaceAll("/a","");
        return placeInfo;
    }

    private void showDialog(final int title,
                            String message,
                            final int button,
                            DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PagerActivity.this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(button,listener);
        builder.create().show();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        actionBar.setTitle(photos.get(viewPager.getCurrentItem()).getName().toString());
        actionBar.setSubtitle((viewPager.getCurrentItem()+1)+" "+getResources().getString(R.string.of)+" "+pages.size());
        toolbar.setSubtitle(photos.get(viewPager.getCurrentItem()).getAddress().toString());
    }
}
