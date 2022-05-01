package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Appoint;
import com.example.bchainprac.network.model.AppointRequest;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.presenter.Adapter.AppointAdapter;
import com.example.bchainprac.presenter.Adapter.HospitalAppointAdapter;
import com.example.bchainprac.view.activity.AppointmentActivity;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HospitalViewAppointment extends BaseActivity {

    SwipeRefreshLayout swipeRefresh;

    TextView emptyAppoint;
    LinearLayout searchLinear;
    SearchView searchView;
    RecyclerView appointRecyclerView;
    HospitalAppointAdapter appointAdapter;
    List<Appoint> appoints = new ArrayList<>();

    ProgressViewDialog progressViewDialog;

    public HospitalViewAppointment(){
        super(R.layout.activity_hospital_view_appointment,true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarTextView.setText(R.string.appointment);
        toolbarBackImageView.setVisibility(View.VISIBLE);
        emptyAppoint = findViewById(R.id.appoint_emptyAppoint_h);
        initializeComponents();
        setListeners();

        callAPI();
    }

    private void initializeComponents() {

        swipeRefresh = findViewById(R.id.appoint_SwipeRefresh_h);

        searchView = findViewById(R.id.appoint_searchView_h);
        searchLinear = findViewById(R.id.appoint_searchLinear_h);

        appointRecyclerView = findViewById(R.id.appoint_recyclerView_h);
        appointRecyclerView.setLayoutManager(new LinearLayoutManager(HospitalViewAppointment.this));
        appointRecyclerView.setHasFixedSize(true);

        AppointAdapter.ItemClick itemClick=new AppointAdapter.ItemClick() {
            @Override
            public void onClick(final int position) {
                AlertDialog.Builder builder=new AlertDialog.Builder(HospitalViewAppointment.this);
                builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdateRequest(appoints.get(position).getId(),"accepted");
                    }
                });
                builder.setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdateRequest(appoints.get(position).getId(),"rejected");
                    }
                });
                builder.setTitle("Do you want to accept or reject ?");
                builder.create().show();
                Log.d("message",appoints.get(position).getStatus());

            }
        };
        appointAdapter = new HospitalAppointAdapter(HospitalViewAppointment.this, appoints, itemClick);

        appointRecyclerView.setAdapter(appointAdapter);

    }

    private void UpdateRequest(final String id,final String msg) {
        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AppointRequest request=dataSnapshot.getValue(AppointRequest.class);
                request.setReq_status(msg);
                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments").child(id).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            callAPI();
                            if(msg.equals("accepted")) {
                                CustomToast.darkColor(HospitalViewAppointment.this, CustomToastType.SUCCESS, "Appointment " + msg);
                            }
                            if(msg.equals("rejected")){
                                CustomToast.darkColor(HospitalViewAppointment.this, CustomToastType.ERROR, "Appointment " + msg);
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void callAPI() {

        appoints.clear();
        emptyAppoint.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
        searchLinear.setVisibility(View.GONE);
        progressViewDialog = new ProgressViewDialog(HospitalViewAppointment.this);
        progressViewDialog.setDialogCancelable(false);
        progressViewDialog.setCanceledOnTouchOutside(false);
        progressViewDialog.showProgressDialog("Getting Appointments information");

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appoints.clear();
                for(DataSnapshot s:dataSnapshot.getChildren()){
                    final AppointRequest appointRequest=s.getValue(AppointRequest.class);
                    if(appointRequest.getH_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        final Appoint appoint = new Appoint(s.getKey(), "", appointRequest.getReq_comment(), appointRequest.getReq_date(), appointRequest.getReq_time(), appointRequest.getReq_status(), "empty", "", "");
                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("MedicalCategory").child(appointRequest.getC_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                MedicalCategory medicalCategory = dataSnapshot.getValue(MedicalCategory.class);
                                MedicalAnalysis analysis = medicalCategory.getMedicalAnalyses().get(Integer.parseInt(appointRequest.getT_id()));
                                appoint.setTitle(analysis.getTitle());
                                appoint.setPre_en(analysis.getPrecautions());
                                appoints.add(appoint);
                                if(appoints.size()>0){
                                    searchView.setVisibility(View.VISIBLE);
                                    searchLinear.setVisibility(View.VISIBLE);
                                    emptyAppoint.setVisibility(View.GONE);
                                }
                                appointAdapter.notifyAdapterChanged(appoints);
                                progressViewDialog.hideDialog();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        progressViewDialog.hideDialog();

    }


    private void setListeners() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!appoints.isEmpty()) {
                    appoints.clear();
                    appointAdapter.notifyAdapterChanged(appoints);
                }
                callAPI();
                swipeRefresh.setRefreshing(false);
            }
        });

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
                appointAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
