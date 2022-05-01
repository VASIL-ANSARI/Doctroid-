package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.network.model.wallet;
import com.example.bchainprac.presenter.Adapter.MedicalCategoryAdapter;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalActivity extends BaseActivity {

    LinearLayout searchLinear;
    SearchView searchView;
    RecyclerView medicalRecyclerView;
    MedicalCategoryAdapter medicalCategoryAdapter;
    List<MedicalCategory> medicalCategories = new ArrayList<>();

    ProgressViewDialog progressViewDialog;
    String hid=null;

    public MedicalActivity() {
        super(R.layout.activity_medical, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {

        toolbarTextView.setText(R.string.medical_analysis);
        toolbarBackImageView.setVisibility(View.VISIBLE);

        hid=getIntent().getStringExtra("IdMedical");

        initializeComponents();
        setListeners();
        callAPI();
    }

    private void initializeComponents() {

        searchView = findViewById(R.id.medical_searchView);
        searchLinear = findViewById(R.id.medical_searchLinear);

        medicalRecyclerView = findViewById(R.id.medical_recyclerView);
        medicalRecyclerView.setLayoutManager(new LinearLayoutManager(MedicalActivity.this));
        medicalRecyclerView.setHasFixedSize(true);

        MedicalCategoryAdapter.ItemClick itemClick=new MedicalCategoryAdapter.ItemClick() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MedicalActivity.this, MedicalAnalysisActivity.class);
                intent.putExtra("medicalAnalysis", (Serializable) medicalCategories.get(position).getMedicalAnalyses());
                intent.putExtra("categoryName", medicalCategories.get(position).getName());
//                intent.putExtra("account",mnemonic);
                intent.putExtra("c_id", medicalCategories.get(position).getId());
                intent.putExtra("h_id",hid);
                startActivity(intent);
            }
        };

        medicalCategoryAdapter = new MedicalCategoryAdapter(MedicalActivity.this, medicalCategories,itemClick);
        medicalRecyclerView.setAdapter(medicalCategoryAdapter);

    }

    private void callAPI() {
        progressViewDialog = new ProgressViewDialog(MedicalActivity.this);
        progressViewDialog.isShowing();
        progressViewDialog.setDialogCancelable(false);
        progressViewDialog.setCanceledOnTouchOutside(false);
        progressViewDialog.showProgressDialog("Getting medicals information");

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("MedicalCategory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    MedicalCategory medicalCategory=snapshot.getValue(MedicalCategory.class);
                    if(hid!=null){
                        if(medicalCategory.getHospitalId().equals(hid)){
                            searchLinear.setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.VISIBLE);
                            medicalCategories.add(medicalCategory);
                            medicalCategoryAdapter.notifyAdapterDataSetChanged(medicalCategories);
                        }
                    }
//                    else {
//                        medicalCategories.add(medicalCategory);
//                        medicalCategoryAdapter.notifyDataSetChanged();
//                    }
                }
                progressViewDialog.hideDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setListeners() {

        searchLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setFocusable(true);
                searchView.setIconified(false);
                searchView.requestFocusFromTouch();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                medicalCategoryAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
