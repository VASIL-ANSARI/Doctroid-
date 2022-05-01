package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.bchainprac.R;
import com.example.bchainprac.view.activity.MainActivity;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.MainFragment;
import com.example.bchainprac.view.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HospitalMainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemReselectedListener,BottomNavigationView.OnNavigationItemSelectedListener {


    public HospitalMainActivity(){
        super(R.layout.activity_hospital_main,true);
    }
    @Override
    protected void doOnCreate(Bundle bundle) {
        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_navigation_hospital);
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setOnNavigationItemReselectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout_hospital, new HospitalMainFragment()).commit();
        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Are you sure to exist?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HospitalMainActivity.super.onBackPressed();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout_hospital, new HospitalMainFragment()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout_hospital, new HospitalSettingsFragment()).commit();
                break;
        }
        return true;
    }
}
