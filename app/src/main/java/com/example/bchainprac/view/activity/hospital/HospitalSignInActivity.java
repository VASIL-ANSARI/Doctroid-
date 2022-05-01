package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.helpers.Validator;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.SignInForm;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.view.activity.SignInActivity;
import com.example.bchainprac.view.activity.SignUpActivity;
import com.example.bchainprac.view.activity.admin.AdminMainActivity;
import com.example.bchainprac.view.activity.admin.AdminModel;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HospitalSignInActivity extends BaseActivity {


    FirebaseDatabase firebaseDatabase ;
    DatabaseReference reference;
    private FirebaseAuth auth;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    public HospitalSignInActivity(){
        super(R.layout.activity_hospital_sign_in,true);
    }


    EditText email,codeSMS;
    TextView createAccount, errorMessage;
    ImageView errorDialog;
    Button signIn,sendCode;
    ProgressViewDialog progressViewDialog;
    private String code;
    private String verificationID;
    private DatabaseReference ownerRef;
    private List<String> ownerAccepted;


    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarTextView.setText("Sign in for Hospital");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        //FirebaseAuth.getInstance().signOut();
        sharedPreferences=getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        FirebaseApp.initializeApp(HospitalSignInActivity.this);
        firebaseDatabase=FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
        reference=firebaseDatabase.getReference("Hospital");
        ownerRef=firebaseDatabase.getReference("Owners");
        auth=FirebaseAuth.getInstance();
        ownerAccepted=new ArrayList<>();
        fetchOwners();

        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(HospitalSignInActivity.this,HospitalMainActivity.class));
            finish();
        }
        initializeComponents();
        setListeners();
    }

    private void fetchOwners() {
        ownerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ConfirmSignUpForm form=snapshot.getValue(ConfirmSignUpForm.class);
                    if(form.getVerified()){
                        ownerAccepted.add(form.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeComponents() {


        email = findViewById(R.id.hsignIn_email_editText);
        errorMessage = findViewById(R.id.hsignIn_errorMessage_textView);
        errorDialog = findViewById(R.id.hsignIn_errorDialog_imageView);
        codeSMS=findViewById(R.id.hphone_no_edit_txt);

        sendCode=findViewById(R.id.hsend_code);
        signIn = findViewById(R.id.hsignIn_login_button);
        createAccount = findViewById(R.id.hsignIn_createAccount_textView);
    }

    private void setListeners() {
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMSCode();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    progressViewDialog = new ProgressViewDialog(HospitalSignInActivity.this);
                    progressViewDialog.isShowing();
                    progressViewDialog.setDialogCancelable(false);
                    progressViewDialog.setCanceledOnTouchOutside(false);
                    progressViewDialog.showProgressDialog("Checking information");

                    if (validate()) {

                        SignInForm signInForm = new SignInForm(email.getText().toString(),"");
                        signIn(signInForm);

                    } else {
                        progressViewDialog.hideDialog();
                    }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HospitalSignInActivity.this, HospitalSignUpActivity.class));
            }
        });

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            code=credential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.ERROR,"Cannot complete verification");
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId,token);
            verificationID=verificationId;
            CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.INFO,"OTP sent");
        }
    };

    private void sendSMSCode() {
        if(email.getText().toString().trim().equals("")){
            errorDialog.setVisibility(View.VISIBLE);
            errorMessage.setText("Enter valid code");
            email.requestFocus();
        }
        else{
            reference.child(email.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        errorDialog.setVisibility(View.GONE);
                        errorMessage.setText("");
                        Hospital h=dataSnapshot.getValue(Hospital.class);
                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(h.getHospital_phone())       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(HospitalSignInActivity.this)                 // Activity (for callback binding)
                                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                        .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);
                    }
                    else{
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setText("Enter valid code");
                        email.requestFocus();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void signIn(final SignInForm signInForm) {

        reference.child(signInForm.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hospital h=dataSnapshot.getValue(Hospital.class);
                if(h.getVerified()!=null && h.getVerified()==false){
                    if(progressViewDialog.isShowing()){
                        progressViewDialog.hideDialog();
                    }
                    CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.ERROR,"Profile not verified yet...");
                }
                else {
                    errorDialog.setVisibility(View.GONE);
                    errorMessage.setText("");
                    verifyCode(codeSMS.getText().toString(),h);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void verifyCode(String code,Hospital h) {
        if(verificationID==null || code==null){
            CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.ERROR,"Phone  verification not done");
            progressViewDialog.hideDialog();
        }
        else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);
            signinByCredential(credential,h);
        }
    }

    private void signinByCredential(PhoneAuthCredential credential,final Hospital h) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            progressViewDialog.hideDialog();
                            boolean b=sharedPreferences.getBoolean(Constants.HOSPITAL_FIRST_TIME_LOGIN,true);
                            Log.d("message",b+"");
                            if(h.getHospital_itManager().length()==0  || h.getHospital_MarketingManager().length()==0 || h.getHospital_PurchasingManager().length()==0 || h.getHospital_adminstratonManager().length()==0 || h.getHospital_generalManager().length()==0 || b) {
                                email.setText("");
                                codeSMS.setText("");
                                CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.INFO,"Update hospital owners");
                                Intent ownerIntent=new Intent(HospitalSignInActivity.this, AddUserActivity.class);
                                ownerIntent.putExtra("HospitalUid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                startActivity(ownerIntent);
                                FirebaseAuth.getInstance().signOut();
                            }
                            else {
                                if(ownerAccepted.containsAll(new ArrayList<String>(Arrays.asList(new String[]{h.getHospital_MarketingManager(), h.getHospital_adminstratonManager(), h.getHospital_generalManager(), h.getHospital_itManager(), h.getHospital_PurchasingManager()})))){
                                    editor.putString("isLogin","hospital");
                                    editor.commit();
                                    email.setText("");
                                    codeSMS.setText("");
                                    CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.SUCCESS,"Finally signed in...");
                                    startActivity(new Intent(HospitalSignInActivity.this, HospitalMainActivity.class));
                                    finish();
                                }
                                else{
                                    CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.INFO,"Your owner's profile not verified yet. Please verify it asap.");
                                    auth.signOut();
                                }

                            }

                        }
                        else{
                            CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.ERROR,"Failed signing in...");
                            progressViewDialog.hideDialog();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CustomToast.darkColor(HospitalSignInActivity.this,CustomToastType.ERROR,"Failed signing in...");
                progressViewDialog.hideDialog();
            }
        });
    }

    private boolean validate() {

        if (!InternetUtilities.isConnected(HospitalSignInActivity.this)) {
            CustomToast.darkColor(HospitalSignInActivity.this, CustomToastType.NO_INTERNET, "Please check your internet connection!");
            return false;
        } else {
            String code=email.getText().toString();
            if(code.length()==0){
                errorDialog.setVisibility(View.VISIBLE);
                errorMessage.setText("Enter valid code");
                email.requestFocus();
                return false;
            }
            else if(codeSMS.getText().toString().length()==0){
                errorDialog.setVisibility(View.VISIBLE);
                errorMessage.setText("Enter valid otp");
                codeSMS.requestFocus();
                return false;
            }
            else{
                errorDialog.setVisibility(View.INVISIBLE);
                errorMessage.setText("");
                return true;
            }
        }
    }

}
