package com.example.bchainprac.view.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bchainprac.R;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.MainFragment;
import com.example.bchainprac.view.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemReselectedListener,BottomNavigationView.OnNavigationItemSelectedListener {


    public MainActivity() {
        super(R.layout.activity_main, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setOnNavigationItemReselectedListener(this);
        setFragment(new MainFragment());
        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    private void setFragment(Fragment selectedFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, selectedFragment).commit();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Are you sure to exist?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.onBackPressed();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                setFragment(new MainFragment());
                break;
            case R.id.nav_settings:
                setFragment(new SettingsFragment());
                break;
        }
        return true;
    }
}
