package com.example.sapihub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.sapihub.Fragments.HomeFragment;
import com.example.sapihub.Fragments.MessagesFragment;
import com.example.sapihub.Fragments.NotificationsFragment;
import com.example.sapihub.Fragments.ProfileFragment;
import com.example.sapihub.Helpers.ViewPagerAdapter;
import com.example.sapihub.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    private ViewPagerAdapter adapter;
    private BottomNavigationView bottomNavigationView;
    private MenuItem prevMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupViewPager();
        initializeVariables();
    }

    private void initializeVariables() {
        bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        drawerLayout = findViewById(R.id.drawerLayout);

        viewPager.addOnPageChangeListener(this);

        //sets home fragment as highlighted
        viewPager.setCurrentItem(2);
    }

    public void setNavigationItem(int position){
        if (prevMenuItem != null){
            prevMenuItem.setChecked(false);
        }
        else {
            bottomNavigationView.getMenu().getItem(0).setChecked(false);
        }
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
        prevMenuItem = bottomNavigationView.getMenu().getItem(position);
    }

    public void setupViewPager(){
        viewPager = findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ProfileFragment());
        adapter.addFragment(new NotificationsFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessagesFragment());
        adapter.addFragment(new ProfileFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.bottom_nav_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.bottom_nav_notification:
                viewPager.setCurrentItem(1);
                return true;
            case R.id.bottom_nav_home:
                viewPager.setCurrentItem(2);
                return true;
            case R.id.bottom_nav_messages:
                viewPager.setCurrentItem(3);
                return true;
            case R.id.bottom_nav_profile:
                viewPager.setCurrentItem(4);
                return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
