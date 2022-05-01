package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.view.activity.admin.AdminModel;
import com.example.bchainprac.view.base.BaseActivity;
import com.example.bchainprac.view.fragment.DatePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddUserActivity extends BaseActivity implements DatePickerFragment.DateSet {
    private Button signUp, date;
    private MaterialSpinner citySpinner,ownerSpinner;
    private ProgressViewDialog progressViewDialog;
    private EditText snn;

    private ImageView errorDialog;
    private TextView errorMessage;
    private ArrayList<String> ownerLists=new ArrayList<>();

    String citySTR = "Empty", dateSTR = "Empty",Ownertype="Empty";

    private RecyclerView recyclerView;
    private CardView ownerCardView;
    private ownerAdapter medicineAdapter;
    private List<ConfirmSignUpForm> ownerArrayList;
    private String[] lists;
    private String HospitalId;
    private String[] owners=new String[5];
    //0-general
    //1-administrator
    //2-purchasing
    //3-marketing
    //4-it

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure to exit?");
        builder.setMessage("Your saved Owner list will be retained.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddUserActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public AddUserActivity() {
        super(R.layout.activity_add_user, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {
        toolbarTextView.setText("Owner Information");
        HospitalId=getIntent().getStringExtra("HospitalUid");


        initializeComponents();
        setListeners();
    }

    private void initializeComponents() {

        date = findViewById(R.id.addMedicine_date_button);
        citySpinner = findViewById(R.id.addMedicine_citySpinner);
        signUp = findViewById(R.id.addMedicine_SignUp_button);
        snn = findViewById(R.id.addMedicine_SNN_ET);
        ownerSpinner=findViewById(R.id.addMedicine_medicineAutoComplete);
        ownerCardView = findViewById(R.id.addOwnerCardViewList);
        recyclerView=findViewById(R.id.addOwnerRecyclerView);

        ownerCardView.setVisibility(View.VISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(AddUserActivity.this));
        recyclerView.setHasFixedSize(true);

        ownerArrayList=new ArrayList<>();

        medicineAdapter = new ownerAdapter(AddUserActivity.this, ownerArrayList,
                new ownerAdapter.ItemClick() {
                    @Override
                    public void onClick(final int position) {
                        final ConfirmSignUpForm form=ownerArrayList.get(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
                        builder.setTitle(form.getName());
                        builder.setMessage("Are you sure to delete this owner?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").child(form.getId()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            if(form.getOwnerType().equals("General Manager")){
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).child("hospital_generalManager").setValue("");
                                                owners[0]="";
                                            }
                                            else if(form.getOwnerType().equals("Administration Manager")){
                                                owners[1]="";
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).child("hospital_adminstratonManager").setValue("");
                                            }
                                            else if(form.getOwnerType().equals("Purchasing Manager")){
                                                owners[2]="";
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).child("hospital_PurchasingManager").setValue("");
                                            }
                                            else if(form.getOwnerType().equals("Marketing Manager")){
                                                owners[3]="";
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).child("hospital_MarketingManager").setValue("");
                                            }else if(form.getOwnerType().equals("IT Manager")){
                                                owners[4]="";
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).child("hospital_itManager").setValue("");
                                            }
                                            callApi();
//                                            ownerArrayList.remove(form);
//                                            medicineAdapter.notifyDataSetChanged();
//                                            ownerArrayList.clear();
                                            ownerLists.add(form.getOwnerType());
                                            ownerSpinner.setItems(ownerLists);
//                                            ownerSpinner.setSelectedIndex(0);
//                                            citySpinner.setSelectedIndex(0);
//                                            dateSTR="Empty";
//                                            citySTR="Empty";
//                                            Ownertype="Empty";
//                                            date.setText("Set DOB");
//                                            snn.setText("");
                                        }
                                    }
                                });

                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
        recyclerView.setAdapter(medicineAdapter);

        errorDialog = findViewById(R.id.addMedicine_errorDialog_imageView);
        errorMessage = findViewById(R.id.addMedicine_errorMessage_textView);

        citySpinner.setItems("Select City",
                "Agra",
                "Ahmedabad",
                "Bangalore",
                "Chennai",
                "Auraiya",
                "Bareilly",
                "Bijnor",
                "Chittor",
                "Dehradun",
                "Hassan",
                "Haveri",
                "Lucknow",
                "Meerut",
                "Mandya",
                "Mau",
                "Mathura",
                "Muzaffarpur",
                "Mysore",
                "Nagpur",
                "Pallakad",
                "Puri",
                "Raichur",
                "Shimla",
                "Thane",
                "Tiruchirappalli",
                "Trichi",
                "Varanasi");

        lists=new String[]{"Select Authority type", "General Manager", "Administration Manager", "Purchasing Manager", "IT Manager", "Marketing Manager"};
        ownerLists=new ArrayList<>(Arrays.asList(lists));
        ownerSpinner.setItems(ownerLists);

    }

    private void setListeners() {

        callApi();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment(AddUserActivity.this, "birthday");
                datePicker.show(AddUserActivity.this.getSupportFragmentManager(), "Date Picker");
            }

        });

        citySpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                citySTR = item;
            }
        });
        ownerSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    Ownertype = item;
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signUp.getText().toString().equals("DONE")){
                    taskDone();
                }
                else {
                    String snnSTR = snn.getText().toString().trim();

                    if (dateSTR.equals("Empty")) {
                        errorDialog.setVisibility(android.view.View.VISIBLE);
                        errorMessage.setText("Please select your DOB");
                    } else if (citySTR.equals("Empty") || citySTR.equals("Select City")) {
                        errorDialog.setVisibility(android.view.View.VISIBLE);
                        errorMessage.setText("Please select your City");
                    } else if (Ownertype.equals("Empty") || Ownertype.equals("Select Authority type")) {
                        errorDialog.setVisibility(android.view.View.VISIBLE);
                        errorMessage.setText("Please select your Owner Type");
                    } else if (snnSTR.length() == 0) {
                        errorDialog.setVisibility(android.view.View.VISIBLE);
                        errorMessage.setText("Enter your owner's name");
                        snn.requestFocus();
                    } else {
                        errorDialog.setVisibility(android.view.View.INVISIBLE);
                        errorMessage.setVisibility(android.view.View.INVISIBLE);

                        signUpAPI(dateSTR, citySTR, snnSTR, Ownertype);

                    }
                }
            }
        });

    }

    private void callApi() {
        final ProgressViewDialog progressViewDialog=new ProgressViewDialog(this);
        progressViewDialog.showProgressDialog("Fetching Owners...");
        ownerArrayList.clear();
        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hospital hospital=dataSnapshot.getValue(Hospital.class);
                owners[0]=hospital.getHospital_generalManager();
                owners[1]=hospital.getHospital_adminstratonManager();
                owners[2]=hospital.getHospital_PurchasingManager();
                owners[3]=hospital.getHospital_MarketingManager();
                owners[4]=hospital.getHospital_itManager();
                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ConfirmSignUpForm f=snapshot.getValue(ConfirmSignUpForm.class);
                            Log.d("message owners",f.getId());
                            if(f.getId().equals(owners[0]) || f.getId().equals(owners[1]) || f.getId().equals(owners[2]) || f.getId().equals(owners[3]) || f.getId().equals(owners[4])){
                                ownerArrayList.add(f);
                                medicineAdapter.notifyDataSetChanged();
                                ownerLists.remove(f.getOwnerType());
                                ownerSpinner.setItems(ownerLists);
                                ownerSpinner.setSelectedIndex(0);
                                citySpinner.setSelectedIndex(0);
                                dateSTR="Empty";
                                citySTR="Empty";
                                Ownertype="Empty";
                                date.setText("Set DOB");
                                snn.setText("");
                                if(ownerArrayList.size()==5){
                                    signUp.setText("DONE");
                                }
                                else{
                                    signUp.setText("ADD");
                                }
                                progressViewDialog.hideDialog();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void taskDone() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure to Submit these details ?");
        builder.setMessage("These owners have to send valid document to vasilXXXXXXXXXX@gmail.com . After verification only you will be able to access your account.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressViewDialog = new ProgressViewDialog(AddUserActivity.this);
                progressViewDialog.setDialogCancelable(false);
                progressViewDialog.setCanceledOnTouchOutside(false);
                progressViewDialog.showProgressDialog("Wait for a while.Saving Details...");

                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Hospital h=dataSnapshot.getValue(Hospital.class);
                        h.setHospital_generalManager(owners[0]);
                        h.setHospital_adminstratonManager(owners[1]);
                        h.setHospital_PurchasingManager(owners[2]);
                        h.setHospital_MarketingManager(owners[3]);
                        h.setHospital_itManager(owners[4]);
                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);

                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                                    ConfirmSignUpForm form = snapshot.getValue(ConfirmSignUpForm.class);
                                    if (form.getId().equals(owners[0]) || form.getId().equals(owners[1]) || form.getId().equals(owners[2]) || form.getId().equals(owners[3]) || form.getId().equals(owners[4])) {
                                        List<String> strs = new ArrayList<>();
                                        strs.add("owner verification");
                                        strs.add(form.getName());
                                        strs.add(form.getOwnerType());
                                        strs.add(HospitalId);
                                        final AdminModel model = new AdminModel(strs, "hospital", form.getId());
                                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").push().addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                model.setId(dataSnapshot.getKey());
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").child(model.getId()).setValue(model);
                                                CustomToast.darkColor(AddUserActivity.this, CustomToastType.SUCCESS, "Added Successfully!!");
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


                        SharedPreferences preferences=getSharedPreferences(Constants.SHARED_PREF_NAME,MODE_PRIVATE);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putBoolean(Constants.HOSPITAL_FIRST_TIME_LOGIN,false);
                        editor.commit();
                        CustomToast.darkColor(AddUserActivity.this,CustomToastType.INFO,"All details saved.You will be notified when all owners are verified.");
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onDateSet(int year, int month, int day) {
        dateSTR = day + "/" + ++month + "/" + year;
        date.setText("Date: " + dateSTR);
    }


    private void signUpAPI(String dob, String city, String name,final String owner) {
        final ConfirmSignUpForm form=new ConfirmSignUpForm(dob,name,owner,city);
        form.setVerified(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressViewDialog = new ProgressViewDialog(AddUserActivity.this);
                progressViewDialog.setDialogCancelable(false);
                progressViewDialog.setCanceledOnTouchOutside(false);
                progressViewDialog.showProgressDialog("Wait for a while.Updating Details...");

                String key=FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").push().getKey();
                form.setId(key);
                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").child(key).setValue(form).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Hospital h=dataSnapshot.getValue(Hospital.class);
//                                            ownerArrayList.add(form);
//                                            medicineAdapter.notifyDataSetChanged();
                                            Log.d("message",form.getOwnerType());
                                            if(form.getOwnerType().equals("General Manager")){
                                                h.setHospital_generalManager(form.getId());
                                                owners[0]=form.getId();
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);
                                            }
                                            else if(form.getOwnerType().equals("Administration Manager")){
                                                h.setHospital_adminstratonManager(form.getId());
                                                owners[1]=form.getId();
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);
                                            }
                                            else if(form.getOwnerType().equals("Purchasing Manager")){
                                                h.setHospital_PurchasingManager(form.getId());
                                                owners[2]=form.getId();
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);
                                            }
                                            else if(form.getOwnerType().equals("Marketing Manager")){
                                                h.setHospital_MarketingManager(form.getId());
                                                owners[3]=form.getId();
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);
                                            }else if(form.getOwnerType().equals("IT Manager")){
                                                h.setHospital_itManager(form.getId());
                                                owners[4]=form.getId();
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(HospitalId).setValue(h);
                                            }

                                            callApi();

//                                            ownerLists.remove(Ownertype);
//                                            ownerSpinner.setItems(ownerLists);
//                                            ownerSpinner.setSelectedIndex(0);
//                                            citySpinner.setSelectedIndex(0);
//                                            dateSTR="Empty";
//                                            citySTR="Empty";
//                                            Ownertype="Empty";
//                                            date.setText("Set DOB");
//                                            snn.setText("");
                                            if(ownerArrayList.size()==5){
                                                signUp.setText("DONE");
                                            }
                                            progressViewDialog.hideDialog();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();



    }

}
