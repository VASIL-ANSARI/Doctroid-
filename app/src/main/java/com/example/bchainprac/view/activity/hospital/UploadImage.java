package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Images;
import com.example.bchainprac.view.activity.MainActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.internal.ws.RealWebSocket;

public class UploadImage extends AppCompatActivity {

    private RecyclerView uploaded_img_view;
    private RecyclerView.Adapter uploaded_img_adapter;
    private DatabaseReference databaseReference;
    private TextView img_count_text;
    private ImageButton upload_button;
    private final int UPLOAD_LIMIT=10;
    private List<String> imagesList;
    //private List<Images> imagesList;
    private StorageReference media_storage_ref;
    ProgressViewDialog progressViewDialog;

    // class variables
    private static final int REQUEST_CODE = 123;
    private ArrayList<String> mResults = new ArrayList<>();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        media_storage_ref= FirebaseStorage.getInstance("gs://doctroid-app.appspot.com").getReference();
        auth=FirebaseAuth.getInstance();
        uploaded_img_view=findViewById(R.id.uploaded_img_view);
        img_count_text=findViewById(R.id.img_count_text);
        upload_button=findViewById(R.id.upload_button);
        uploaded_img_view.setHasFixedSize(true);
        uploaded_img_view.setLayoutManager(new GridLayoutManager(UploadImage.this, 2));
        databaseReference= FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Images");

        progressViewDialog=new ProgressViewDialog(UploadImage.this);
        progressViewDialog.showProgressDialog("Fetching Images....");

        Fresco.initialize(getApplicationContext());
        //get the bundle
        Bundle b = getIntent().getExtras();
        List<Images> imgList = (ArrayList<Images>) b.getSerializable("ImagesList");
        if(imgList==null || imgList.size()==0){
            imagesList=new ArrayList<>();
        }else{
            imagesList=imgList.get(0).getUrls();
        }
        uploaded_img_adapter=new ImageAdapter(UploadImage.this,imagesList);
        uploaded_img_view.setAdapter(uploaded_img_adapter);
        img_count_text.setText("Images uploaded: " + uploaded_img_adapter.getItemCount() + "/10");

        if(uploaded_img_adapter.getItemCount()>=10){
            upload_button.setVisibility(View.GONE);
        }
        else{
            upload_button.setVisibility(View.VISIBLE);
        }

        progressViewDialog.hideDialog();


        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        }.execute();


        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestPermission()){
                    // start multiple photos selector
                    Intent intent = new Intent(UploadImage.this, ImagesSelectorActivity.class);
                    // max number of images to be selected
                    intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, Math.min(5,10-imagesList.size()));
                    // min size of image which will be shown; to filter tiny images (mainly icons)
                    //                    intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
                    // show camera or not
                    intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
                    // pass current selected images as the initial value
                    //                    intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults);
                    // start the selector
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }

    private void fetchImages() {

        upload_button.setVisibility(View.VISIBLE);
        imagesList.clear();

        progressViewDialog.showProgressDialog("Fetching Images...");


        databaseReference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Images images = dataSnapshot.getValue(Images.class);
                    List<String> urlLinks = images.getUrls();
                    if (urlLinks != null && urlLinks.size() > 0) {
                        imagesList.addAll(urlLinks.subList(0, Math.min(urlLinks.size(), 10)));
                        if(imagesList.size()>UPLOAD_LIMIT){
                            upload_button.setVisibility(View.GONE);
                        }
                        Log.d("message",urlLinks.size()+" ");

                    }
                    uploaded_img_adapter=new ImageAdapter(UploadImage.this,imagesList);
                    uploaded_img_view.setAdapter(uploaded_img_adapter);
                    uploaded_img_adapter.notifyDataSetChanged();
                    img_count_text.setText("Images uploaded: " + uploaded_img_adapter.getItemCount() + "/10");
                    Log.d("message","dismiss");
                    progressViewDialog.hideDialog();
                }
                else{
                    uploaded_img_adapter=new ImageAdapter(UploadImage.this,imagesList);
                    uploaded_img_view.setAdapter(uploaded_img_adapter);
                    uploaded_img_adapter.notifyDataSetChanged();
                    img_count_text.setText("Images uploaded: " + uploaded_img_adapter.getItemCount() + "/10");
                    Log.d("message","dismiss else");
                    progressViewDialog.hideDialog();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UploadImage.this,databaseError.toString(),Toast.LENGTH_LONG).show();
                progressViewDialog.hideDialog();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== REQUEST_CODE && data!=null){
            mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
            assert mResults != null;
            final  ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Images...");
            progressDialog.setMax(mResults.size());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Uri[] uri=new Uri[mResults.size()];
            for (int i =0 ; i < mResults.size(); i++) {
                uri[i] = Uri.parse("file://"+mResults.get(i));
                StorageReference storageRef = media_storage_ref.child("Images");
                final StorageReference ref = storageRef.child(uri[i].getLastPathSegment());
                final int finalI = i;
                ref.putFile(uri[i]).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(task.isSuccessful()){
                            return ref.getDownloadUrl();
                        }
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            imagesList.add(task.getResult().toString());
                            Images images1=new Images();
                            images1.setUrls(imagesList);
                            uploaded_img_adapter.notifyDataSetChanged();
                            img_count_text.setText("Images uploaded: " + uploaded_img_adapter.getItemCount() + "/10");
                            databaseReference.child(auth.getCurrentUser().getUid()).setValue(images1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.setProgress(finalI +1);
                                    if(progressDialog.getProgress()==mResults.size()){
                                        CustomToast.darkColor(UploadImage.this, CustomToastType.SUCCESS, "Uploaded Successfully!");
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });

            }
        }
    }




    private boolean requestPermission() {
        if(ActivityCompat.checkSelfPermission(UploadImage.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UploadImage.this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(UploadImage.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else{
            ActivityCompat.requestPermissions((Activity) UploadImage.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 44);
            return false;
        }
    }
}
