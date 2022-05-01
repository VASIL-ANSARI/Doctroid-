package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.helpers.Validator;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.view.activity.MainActivity;
import com.example.bchainprac.view.activity.SignUpActivity;
import com.example.bchainprac.view.activity.admin.AdminModel;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HospitalSignUpActivity extends BaseActivity {

    FirebaseDatabase firebaseDatabase ;
    DatabaseReference database;
    private FirebaseAuth auth;
    private double latitude,longitude;
    private String verificationID;
    private Hospital hospital;
    private String code;
    private AutoCompleteTextView ownerTxt;


    private String mLastUpdateTime;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private android.location.LocationListener locationListener;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            code=credential.getSmsCode();
            if(code!=null)
            Log.d("message code",code);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            CustomToast.darkColor(HospitalSignUpActivity.this,CustomToastType.ERROR,"Cannot complete verification");
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId,token);
            verificationID=verificationId;
            Log.d("message verification",verificationId);
            CustomToast.darkColor(HospitalSignUpActivity.this,CustomToastType.INFO,"OTP sent");
            otp.setVisibility(View.VISIBLE);
        }
    };

    public HospitalSignUpActivity(){
        super(R.layout.activity_hospital_sign_up,true);
    }

    EditText name, facebook,website,email, confirmPassword, phone,otp;
    ImageView errorDialog;
    TextView errorMessage,locationTxt;
    Button signUp,getLocation,otpSend;
    ProgressViewDialog progressViewDialog;
    private LocationManager locationManager;

    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarTextView.setText("Sign Up for Hospital");
        toolbarBackImageView.setVisibility(View.VISIBLE);
        auth=FirebaseAuth.getInstance();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!InternetUtilities.isConnected(HospitalSignUpActivity.this)) {
            CustomToast.darkColor(HospitalSignUpActivity.this, CustomToastType.NO_INTERNET, "Please check your internet connection!");
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        initializeComponents();
        setListeners();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            longitude=mCurrentLocation.getLongitude();
            latitude=mCurrentLocation.getLatitude();
            Geocoder geocoder = new Geocoder(HospitalSignUpActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                String loc = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getCountryCode() + " " +
                        addresses.get(0).getCountryName();
                locationTxt.setText(loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("message", "All location settings are satisfied.");
                Toast.makeText(getApplicationContext(), "Started location updates!",
                        Toast.LENGTH_SHORT).show();
                //noinspection MissingPermission
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());
                updateLocationUI();
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
                            rae.startResolutionForResult(HospitalSignUpActivity.this, REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException sie) {
                            Log.d("message", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings.";
                        Log.d("message", errorMessage);
                        Toast.makeText(HospitalSignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
                updateLocationUI();
            }
        });
    }
    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!",
                                Toast.LENGTH_SHORT).show();
                        //toggleButtons();
                    }
                });
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,code);
        signinByCredential(credential);
    }

    private void signinByCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            firebaseDatabase=FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
                            database=firebaseDatabase.getReference("Hospital");
                            //if(check(auth.getCurrentUser().getUid(),database)) {
                                hospital.setHospital_id(auth.getCurrentUser().getUid());
                                database.child(auth.getCurrentUser().getUid()).setValue(hospital).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            List<String> lists = new ArrayList<>();
                                            lists.add("verification");
                                            lists.add(hospital.getHospital_phone());
                                            lists.add(hospital.getHospital_id());
                                            final AdminModel adminModel = new AdminModel(lists, "hospital", hospital.getHospital_id());
                                            firebaseDatabase.getReference("Admin").push().addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    adminModel.setId(dataSnapshot.getKey());
                                                    firebaseDatabase.getReference("Admin").child(adminModel.getId()).setValue(adminModel);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                });
                                //sendLoginCredentials();
                                auth.signOut();
                                CustomToast.darkColor(HospitalSignUpActivity.this, CustomToastType.INFO, "Your account will be activated within 24 hours and login credentials will be sent through SMS");
                                startActivity(new Intent(HospitalSignUpActivity.this, HospitalSignInActivity.class));
                                finish();
