package com.example.sapihub.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.sapihub.Fragments.DeadlinesFragment;
import com.example.sapihub.Fragments.NewsFragment;
import com.example.sapihub.R;

public class FragmentLoader extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_loader);

        String[] menuItems = this.getResources().getStringArray(R.array.homeMenu);
        String fragment = getIntent().getExtras().getString("fragmentName");
        if (fragment.equals(menuItems[0])){
            loadFragment(new DeadlinesFragment(),null);
        }

    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1) {
            super.onBackPressed();
            startActivity(new Intent(FragmentLoader.this,HomeActivity.class));
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    private void loadFragment(Fragment fragment, Bundle bundle){
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment, Bundle bundle){
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
