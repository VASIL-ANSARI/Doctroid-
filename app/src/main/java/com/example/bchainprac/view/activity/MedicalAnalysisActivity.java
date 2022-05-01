package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.network.model.wallet;
import com.example.bchainprac.presenter.Adapter.MedicalAnalysisAdapter;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.BottomSheetFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MedicalAnalysisActivity extends BaseActivity {
    androidx.appcompat.widget.SearchView searchView;
    RecyclerView recyclerView;
    List<MedicalAnalysis> medicalAnalyses = new ArrayList<>();
    MedicalAnalysisAdapter medicalAnalysisAdapter;
    private String mnemonic;

    public MedicalAnalysisActivity() {
        super(R.layout.activity_medical_analysis, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarBackImageView.setVisibility(View.VISIBLE);
        mnemonic=null;

        fetchMnemonic();
        initializeComponents();
        setListeners();
    }

    private void fetchMnemonic() {

        final ProgressViewDialog progressViewDialog=new ProgressViewDialog(MedicalAnalysisActivity.this);
        progressViewDialog.showProgressDialog("Loading...");

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Wallet").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            final wallet w = dataSnapshot.getValue(wallet.class);
                            FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User u=dataSnapshot.getValue(User.class);
                                    if(u.getWalletStatus()==2){
                                        mnemonic = w.getMenomic();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        progressViewDialog.hideDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initializeComponents() {

        searchView = findViewById(R.id.medicalAnalysis_searchView);

        medicalAnalyses = (List<MedicalAnalysis>) getIntent().getSerializableExtra("medicalAnalysis");
        String categoryName = getIntent().getStringExtra("categoryName");
        toolbarTextView.setText(categoryName);

        if(medicalAnalyses==null){
            searchView.setVisibility(View.GONE);
            medicalAnalyses=new ArrayList<>();
        }

        recyclerView = findViewById(R.id.medicalAnalysis_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MedicalAnalysisActivity.this));
        recyclerView.setHasFixedSize(true);

        MedicalAnalysisAdapter.ItemClick itemClick=new MedicalAnalysisAdapter.ItemClick() {
            @Override
            public void onClick(int position) {
                MedicalAnalysis m1;
                m1 = medicalAnalyses.get(position);

                String c_id = getIntent().getStringExtra("c_id");
                String h_id=getIntent().getStringExtra("h_id");
                if(mnemonic==null){
                    CustomToast.darkColor(MedicalAnalysisActivity.this, CustomToastType.INFO, "No wallet attached to your account.Go to wallet settings to activate it.");
                }
                else{
                    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(m1, c_id, getApplicationContext(), mnemonic,h_id);
                    bottomSheetFragment.show(getSupportFragmentManager(), "Appoint");
                }
                   // Log.d("message",mnemonic);

            }
        };

        medicalAnalysisAdapter = new MedicalAnalysisAdapter(MedicalAnalysisActivity.this,
                medicalAnalyses,itemClick);

        recyclerView.setAdapter(medicalAnalysisAdapter);


    }

    private void setListeners() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                medicalAnalysisAdapter.getFilter().filter(newText);
                return false;
            }
        });

    }
}
