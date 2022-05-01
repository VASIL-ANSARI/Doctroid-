package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.presenter.Adapter.MedicalAnalysisAdapter;
import com.example.bchainprac.view.activity.MedicalAnalysisActivity;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.BottomSheetFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalMedicalAnalysisActivity extends BaseActivity {

    RecyclerView recyclerView;
    List<MedicalAnalysis> medicalAnalyses = new ArrayList<>();
    HospitallMedicalAnalysisAdapter medicalAnalysisAdapter;
    ImageButton imageButton;
    private String c_id;

    public HospitalMedicalAnalysisActivity(){
        super(R.layout.activity_hospital_medical_analysis,true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {
        recyclerView=findViewById(R.id.hospital_medicalAnalysis_recyclerView);
        imageButton=findViewById(R.id.hospital_medical_analysis_add_btn);

        medicalAnalyses = (List<MedicalAnalysis>) getIntent().getSerializableExtra("medicalAnalysis");
        String categoryName = getIntent().getStringExtra("categoryName");
        c_id = getIntent().getStringExtra("c_id");

        if(medicalAnalyses==null){
            medicalAnalyses=new ArrayList<>();
        }

        toolbarTextView.setText(categoryName);

        recyclerView.setLayoutManager(new LinearLayoutManager(HospitalMedicalAnalysisActivity.this));
        recyclerView.setHasFixedSize(true);


        medicalAnalysisAdapter = new HospitallMedicalAnalysisAdapter(HospitalMedicalAnalysisActivity.this,
                medicalAnalyses);

        recyclerView.setAdapter(medicalAnalysisAdapter);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedicalAnalysisCategory();
            }
        });
    }

    ProgressViewDialog progressViewDialog;
    private void addMedicalAnalysisCategory() {
        progressViewDialog=new ProgressViewDialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(HospitalMedicalAnalysisActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_another_dialog_box, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        final EditText title=dialogView.findViewById(R.id.test_title);
        final EditText description =dialogView.findViewById(R.id.test_precautions);
        final EditText price=dialogView.findViewById(R.id.test_price);
        final EditText duration=dialogView.findViewById(R.id.test_duration);
        Button add=dialogView.findViewById(R.id.test_add_btn);

        alertDialog.show();


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalAnalysisActivity.this,"Enter Title",Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                }
                else if(description.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalAnalysisActivity.this,"Enter Precautions",Toast.LENGTH_SHORT).show();
                    description.requestFocus();
                }
                else if(price.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalAnalysisActivity.this,"Enter price",Toast.LENGTH_SHORT).show();
                    price.requestFocus();
                }
                else if(duration.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalAnalysisActivity.this,"Enter duration",Toast.LENGTH_SHORT).show();
                    duration.requestFocus();
                }
                else{
                    progressViewDialog.showProgressDialog("Updating UI Please Wait...");
                    final MedicalAnalysis medicalAnalysis=new MedicalAnalysis("",title.getText().toString(),duration.getText().toString(),price.getText().toString(),description.getText().toString());
                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("MedicalCategory").child(c_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            MedicalCategory medicalCategory=dataSnapshot.getValue(MedicalCategory.class);
                            List<MedicalAnalysis> list=medicalCategory.getMedicalAnalyses();
                            if(list==null){
                                list=new ArrayList<>();
                            }
                            medicalAnalysis.setId(String.valueOf(list.size()));
                            list.add(medicalAnalysis);
                            medicalCategory.setMedicalAnalyses(list);
                            Map<String,Object> hashMap=new HashMap<>();
                            hashMap.put("medicalAnalyses",list);

                            FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("MedicalCategory").child(c_id).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    medicalAnalyses.add(medicalAnalysis);
                                    medicalAnalysisAdapter.notifyDataSetChanged();
                                    progressViewDialog.hideDialog();
                                    alertDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressViewDialog.hideDialog();
                                    alertDialog.dismiss();
                                    CustomToast.darkColor(HospitalMedicalAnalysisActivity.this, CustomToastType.INFO,"Record not saved to database.Try again later!!");
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

    }

}
