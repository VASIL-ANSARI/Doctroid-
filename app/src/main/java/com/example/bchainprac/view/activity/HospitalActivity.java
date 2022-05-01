package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.helpers.Navigator;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.HospitalAboutFragment;
import com.example.bchainprac.view.fragment.HospitalPhotosFragment;
import com.example.bchainprac.view.fragment.MapsActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HospitalActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemReselectedListener {

    ImageView phone, facebook, website,email;
    BottomNavigationView bottomNav;
    Hospital hospital;
    String hid;
    TextView ph;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String it,admin,general,marketing,purchasing;
    private List<String> managers;
    private Boolean verification;
    private ImageView img;

    public HospitalActivity() {
        super(R.layout.activity_hospital, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarBackImageView.setVisibility(View.VISIBLE);

        hid=getIntent().getStringExtra("Id");
        hospital=new Hospital();
        hospital.setHospital_name(getIntent().getStringExtra("nameHospital"));
        hospital.setHospital_location(getIntent().getStringExtra("Loc"));
        hospital.setHospital_phone(getIntent().getStringExtra("phoneHospital"));
        hospital.setLatitude(getIntent().getDoubleExtra("Latitude",0.0));
        hospital.setLongitude(getIntent().getDoubleExtra("Longitude",0.0));
        hospital.setHospital_facebook(getIntent().getStringExtra("facebookUrl"));
        hospital.setHospital_website(getIntent().getStringExtra("websiteUrl"));
        hospital.setHospital_email(getIntent().getStringExtra("emailId"));
        it=getIntent().getStringExtra("ITmanager");
        admin=getIntent().getStringExtra("Adminmanager");
        general=getIntent().getStringExtra("Generalmanager");
        marketing=getIntent().getStringExtra("Marketingmanager");
        purchasing=getIntent().getStringExtra("Purchasingmanager");
        managers=new ArrayList<>(Arrays.asList(new String[]{it,admin,general,marketing,purchasing}));
        verification=getIntent().getBooleanExtra("verificationId",false);


        toolbarTextView.setText(hospital.getHospital_name());

        initializeComponents();
        setListeners();
        //loc.setText(hospital.getHospital_location());
        ph.setText(hospital.getHospital_phone());


        bottomNav = findViewById(R.id.hospital_bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        setFragment(new HospitalAboutFragment(hospital,managers));
        bottomNav.setOnNavigationItemReselectedListener(this);
        bottomNav.getMenu().findItem(R.id.hospital_nav_about).setChecked(true);

        checkLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        startLocationUpdates();


    }

    private void startLocationUpdates() {
        Log.d("message","start updates");
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("message", "All location settings are satisfied.");
                Toast.makeText(HospitalActivity.this, "Started location updates!",
                        Toast.LENGTH_SHORT).show();
                //noinspection MissingPermission
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("message", "Location settings are not satisfied. Attempting to upgrade " + "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult((Activity) HospitalActivity.this, REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException sie) {
                            Log.d("message", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings.";
                        Log.d("message", errorMessage);
                        Toast.makeText(HospitalActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void stopLocationUpdates() {
        Log.d("message","stop updates");
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener((Activity) this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Toast.makeText(HospitalActivity.this, "Location updates stopped!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeComponents() {
        //loc=findViewById(R.id.idd1);
        ph=findViewById(R.id.idd2);
        phone = findViewById(R.id.hospital_phone_icon);
        facebook = findViewById(R.id.hospital_facebook_icon);
        website = findViewById(R.id.hospital_website_icon);
        email=findViewById(R.id.hospital_email_icon);
        img=findViewById(R.id.verified_id);
        if(verification && it.length()>0 && admin.length()>0 && general.length()>0 && marketing.length()>0 && purchasing.length()>0){
            img.setImageDrawable(this.getDrawable(R.drawable.verification_logo));
        }
        else{
            img.setImageDrawable(this.getDrawable(R.drawable.rejected_logo));
        }

    }

    private void setListeners() {

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigator.callPhoneNumber(HospitalActivity.this, hospital.getHospital_phone());
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hospital.getHospital_facebook().equals("")) {
                    CustomToast.darkColor(HospitalActivity.this, CustomToastType.INFO, "No facebook profile provided...");
                }
                else{
                    Navigator.openUrlInBrowser(HospitalActivity.this,hospital.getHospital_facebook());
                }
            }
        });
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hospital.getHospital_website().equals("")) {
                    CustomToast.darkColor(HospitalActivity.this,CustomToastType.INFO,"No Website provided...");
                }
                else{
                    Navigator.openUrlInBrowser(HospitalActivity.this,hospital.getHospital_website());
                }

            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hospital.getHospital_email().equals("")) {
                    CustomToast.darkColor(HospitalActivity.this,CustomToastType.INFO,"No Email provided...");
                }
                else{
                    Navigator.openEmail(HospitalActivity.this,hospital.getHospital_email(),"Sending Email to "+hospital.getHospital_name(),"");
                }
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.hospital_nav_about:
                    selectedFragment = new HospitalAboutFragment(hospital,managers);
                    break;
                case R.id.hospital_nav_location:
                    stopLocationUpdates();
                    selectedFragment = new MapsActivity(hospital,mCurrentLocation);
                    break;
                case R.id.hospital_nav_photos:
                    selectedFragment = new HospitalPhotosFragment(hid);
                    break;
            }

            setFragment(selectedFragment);
            return true;
        }
    };

    private void setFragment(Fragment selectedFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.hospital_frameLayout, selectedFragment).commit();

    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    private void checkLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(this), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(this),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 111) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CustomToast.darkColor(HospitalActivity.this, CustomToastType.ERROR, "Permission denied.");
            }
            else{
                startLocationUpdates();
            }
        }
    }
}
