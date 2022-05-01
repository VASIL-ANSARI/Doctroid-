package com.example.bchainprac.view.activity.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.view.activity.SelectionActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class AdminMainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<AdminModel> adminModelList;
    AdminRecylerViewAdapter adminRecyclerView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FloatingActionButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        sharedPreferences=getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);
        editor=sharedPreferences.edit();

        StrictMode.ThreadPolicy st = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(st);

        recyclerView=findViewById(R.id.recyclerViewAdmin);
        btn=findViewById(R.id.btnLogout);
        adminModelList= new ArrayList<>();
        adminRecyclerView=new AdminRecylerViewAdapter(adminModelList,getApplicationContext());
        recyclerView.setAdapter(adminRecyclerView);

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").orderByChild("done").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adminModelList.clear();
                for(DataSnapshot s:dataSnapshot.getChildren()){
                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").child(s.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            AdminModel adminModel=dataSnapshot.getValue(AdminModel.class);
                            adminModelList.add(adminModel);
                            adminRecyclerView.notifyDataSetChanged();
//                            Collections.sort(adminModelList, new Comparator<AdminModel>() {
//                                @Override
//                                public int compare(AdminModel o1, AdminModel o2) {
//                                    return Boolean.compare(o1.isDone(),o2.isDone());
//                                }
//                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminMainActivity.this);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage("Are you sure to Logout?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("isLogin",null);
                        editor.commit();
                        startActivity(new Intent(AdminMainActivity.this, SelectionActivity.class));
                        finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
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
                AdminMainActivity.super.onBackPressed();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
