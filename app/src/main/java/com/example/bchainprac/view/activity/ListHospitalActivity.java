package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.presenter.Adapter.HospitalActivityListAdapter;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListHospitalActivity extends BaseActivity {

    public ListHospitalActivity(){
        super(R.layout.activity_list_hospital,true);
    }


    RecyclerView recyclerView;
    private List<Pair<Hospital,String>> hospitalList;
    private Map<String,ConfirmSignUpForm> ownerslist;
    private ProgressViewDialog progressViewDialog;
    private HospitalActivityListAdapter adapter;

    @Override
    protected void doOnCreate(Bundle bundle) {

        toolbarBackImageView.setVisibility(View.VISIBLE);
        toolbarTextView.setText("Select hospital from List");

        String activity=getIntent().getStringExtra("activity");

        hospitalList=new ArrayList<>();
        ownerslist=new HashMap<>();
        recyclerView=findViewById(R.id.recyclerViewListId);

        progressViewDialog=new ProgressViewDialog(ListHospitalActivity.this);
        progressViewDialog.showProgressDialog("Loading...");

        if(activity!=null && activity.equals("details")){
            HospitalActivityListAdapter.ItemClick itemClick=new HospitalActivityListAdapter.ItemClick() {
                @Override
                public void onClick(int position) {
                    Hospital h=hospitalList.get(position).first;
                    String id=hospitalList.get(position).second;
                    Intent intent=new Intent(ListHospitalActivity.this,HospitalActivity.class);
                    intent.putExtra("nameHospital",h.getHospital_name());
                    intent.putExtra("phoneHospital",h.getHospital_phone());
                    intent.putExtra("Loc",h.getHospital_location());
                    intent.putExtra("Latitude",h.getLatitude());
                    intent.putExtra("Longitude",h.getLongitude());
                    intent.putExtra("Id",id);
                    intent.putExtra("facebookUrl",h.getHospital_facebook());
                    intent.putExtra("websiteUrl",h.getHospital_website());
                    intent.putExtra("emailId",h.getHospital_email());
                    intent.putExtra("ITmanager",ownerslist.get(h.getHospital_itManager()).getName());
                    intent.putExtra("Adminmanager",ownerslist.get(h.getHospital_adminstratonManager()).getName());
                    intent.putExtra("Generalmanager",ownerslist.get(h.getHospital_generalManager()).getName());
                    intent.putExtra("Marketingmanager",ownerslist.get(h.getHospital_MarketingManager()).getName());
                    intent.putExtra("Purchasingmanager",ownerslist.get(h.getHospital_PurchasingManager()).getName());
                    intent.putExtra("verificationId",h.getVerified());
                    startActivity(intent);
                }
            };

            adapter=new HospitalActivityListAdapter(ListHospitalActivity.this,hospitalList,itemClick);
            recyclerView.setAdapter(adapter);
        }
        else if(activity!=null && activity.equals("analysis")){
            HospitalActivityListAdapter.ItemClick itemClick=new HospitalActivityListAdapter.ItemClick() {
                @Override
                public void onClick(int position) {
                    String id=hospitalList.get(position).second;
                    Intent intent=new Intent(ListHospitalActivity.this,MedicalActivity.class);
                    intent.putExtra("IdMedical",id);
                    startActivity(intent);
                }
            };
            adapter=new HospitalActivityListAdapter(ListHospitalActivity.this,hospitalList,itemClick);
            recyclerView.setAdapter(adapter);
        }

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hospitalList.clear();
                for(final DataSnapshot snapshot:dataSnapshot.getChildren()){
                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Hospital hospital=dataSnapshot.getValue(Hospital.class);
                            Log.d("message",dataSnapshot.getKey()+" "+hospital.getHospital_name());
                            hospitalList.add(new Pair<Hospital, String>(hospital,snapshot.getKey()));
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
                    progressViewDialog.hideDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
}
