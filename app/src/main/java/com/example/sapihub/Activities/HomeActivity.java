package com.example.sapihub.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.sapihub.Fragments.MessagesFragment;
import com.example.sapihub.Fragments.NewsFragment;
import com.example.sapihub.Fragments.NotificationsFragment;
import com.example.sapihub.Fragments.ProfileFragment;
import com.example.sapihub.Helpers.Adapters.ViewPagerAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener {
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    private ViewPagerAdapter adapter;
    private BottomNavigationView bottomNavigationView;
    private int selectedMenuItem = -1;
    private ListView menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupViewPager();
        initializeVariables();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseHelper.changeStatus(Utils.getCurrentUserToken(this),getString(R.string.online));
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseHelper.changeStatus(Utils.getCurrentUserToken(this),getString(R.string.offline));
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHelper.changeStatus(Utils.getCurrentUserToken(this),getString(R.string.offline));
    }

    private void initializeVariables() {
        bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        menuList = findViewById(R.id.menuList);
        menuList.setAdapter(new ArrayAdapter<>(this, R.layout.menu_row, R.id.title, this.getResources().getStringArray(R.array.homeMenu)));
        menuList.setOnItemClickListener(this);

        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(1); //sets home fragment as highlighted
    }

    public void setNavigationItem(int position){
        if (selectedMenuItem != -1){
            bottomNavigationView.getMenu().getItem(selectedMenuItem).setChecked(false);
        }
        else {
            //first swipe
            bottomNavigationView.getMenu().getItem(1).setChecked(false);
        }

        bottomNavigationView.getMenu().getItem(position).setChecked(true);
        selectedMenuItem = position;
    }

    public void setupViewPager(){
        viewPager = findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NotificationsFragment());
        adapter.addFragment(new NewsFragment());
        adapter.addFragment(new MessagesFragment());
        adapter.addFragment(new ProfileFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.bottom_nav_menu:
                drawerLayout.openDrawer(menuList);
                return true;
            case R.id.bottom_nav_notification:
                viewPager.setCurrentItem(0);
                return true;
            case R.id.bottom_nav_home:
                viewPager.setCurrentItem(1);
                return true;
            case R.id.bottom_nav_messages:
                viewPager.setCurrentItem(2);
                return true;
            case R.id.bottom_nav_profile:
                viewPager.setCurrentItem(3);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setNavigationItem(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String fragmentName = String.valueOf(parent.getItemAtPosition(position));
        if (fragmentName.equals(getString(R.string.logOut))){
            SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove("username").apply();
            sharedPreferences.edit().remove("password").apply();
            startActivity(new Intent(HomeActivity.this,LoginActivity.class));
            //todo

        }

        if (fragmentName.equals(getString(R.string.deadlines))){
            startActivity(new Intent(HomeActivity.this,DeadlinesActivity.class));
        }

        if (fragmentName.equals(getString(R.string.settings))){
            startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
        }
    }
}
