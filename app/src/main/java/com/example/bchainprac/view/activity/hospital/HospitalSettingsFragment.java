package com.example.bchainprac.view.activity.hospital;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.bchainprac.BuildConfig;
import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.Images;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.view.activity.SelectionActivity;
import com.example.bchainprac.view.fragment.HInfoFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HospitalSettingsFragment extends Fragment {
    public Context context;
    private Button logout,uploadImages,resetPassword,personalInfo;
    private TextView version;
    private User user;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private Hospital hospital;

    ProgressViewDialog progressViewDialog;
    private DatabaseReference databaseReference;
    private List<Images> imagesList;
    private List<String> phoneLists;
    private String code;
    private String verificationID;
    private Map<String, ConfirmSignUpForm> ownerslist;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_hospital_settings, container, false);
        context = getContext();
        ownerslist=new HashMap<>();
        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
        reference=firebaseDatabase.getReference("Hospital");
        progressViewDialog=new ProgressViewDialog(context);
        progressViewDialog.showProgressDialog("Loading ....");
        phoneLists=new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    phoneLists.add(snapshot.getValue(Hospital.class).getHospital_phone());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hospital h=dataSnapshot.getValue(Hospital.class);
                hospital=new Hospital(h.getHospital_name(),h.getHospital_location(),h.getHospital_phone(),h.getLatitude(),h.getLongitude(),h.getHospital_website(),h.getHospital_facebook(),h.getHospital_email(),h.getHospital_generalManager(),h.getHospital_adminstratonManager(),h.getHospital_itManager(),h.getHospital_MarketingManager(),h.getHospital_PurchasingManager());
                initializeComponents(view);
                setListeners();
                fetchImages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void fetchImages() {

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ConfirmSignUpForm form=snapshot.getValue(ConfirmSignUpForm.class);
                    ownerslist.put(form.getId(),form);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void initializeComponents(View view) {

        logout = view.findViewById(R.id.hospital_settingsFragment_Logout_Button);
        version = view.findViewById(R.id.hospital_settingsFragment_version_TV);
        resetPassword = view.findViewById(R.id.hospital_settingFragment_accountSettings);
        personalInfo=view.findViewById(R.id.hospital_settingFragment_personalInformation);
        uploadImages=view.findViewById(R.id.hospital_settingFragment_notification);
    }

    private void setListeners() {


        version.setText(String.format("Version: %s", BuildConfig.VERSION_NAME));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        personalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HInfoFragment hInfoFragment=new HInfoFragment(context,hospital,ownerslist);
                hInfoFragment.show(getFragmentManager(),"HInfo");
            }
        });
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePhone();
            }
        });
        uploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intialize Bundle instance
                Bundle b = new Bundle();
                b.putSerializable("ImagesList", (Serializable) imagesList);
                Intent i = new Intent(context, UploadImage.class);
                i.putExtras(b);
                startActivity(i);
            }
        });

    }

    private EditText codeTxt;
    private android.app.AlertDialog alertDialog;
    private EditText title;

    private void updatePhone() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_phone_no_change, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        title=dialogView.findViewById(R.id.txtnewPhone);
        codeTxt=dialogView.findViewById(R.id.codeTxtId);
        Button otp=dialogView.findViewById(R.id.sendOtpChange);
        Button add=dialogView.findViewById(R.id.acceptPhoneChange);

        alertDialog.show();


        otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().length()==0){
                    Toast.makeText(context,"Enter Phone no",Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                }
                else if(phoneLists.contains(title.getText().toString())){
                    Toast.makeText(context,"Phone no already exists",Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                }
                else{
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(title.getText().toString())       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity((Activity) context)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codeTxt.getText().toString().length()==0){
                    Toast.makeText(context,"Enter otp",Toast.LENGTH_SHORT).show();
                    codeTxt.requestFocus();
                }
                else{
                    progressViewDialog.showProgressDialog("Updating Phone number...");
                    verifyCode(codeTxt.getText().toString());
                }
            }
        });
    }

    private void verifyCode(String toString) {
        if (verificationID==null){
            Log.d("message","null verification id");
        }
        if(code==null){
            Log.d("message","null code");
        }
        if(verificationID==null){
            CustomToast.darkColor(context,CustomToastType.ERROR,"Phone  verification not done");
            progressViewDialog.hideDialog();
        }
        else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, toString);
            FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        reference.child(auth.getCurrentUser().getUid()).child("hospital_phone").setValue(title.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    CustomToast.darkColor(context,CustomToastType.SUCCESS,"Phone no. Updated Successfully!!");
                                    progressViewDialog.hideDialog();
                                    alertDialog.dismiss();

                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            code=credential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            CustomToast.darkColor(context,CustomToastType.ERROR,"Cannot complete verification");
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId,token);
            verificationID=verificationId;
            CustomToast.darkColor(context,CustomToastType.INFO,"OTP sent");
            codeTxt.setVisibility(View.VISIBLE);
        }
    };


    private void showLogoutDialog() {
        SharedPreferences sharedPreferences=context.getSharedPreferences(Constants.SHARED_PREF_NAME,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor=sharedPreferences.edit();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.logout));
        builder.setMessage("Are you sure to logout?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putString("isLogin",null);
                editor.commit();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(context, SelectionActivity.class));
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("message","resume hospital activity");

        imagesList=new ArrayList<>();

        databaseReference= FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Images");
        databaseReference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Images images = dataSnapshot.getValue(Images.class);
                    imagesList.add(images);

                }
                progressViewDialog.hideDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("message","dismiss");
                progressViewDialog.hideDialog();

            }
        });
    }
}
