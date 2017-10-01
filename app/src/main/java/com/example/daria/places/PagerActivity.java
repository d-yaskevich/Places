package com.example.daria.places;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PagerActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener{

    private static final String TAG = "MyAwesomeApp #PAGER";

    private int number;
    Toolbar toolbar,toolbarEnd;
    ArrayList<Integer> places;

    LayoutInflater inflater;
    List<View> pages;

    MyPagerAdapter pagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");

        number = getIntent().getIntExtra("number",0);
        Log.i(TAG,"number: "+number);
        places = getIntent().getIntegerArrayListExtra("array_photo");

        inflater = getLayoutInflater();
        pages = new ArrayList<>();

        for(int i=0;i<places.size();i++){
            addPage(places.get(i));
        }

        setContentView(R.layout.activity_pager);

        toolbar = (Toolbar) findViewById(R.id.toolbar_pager);
        toolbar.setTitle("Balkan Restaurant "+(number+1));
        toolbar.setSubtitle((number+1)+" "+getResources().getString(R.string.of)+" "+pages.size());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        toolbarEnd = (Toolbar) findViewById(R.id.toolbar_pager_end);
        toolbarEnd.setLogo(R.drawable.ic_location_on_black_24dp);
        toolbarEnd.setTitle(" "+(number+1)+"0 Crown St, Sydney NSW 2010, Австралия");

        pagerAdapter = new MyPagerAdapter(pages);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(number);

        viewPager.addOnPageChangeListener(this);
    }

    private void addPage(int image) {
        View page = inflater.inflate(R.layout.page,null);
        ImageView imgView = (ImageView) page.findViewById(R.id.image_view);
        imgView.setImageResource(image);
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
                Toast.makeText(this,"save_button",Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        toolbar.setTitle("Balkan Restaurant "+(viewPager.getCurrentItem()+1));
        toolbar.setSubtitle((viewPager.getCurrentItem()+1)+" "+getResources().getString(R.string.of)+" "+pages.size());
        toolbarEnd.setTitle((viewPager.getCurrentItem()+1)+"0 Crown St, Sydney NSW 2010, Австралия");
    }
}
