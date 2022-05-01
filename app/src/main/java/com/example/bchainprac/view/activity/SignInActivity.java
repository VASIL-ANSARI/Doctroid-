package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.bchainprac.network.model.SignInForm;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.view.activity.admin.AdminMainActivity;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignInActivity extends BaseActivity {

    FirebaseDatabase firebaseDatabase ;
    DatabaseReference reference;
    private FirebaseAuth auth;

    public SignInActivity() {
        super(R.layout.activity_sign_in, true);
    }

    EditText email, password;
    TextView createAccount, errorMessage,txtDont,txtPassword,resetPassword;
    ImageView errorDialog;
    Button signIn;
    ProgressViewDialog progressViewDialog;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    String id=null;

    @Override
    protected void doOnCreate(Bundle bundle) {

        sharedPreferences=getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);
        editor=sharedPreferences.edit();

        initializeComponents();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        String sstr=getIntent().getStringExtra("choice");
        id=getIntent().getStringExtra("id");
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (sstr==null && firebaseUser != null && id==null && firebaseUser.isEmailVerified()) {
            navigateToMain();
        }
    }

    private void initializeComponents() {
        id=getIntent().getStringExtra("id");
        email = findViewById(R.id.signIn_email_editText);
        password = findViewById(R.id.signIn_password_editText);
        errorMessage = findViewById(R.id.signIn_errorMessage_textView);
        errorDialog = findViewById(R.id.signIn_errorDialog_imageView);
        txtDont=findViewById(R.id.doNotHaveAcc_textView);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        signIn = findViewById(R.id.signIn_login_button);
        createAccount = findViewById(R.id.signIn_createAccount_textView);
        txtPassword=findViewById(R.id.dont_remember_password_textView);
        resetPassword=findViewById(R.id.dont_remember_password_link);

        if(id!=null && id.equals("admin")){
            toolbarTextView.setText("Admin");
            createAccount.setVisibility(View.GONE);
            txtDont.setVisibility(View.GONE);
            txtPassword.setVisibility(View.GONE);
            resetPassword.setVisibility(View.GONE);
        }
        else{
            toolbarTextView.setText("Sign in for User");
        }
    }

    private void setListeners() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id != null && id.equals("admin")) {
                    if (email.getText().toString().toLowerCase().trim().equals("admin@doc.com") && password.getText().toString().trim().equals("ADMIN")) {
                        editor.putString("isLogin","admin");
                        editor.commit();
                        Intent intent = new Intent(SignInActivity.this, AdminMainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        CustomToast.darkColor(SignInActivity.this, CustomToastType.ERROR, "Unauthorised Access!!");
                    }
                } else {
                    progressViewDialog = new ProgressViewDialog(SignInActivity.this);
                    progressViewDialog.isShowing();
                    progressViewDialog.setDialogCancelable(false);
                    progressViewDialog.setCanceledOnTouchOutside(false);
                    progressViewDialog.showProgressDialog("Checking information");

                    if (validate()) {

                        SignInForm signInForm = new SignInForm(email.getText().toString().toLowerCase().trim(), password.getText().toString().trim());
                        signIn(signInForm);

                    } else {
                        progressViewDialog.hideDialog();
                    }
                }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Validator.isValidEmail(email.getText().toString())) {
                    errorDialog.setVisibility(View.VISIBLE);
                    errorMessage.setText("Enter email linked with your account.");
                    email.requestFocus();
                }
                else {
                    resetPassword(email.getText().toString());
                }
            }
        });

    }
    protected final void resetPassword(final String emailId) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setTitle("Reset Password");
        builder.setMessage("Are you sure ? Reset password link will be sent to your registered email id.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailId)

                        .addOnCompleteListener(new OnCompleteListener() {

                            @Override

                            public void onComplete(@NonNull Task task) {

                                if (task.isSuccessful()) {
                                    CustomToast.darkColor(SignInActivity.this, CustomToastType.SUCCESS,"Link sent to your registered email id");
                                } else {
                                    CustomToast.darkColor(SignInActivity.this,CustomToastType.ERROR,"Error sending link!");
                                }

                            }

                        });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean validate() {

        if (!InternetUtilities.isConnected(SignInActivity.this)) {
            CustomToast.darkColor(SignInActivity.this, CustomToastType.NO_INTERNET, "Please check your internet connection!");
            return false;
        } else {
            if (!Validator.isValidEmail(email.getText().toString()) && password.getText().toString().trim().length() < 6) {
                errorDialog.setVisibility(View.VISIBLE);
                errorMessage.setText(getString((R.string.email_and_password_not_valid)));
                email.requestFocus();
                return false;
            }

            if (!Validator.isValidEmail(email.getText().toString()) && password.getText().toString().trim().length() >= 6) {
                errorDialog.setVisibility(View.VISIBLE);
                errorMessage.setText(getString(R.string.email_not_valid));
                email.requestFocus();
                return false;
            }

            if (Validator.isValidEmail(email.getText().toString()) && password.getText().toString().trim().length() < 6) {
                errorDialog.setVisibility(View.VISIBLE);
                errorMessage.setText(getString(R.string.password_not_valid));
                password.requestFocus();
                return false;
            }

            if (Validator.isValidEmail(email.getText().toString()) && password.getText().toString().trim().length() >= 6) {
                errorDialog.setVisibility(View.INVISIBLE);
                errorMessage.setText("");
                return true;
            }
        }
        return false;
    }

    private void signIn(final SignInForm signInForm) {

        firebaseDatabase = FirebaseDatabase.getInstance("https://doctroid-app-default-rtdb.firebaseio.com/");
        reference =firebaseDatabase.getReference("User");
        //authenticate user
        auth.signInWithEmailAndPassword(signInForm.getEmail(), signInForm.getPassword())
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // there was an error
                            CustomToast.darkColor(SignInActivity.this, CustomToastType.ERROR, "Error!");
                            progressViewDialog.hideDialog();
                        } else {
                            if(auth.getCurrentUser().isEmailVerified()) {
                                editor.putString("isLogin","user");
                                editor.commit();
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.putExtra("uid", auth.getCurrentUser().getUid());
                                startActivity(intent);
                                finish();
                            }
                            else{
                                CustomToast.darkColor(SignInActivity.this, CustomToastType.ERROR, "Email not verified!");
                                progressViewDialog.hideDialog();
                            }
                        }
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("uid",auth.getCurrentUser().getUid());
        startActivity(intent);
        finish();
    }

}
