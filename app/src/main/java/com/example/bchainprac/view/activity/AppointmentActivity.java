package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Appoint;
import com.example.bchainprac.network.model.AppointRequest;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.network.model.wallet;
import com.example.bchainprac.presenter.Adapter.AppointAdapter;
import com.example.bchainprac.view.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentActivity extends BaseActivity {

    SwipeRefreshLayout swipeRefresh;

    TextView emptyAppoint;
    LinearLayout searchLinear;
    SearchView searchView;
    RecyclerView appointRecyclerView;
    AppointAdapter appointAdapter;
    List<Pair<Appoint,String>> appoints = new ArrayList<>();
    private String mnemonic=null;

    ProgressViewDialog progressViewDialog;

    public AppointmentActivity() {
        super(R.layout.activity_appointment, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {

        toolbarTextView.setText(R.string.appointment);
        toolbarBackImageView.setVisibility(View.VISIBLE);
        emptyAppoint = findViewById(R.id.appoint_emptyAppoint);
        emptyAppoint.setVisibility(View.GONE);
        initializeComponents();
        setListeners();

        callAPI();
        fetchMnemonic();

    }

    private void fetchMnemonic() {

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
                        //progressViewDialog.hideDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void initializeComponents() {

        swipeRefresh = findViewById(R.id.appoint_SwipeRefresh);

        searchView = findViewById(R.id.appoint_searchView);
        searchLinear = findViewById(R.id.appoint_searchLinear);
        searchView.setVisibility(View.GONE);
        searchLinear.setVisibility(View.GONE);

        appointRecyclerView = findViewById(R.id.appoint_recyclerView);
        appointRecyclerView.setLayoutManager(new LinearLayoutManager(AppointmentActivity.this));
        appointRecyclerView.setHasFixedSize(true);

        AppointAdapter.ItemClick itemClick=new AppointAdapter.ItemClick() {
            @Override
            public void onClick(final int position) {
                Log.d("message",appoints.get(position).first.getStatus());
                if(appoints.get(position).first.getStatus().toLowerCase().equals("pending") || appoints.get(position).first.getStatus().toLowerCase().equals("rejected")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentActivity.this);
                    builder.setTitle("Are you sure to delete this request?");
                    builder.setMessage("This can't be undone.Note that 1 Algo will be charged for cancellation.");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteRequest(appoints.get(position).first.getId(),appoints.get(position));
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    CustomToast.darkColor(AppointmentActivity.this, CustomToastType.INFO,"Accepted appointments cannot be deleted!");
                }
            }
        };
        appointAdapter = new AppointAdapter(AppointmentActivity.this, appoints, itemClick);

        appointRecyclerView.setAdapter(appointAdapter);

    }

    private void deleteRequest(String id, final Pair<Appoint,String> appoint) {
        if(mnemonic==null){
            CustomToast.darkColor(AppointmentActivity.this, CustomToastType.ERROR,"Some error occurred!!");
            return ;
        }
        int amount=(Integer.parseInt(appoint.second)-1)*1000;
        Uri uri = Uri.parse("https://www.example.com/"+mnemonic+"/"+amount+"/"+id+"/"+"reverse");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivityForResult(intent,1055);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1055 && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra("result");
            if(result!=null){
                Log.d("message",result);
                Toast.makeText(AppointmentActivity.this,"Transaction Done!!",Toast.LENGTH_SHORT).show();
                String id=result.split("//")[1];
                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments").child(id).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //appointAdapter.notifyAdapterChanged(appoints);
                            CustomToast.darkColor(AppointmentActivity.this, CustomToastType.INFO,"Appointment deleted.Please refresh if changes not seen");
                        }
                    }
                });

            }
        }
        else{
            Toast.makeText(AppointmentActivity.this,"Transaction Failed!!",Toast.LENGTH_LONG).show();
        }
    }

    private void callAPI() {

        searchView.setVisibility(View.GONE);
        searchLinear.setVisibility(View.GONE);

        progressViewDialog = new ProgressViewDialog(AppointmentActivity.this);
        progressViewDialog.setDialogCancelable(false);
        progressViewDialog.setCanceledOnTouchOutside(false);
        progressViewDialog.showProgressDialog("Getting Appointments information");

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot s:dataSnapshot.getChildren()){
                    final AppointRequest appointRequest=s.getValue(AppointRequest.class);
                    if(appointRequest.getP_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        searchView.setVisibility(View.VISIBLE);
                        searchLinear.setVisibility(View.VISIBLE);
                        final Appoint appoint = new Appoint(s.getKey(), "", appointRequest.getReq_comment(), appointRequest.getReq_date(), appointRequest.getReq_time(), appointRequest.getReq_status(), "empty", "", "");
                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("MedicalCategory").child(appointRequest.getC_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                MedicalCategory medicalCategory = dataSnapshot.getValue(MedicalCategory.class);
                                MedicalAnalysis analysis = medicalCategory.getMedicalAnalyses().get(Integer.parseInt(appointRequest.getT_id()));
                                appoint.setTitle(analysis.getTitle());
                                appoint.setPre_en(analysis.getPrecautions());
                                appoints.add(new Pair<Appoint, String>(appoint,analysis.getPrice()));
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