//                            }
//                            else{
//                                CustomToast.darkColor(HospitalSignUpActivity.this,CustomToastType.INFO,"Phone no already exists...");
//                            }
                        }
                    }
                });
    }

    private void sendVerificationCode(String phone_string) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone_string)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setListeners() {


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!InternetUtilities.isConnected(HospitalSignUpActivity.this)) {
                    CustomToast.darkColor(HospitalSignUpActivity.this, CustomToastType.NO_INTERNET, "Please check your internet connection!");
                } else {
                    if (name.getText().toString().trim().isEmpty()) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Enter Hospital name");
                        name.requestFocus();
                    }  else if (!Validator.isValidPhoneNumber(phone.getText().toString().trim())) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.phone_not_valid));
                        phone.requestFocus();
                    }
                    else if(locationTxt.getText().toString().trim().length()==0){
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Please provide hospital location");
                    }
                    else if (otp.getText().toString().trim().isEmpty()) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Enter OTP");
                        otp.requestFocus();
                    }
                    else {
                        errorDialog.setVisibility(View.INVISIBLE);
                        errorMessage.setVisibility(View.INVISIBLE);
                        if(!checkWebsite(website.getText().toString())){
                            errorDialog.setVisibility(View.VISIBLE);
                            errorMessage.setVisibility(View.VISIBLE);
                            errorMessage.setText("Enter valid url");
                            website.requestFocus();
                        }
                        else if(!checkFacebook(facebook.getText().toString())){
                            errorDialog.setVisibility(View.VISIBLE);
                            errorMessage.setVisibility(View.VISIBLE);
                            errorMessage.setText("Enter valid facebook url");
                            facebook.requestFocus();

                        }
                        else if(!checkEmail(email.getText().toString())){
                            errorDialog.setVisibility(View.VISIBLE);
                            errorMessage.setVisibility(View.VISIBLE);
                            errorMessage.setText("Enter valid email");
                            email.requestFocus();

                        }
                        else{
                            hospital=new Hospital(name.getText().toString(),locationTxt.getText().toString(),phone.getText().toString(),latitude,longitude,website.getText().toString(),facebook.getText().toString(),email.getText().toString(),"","","","","");
                            hospital.setVerified(false);
                            hospital.setOwnerVerified(0);
                            createNewOne();
                        }

                    }
                }
            }
        });
        otpSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(HospitalSignUpActivity.this,
                        Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HospitalSignUpActivity.this,
                        Manifest.permission.READ_SMS)== PackageManager.PERMISSION_GRANTED) {
                    if(TextUtils.isEmpty(phone.getText().toString())) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.phone_not_valid));
                        phone.requestFocus();
                    }
                    else {
                        final String phone_string=phone.getText().toString();
                        DatabaseReference ref = FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital");

                        ref.orderByChild("hospital_phone").equalTo(phone_string).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    CustomToast.darkColor(HospitalSignUpActivity.this,CustomToastType.INFO,"Phone number already registered...");
                                }
                                else{
                                    sendVerificationCode(phone_string);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }else{
                    ActivityCompat.requestPermissions(HospitalSignUpActivity.this,
                            new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS
                            }, 44);
                }
            }
        });
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(HospitalSignUpActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HospitalSignUpActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED) {


                    startLocationUpdates();
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
                else{
                    ActivityCompat.requestPermissions(HospitalSignUpActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
                }
            }
        });
    }

    private boolean checkWebsite(String toString) {
        if(toString.length()==0 || URLUtil.isValidUrl(toString))
            return true;
        return false;
    }

    private boolean checkFacebook(String toString) {
        if(toString.length()==0 || URLUtil.isValidUrl(toString) && toString.toLowerCase().contains("facebook"))
            return true;
        return false;
    }
    private boolean checkEmail(String toString) {
        if(toString.length()==0 || Validator.isValidEmail(toString.trim())){
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationUpdates();
    }

    private void createNewOne() {
        verifyCode(otp.getText().toString());
    }

    private void initializeComponents() {
        name = findViewById(R.id.hsignUp_firstName_editText);
        email = findViewById(R.id.hsignUp_email_editText);
        facebook=findViewById(R.id.hsignUp_facebook_editText);
        website=findViewById(R.id.hsignUp_website_editText);
        signUp = findViewById(R.id.hsignUp_signUp_button);
        phone = findViewById(R.id.hsignUp_phone_editText);
        errorMessage = findViewById(R.id.hsignUp_errorMessage_textView);
        errorDialog = findViewById(R.id.hsignUp_errorDialog_imageView);
        otp=findViewById(R.id.otpEditTxt);
        locationTxt=findViewById(R.id.txtLocation);
        getLocation=findViewById(R.id.getLocationbtn);
        otpSend=findViewById(R.id.sendOTPbtn);

    }
}
