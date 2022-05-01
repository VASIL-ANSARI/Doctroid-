package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.App;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.helpers.Validator;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    public SignUpActivity() {
        super(R.layout.activity_sign_up, true);
    }

    EditText firstName, lastName, email, password, confirmPassword, phone;
    ImageView errorDialog, male, female;
    TextView errorMessage;
    Button signUp;
    ProgressViewDialog progressViewDialog;
    boolean maleSelected = false;
    boolean femaleSelected = false;


    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarTextView.setText("Sign Up For User");
        toolbarBackImageView.setVisibility(View.VISIBLE);

        initializeComponents();
        setListeners();
    }

    private void initializeComponents() {


        firstName = findViewById(R.id.signUp_firstName_editText);
        lastName = findViewById(R.id.signUp_lastName_editText);
        email = findViewById(R.id.signUp_email_editText);
        password = findViewById(R.id.signUp_password_editText);
        confirmPassword = findViewById(R.id.signUp_confirmPassword_editText);
        male = findViewById(R.id.signUp_male_imageView);
        female = findViewById(R.id.signUp_female_imageView);
        signUp = findViewById(R.id.signUp_signUp_button);
        phone = findViewById(R.id.signUp_phone_editText);
        errorMessage = findViewById(R.id.signUp_errorMessage_textView);
        errorDialog = findViewById(R.id.signUp_errorDialog_imageView);

    }

    private void setListeners() {

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleSelected = true;
                femaleSelected = false;
                male.setImageDrawable(getResources().getDrawable(R.drawable.signup_male_selected));
                female.setImageDrawable(getResources().getDrawable(R.drawable.signup_female_deselected));
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleSelected = false;
                femaleSelected = true;
                male.setImageDrawable(getResources().getDrawable(R.drawable.signup_male_deselected));
                female.setImageDrawable(getResources().getDrawable(R.drawable.signup_female_selected));
            }
        });


        signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!InternetUtilities.isConnected(SignUpActivity.this)) {
                    CustomToast.darkColor(SignUpActivity.this, CustomToastType.NO_INTERNET, "Please check your internet connection!");
                } else {
                    if (firstName.getText().toString().trim().isEmpty()) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Enter your First name");
                        firstName.requestFocus();
                    } else if (lastName.getText().toString().trim().isEmpty()) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Enter your Last name");
                        lastName.requestFocus();
                    } else if (!Validator.isValidEmail(email.getText().toString().trim())) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.email_not_valid));
                        email.requestFocus();
                    } else if (!Validator.isValidPhoneNumber(phone.getText().toString().trim())) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.phone_not_valid));
                        phone.requestFocus();
                    } else if (!Validator.isConfirmPassMatchPass(password.getText().toString().trim(),
                            confirmPassword.getText().toString().trim())) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.password_not_valid));
                        password.requestFocus();
                    } else if (!maleSelected && !femaleSelected) {
                        errorDialog.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText("Please select a gender");
                    } else {
                        errorDialog.setVisibility(View.INVISIBLE);
                        errorMessage.setVisibility(View.INVISIBLE);
                    }

                    if (Validator.registerValidation(SignUpActivity.this, firstName, lastName,
                            email, password, confirmPassword, phone)) {
                        if (maleSelected || femaleSelected) {
                            createNewUser();
                        }
                    }
                }
            }
        });

    }

    private void createNewUser(){

        String firstNameSTR = firstName.getText().toString().trim();
        String lastNameSTR = lastName.getText().toString().trim();
        String emailSTR = email.getText().toString().toLowerCase().trim();
        String phoneSTR = phone.getText().toString().trim();
        String passwordSTR = password.getText().toString().trim();
        String genderSTR;

        if (maleSelected) {
            genderSTR = "Male";
        } else if (femaleSelected) {
            genderSTR = "Female";
        } else {
            genderSTR = "Not assigned";
        }

        progressViewDialog = new ProgressViewDialog(this);
        progressViewDialog.isShowing();
        progressViewDialog.setDialogCancelable(false);
        progressViewDialog.setCanceledOnTouchOutside(false);
        progressViewDialog.showProgressDialog("Creating new account");

        User user = new User(firstNameSTR, lastNameSTR,
                emailSTR, phoneSTR, passwordSTR, genderSTR, false);

        signUp(user);
    }

    private void signUp(final User user){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://doctroid-app-default-rtdb.firebaseio.com/");
        myRef = database.getReference("User");
        auth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword())
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            CustomToast.darkColor(SignUpActivity.this, CustomToastType.ERROR, "Error!");
                            progressViewDialog.hideDialog();
                        }
                        else {
                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d("message",auth.getCurrentUser().getUid());
//                                    if (snapshot.hasChild(auth.getCurrentUser().getUid())){
//                                        CustomToast.darkColor(SignUpActivity.this, CustomToastType.ERROR, "User Already exists.");
//                                    }else {
                                        myRef.child(auth.getCurrentUser().getUid()).setValue(user);
                                        verifyEmailIdSentEmail(auth.getCurrentUser());
                                        progressViewDialog.hideDialog();
                                        Intent intent=new Intent(SignUpActivity.this, SignInActivity.class);
                                        intent.putExtra("choice","first");
                                        startActivity(intent);
                                        finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError t) {
                                    CustomToast.darkColor(SignUpActivity.this, CustomToastType.ERROR,
                                            Objects.requireNonNull(t.getMessage()));
                                }
                            });
                        }
                    }
                });
    }
    protected final void verifyEmailIdSentEmail( FirebaseUser FirebaseUser){

        FirebaseUser.sendEmailVerification()

                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override

                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            CustomToast.darkColor(SignUpActivity.this, CustomToastType.SUCCESS, "Account Created! Verification link sent to your mail.");
                        }

                        else
                        {
                            CustomToast.darkColor(SignUpActivity.this, CustomToastType.ERROR, "Email verification failed");

                        }

                    }

                }).addOnCanceledListener(new OnCanceledListener() {

            @Override

            public void onCanceled() {
                CustomToast.darkColor(SignUpActivity.this, CustomToastType.ERROR, "Email verification failed");
            }

        });

    }

}
