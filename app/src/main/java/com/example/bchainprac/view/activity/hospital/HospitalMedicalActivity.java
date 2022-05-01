package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.Images;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.presenter.Adapter.MedicalCategoryAdapter;
import com.example.bchainprac.view.activity.MainActivity;
import com.example.bchainprac.view.activity.MedicalActivity;
import com.example.bchainprac.view.activity.MedicalAnalysisActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HospitalMedicalActivity extends AppCompatActivity {

    ImageButton img;
    RecyclerView recyclerView;
    private List<MedicalCategory> list;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private MedicalCategoryAdapter adapter;
    private StorageReference media_storage_ref;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_medical);

        img=findViewById(R.id.hospital_medical_add_btn);
        recyclerView=findViewById(R.id.recyclerView_hospital_medical);

        database=FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
        ref=database.getReference("MedicalCategory");

        img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addMedicalCategory();
            }
        });

        MedicalCategoryAdapter.ItemClick itemClick=new MedicalCategoryAdapter.ItemClick() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(HospitalMedicalActivity.this, HospitalMedicalAnalysisActivity.class);
                intent.putExtra("medicalAnalysis", (Serializable) list.get(position).getMedicalAnalyses());
                intent.putExtra("categoryName", list.get(position).getName());
                intent.putExtra("c_id", list.get(position).getId());
                startActivity(intent);
            }
        };

        list=new ArrayList<>();

        adapter=new MedicalCategoryAdapter(HospitalMedicalActivity.this,list,itemClick);
        recyclerView.setAdapter(adapter);

        user= FirebaseAuth.getInstance().getCurrentUser();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    MedicalCategory category=snapshot.getValue(MedicalCategory.class);
                    if(category.getHospitalId().equals(user.getUid())) {
                        list.add(category);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    ImageView imgView;
    String imgUrl;

    ProgressViewDialog progressViewDialog;
    private void addMedicalCategory() {
        progressViewDialog=new ProgressViewDialog(this);
        media_storage_ref= FirebaseStorage.getInstance("gs://doctroid-app.appspot.com").getReference();
        AlertDialog.Builder builder = new AlertDialog.Builder(HospitalMedicalActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dialog_box, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        imgUrl=null;

        final EditText title=dialogView.findViewById(R.id.medical_title);
        final EditText description =dialogView.findViewById(R.id.medical_description);
        ImageButton imgBtn=dialogView.findViewById(R.id.imgUploadBtn);
        imgView=dialogView.findViewById(R.id.medical_img_view);
        Button add=dialogView.findViewById(R.id.add_medical_category);

        alertDialog.show();

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),10);
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalActivity.this,"Enter Title",Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                }
                else if(description.getText().toString().length()==0){
                    Toast.makeText(HospitalMedicalActivity.this,"Enter Description",Toast.LENGTH_SHORT).show();
                    description.requestFocus();
                }
                else if(imgUrl==null){
                    Toast.makeText(HospitalMedicalActivity.this,"Upload Category Image",Toast.LENGTH_SHORT).show();
                }
                else{
                    final MedicalCategory medicalCategory=new MedicalCategory("",title.getText().toString(),imgUrl,description.getText().toString(),new ArrayList<MedicalAnalysis>());
                    medicalCategory.setHospitalId(user.getUid());
                    ref.push().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            medicalCategory.setId(dataSnapshot.getKey());
                            ref.child(medicalCategory.getId()).setValue(medicalCategory);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    alertDialog.dismiss();
                }
            }
        });



    }

    private void requestPermission() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
        }
        else{
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 44);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==10){
            final Uri image=data.getData();
            if(image!=null) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressViewDialog.showProgressDialog("Uploading image.Please Wait...");
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        String filename = getFileName(image);

                        final StorageReference fileToUpload = media_storage_ref.child("Images").child(filename);
                        fileToUpload.putFile(image).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(task.isSuccessful()){
                                    return fileToUpload.getDownloadUrl();
                                }
                                return null;
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    imgUrl=task.getResult().toString();
                                    imgView.setImageURI(image);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        CustomToast.darkColor(HospitalMedicalActivity.this, CustomToastType.SUCCESS, "Uploaded Successfully!");
                        progressViewDialog.hideDialog();

                    }
                }.execute();
            }
        }
        else{
            CustomToast.darkColor(this, CustomToastType.INFO,"You need to select more than 1 file");
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri){
        String result=null;
        if(uri.getScheme().equals("content")) {
            Cursor cursor=this.getContentResolver().query(uri,null,null,null,null);
            try {
                if(cursor!=null&&cursor.moveToFirst()) {
                    result=cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }
        if(result==null) {
            result=uri.getPath();
            int cut=result.lastIndexOf('/');
            if(cut==-1) {
                result=result.substring(cut+1);
            }
        }
        return result;
    }

}
