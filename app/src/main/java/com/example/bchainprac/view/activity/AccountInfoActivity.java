package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.utilities.PrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AccountInfoActivity extends AppCompatActivity {

    SwitchCompat switchCompat;
    AppCompatButton resetpassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        switchCompat=findViewById(R.id.switch_push_notifications);
        resetpassword=findViewById(R.id.btn_reset);

        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);

        final SharedPreferences.Editor myEdit = sharedPreferences.edit();

        final String notKey=Constants.PREF_NOT+" "+FirebaseAuth.getInstance().getCurrentUser().getUid();
        Boolean b=sharedPreferences.getBoolean(notKey,false);
        switchCompat.setChecked(b);


        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myEdit.putBoolean(notKey,isChecked);
                myEdit.commit();
            }
        });

        resetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });

    }

    protected final void resetPassword(final String emailId) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(AccountInfoActivity.this);
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
                                    CustomToast.darkColor(AccountInfoActivity.this, CustomToastType.SUCCESS,"Link sent to your registered email id");
                                } else {
                                    CustomToast.darkColor(AccountInfoActivity.this,CustomToastType.ERROR,"Error sending link!");
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
}
