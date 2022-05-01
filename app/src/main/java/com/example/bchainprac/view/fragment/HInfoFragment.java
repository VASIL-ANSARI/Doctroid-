package com.example.bchainprac.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.helpers.Validator;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.view.View.inflate;

public class HInfoFragment extends BottomSheetDialogFragment {
    private BottomSheetBehavior bottomSheetBehavior;
    private ProgressViewDialog progressViewDialog;

    private Context context;
    private Hospital hospital;
    private TextView name,phone,location,details;
    Button save;
    private EditText facebook,email,website;
    private Map<String, ConfirmSignUpForm> managers;

    public HInfoFragment(Context context, Hospital hospital, Map<String, ConfirmSignUpForm> managers) {
        this.context = context;
        this.hospital=hospital;
        this.managers=managers;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = inflate(getContext(), R.layout.activity_hospital_information, null);
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        initView(view);
        return bottomSheet;
    }

    private void initView(View view) {
        name=view.findViewById(R.id.name_hospital_id);
        phone=view.findViewById(R.id.phone_hospital_id);
        location=view.findViewById(R.id.location_hospital_id);
        save=view.findViewById(R.id.hospital_save_btn);
        facebook=view.findViewById(R.id.facebookUrl_id);
        website=view.findViewById(R.id.websiteUrl_id);
        email=view.findViewById(R.id.emailId_id);
        details=view.findViewById(R.id.Manager_Details);

        name.setText(hospital.getHospital_name());
        phone.setText(hospital.getHospital_phone());
        location.setText(hospital.getHospital_location());
        facebook.setText((hospital.getHospital_facebook().equals("")?"":hospital.getHospital_facebook()));
        website.setText((hospital.getHospital_website().equals("")?"":hospital.getHospital_website()));
        email.setText((hospital.getHospital_email().equals("")?"":hospital.getHospital_email()));

        String mangers="";
        mangers+="General Manager : "+managers.get(hospital.getHospital_generalManager()).getName()+"\n";
        mangers+="Purchasing Manager : "+managers.get(hospital.getHospital_PurchasingManager()).getName()+"\n";
        mangers+="IT Manager : "+managers.get(hospital.getHospital_itManager()).getName()+"\n";
        mangers+="Administrator Manager : "+managers.get(hospital.getHospital_adminstratonManager()).getName()+"\n";
        mangers+="Marketing Manager : "+managers.get(hospital.getHospital_MarketingManager()).getName()+"\n";
        details.setText(mangers);

    }

    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        setListeners();
    }

    private void setListeners(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkWebsite(website.getText().toString())){
                    CustomToast.darkColor(context,CustomToastType.INFO,"Enter valid website url");
                    website.requestFocus();
                }
                else if(!checkFacebook(facebook.getText().toString())){
                    CustomToast.darkColor(context,CustomToastType.INFO,"Enter valid facebook url");
                    facebook.requestFocus();

                }
                else if(!checkEmail(email.getText().toString())){
                    CustomToast.darkColor(context,CustomToastType.INFO,"Enter valid email");
                    email.requestFocus();

                }
                else{
                    hospital.setHospital_email(email.getText().toString());
                    hospital.setHospital_website(website.getText().toString());
                    hospital.setHospital_facebook(facebook.getText().toString());
                    updateDatabase();
                }
            }
        });
    }

    private boolean checkWebsite(String toString) {
        if(toString.length()==0 || URLUtil.isValidUrl(toString))
            return true;
        return false;
    }

    private boolean checkFacebook(String toString) {
        if(toString.length()==0 || URLUtil.isValidUrl(toString) && toString.toLowerCase().contains("facebook"))
            return true;
        return false;
    }
    private boolean checkEmail(String toString) {
        if(toString.length()==0 || Validator.isValidEmail(toString.trim())){
            return true;
        }
        return false;
    }

    private void updateDatabase() {
        progressViewDialog=new ProgressViewDialog(context);
        progressViewDialog.showProgressDialog("Updating...");
        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(hospital).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    CustomToast.darkColor(context, CustomToastType.SUCCESS,"Updated Successfully!!!");
                    progressViewDialog.hideDialog();
                }
                else{
                    CustomToast.darkColor(context, CustomToastType.ERROR,"Error updating details.Try again later");
                    progressViewDialog.hideDialog();
                }
            }
        });
    }
}
