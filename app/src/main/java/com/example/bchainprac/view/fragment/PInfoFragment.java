package com.example.bchainprac.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
import com.example.bchainprac.network.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.view.View.inflate;

public class PInfoFragment extends BottomSheetDialogFragment {
    private BottomSheetBehavior bottomSheetBehavior;
    private ProgressViewDialog progressViewDialog;

    private Context context;
    private User user;
    private TextView email,phone;
    private EditText fname,lname;
    private ImageButton fname_edit,lname_edit;
    ImageView male,female;
    Button save;

    public PInfoFragment(Context context,User user) {
        this.context = context;
        this.user=user;
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = inflate(getContext(), R.layout.activity_personal_information, null);
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        initView(view);
        return bottomSheet;
    }

    private void initView(View view) {
        fname=view.findViewById(R.id.p_fname_id);
        lname=view.findViewById(R.id.p_lname_id);
        email=view.findViewById(R.id.p_email_id);
        phone=view.findViewById(R.id.p_phone_id);
        fname_edit=view.findViewById(R.id.fname_edit_btn);
        lname_edit=view.findViewById(R.id.lname_edit_btn);
        male=view.findViewById(R.id.p_male_imageView);
        female=view.findViewById(R.id.p_female_imageView);
        save=view.findViewById(R.id.save_button);

        fname.setText(user.getFirstName());
        lname.setText(user.getLastName());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
        if(user.getGender().equals("Male")){
            male.setImageDrawable(getResources().getDrawable(R.drawable.signup_male_selected));
            female.setImageDrawable(getResources().getDrawable(R.drawable.signup_female_deselected));
        }
        else if(user.getGender().equals("Female")){
            male.setImageDrawable(getResources().getDrawable(R.drawable.signup_male_deselected));
            female.setImageDrawable(getResources().getDrawable(R.drawable.signup_female_selected));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        setListeners();
    }

    private void setListeners(){
        fname_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname.setFocusable(true);
                fname.setFocusableInTouchMode(true);
                fname.setClickable(true);
            }
        });
        lname_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lname.setEnabled(true);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressViewDialog = new ProgressViewDialog(context);
                progressViewDialog.isShowing();
                progressViewDialog.setDialogCancelable(false);
                progressViewDialog.setCanceledOnTouchOutside(false);
                progressViewDialog.showProgressDialog("Updating information...");
                user.setFirstName(fname.getText().toString());
                user.setLastName(lname.getText().toString());
                fname.setFocusable(false);
                lname.setFocusable(false);
                updateDatabase();
            }
        });
    }

    private void updateDatabase() {

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                CustomToast.darkColor(context, CustomToastType.SUCCESS, "Successfully Updated");
                progressViewDialog.hideDialog();
            }
        });
    }
}
