package com.example.bchainprac.view.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.view.activity.admin.AdminMainActivity;
import com.example.bchainprac.view.activity.hospital.HospitalSignInActivity;

public class SelectionActivity extends AppCompatActivity {

    private Button patient,hospital,admin;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        patient=findViewById(R.id.patient_account_button);
        hospital=findViewById(R.id.hospital_account_button);
        admin=findViewById(R.id.admin_acount_button);

        sharedPreferences=getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);
        String val=sharedPreferences.getString("isLogin",null);
        if(val!=null){
            Log.d("message",val);
            if(val.equals("user")){
                startActivity(new Intent(SelectionActivity.this,SignInActivity.class));
                finish();
            }else if(val.equals("admin")){
                Intent intent=new Intent(SelectionActivity.this, AdminMainActivity.class);
                startActivity(intent);
                finish();
            }else if(val.equals("hospital")){
                startActivity(new Intent(SelectionActivity.this, HospitalSignInActivity.class));
                finish();
            }
        }


        patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectionActivity.this,SignInActivity.class));
                //finish();
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectionActivity.this,SignInActivity.class);
                intent.putExtra("id","admin");
                startActivity(intent);
                //finish();
            }
        });

        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectionActivity.this, HospitalSignInActivity.class));
                //finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
    public void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Are you sure to exit?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SelectionActivity.super.onBackPressed();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("message","restart");
    }
}
