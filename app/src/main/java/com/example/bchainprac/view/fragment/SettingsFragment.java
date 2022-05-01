package com.example.bchainprac.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.bchainprac.BuildConfig;
import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.utilities.PrefManager;
import com.example.bchainprac.view.activity.AccountInfoActivity;
import com.example.bchainprac.view.activity.SelectionActivity;
import com.example.bchainprac.view.activity.SignInActivity;
import com.example.bchainprac.view.activity.WalletCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public Context context;
    private Button logout,walletCheck,accountInfo,personalInfo,notifications;
    private TextView version;
    private User user;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private DatabaseReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        context = getContext();
        user=new User();
        auth=FirebaseAuth.getInstance();
        user.setEmail(auth.getCurrentUser().getEmail());
        firebaseDatabase = FirebaseDatabase.getInstance("https://doctroid-app-default-rtdb.firebaseio.com/");
        final ProgressViewDialog progressViewDialog=new ProgressViewDialog(context);
        progressViewDialog.showProgressDialog("Loading ....");
        reference =firebaseDatabase.getReference("User");
        reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                initializeComponents(view);
                setListeners();
                progressViewDialog.hideDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
    private void initializeComponents(View view) {
        logout = view.findViewById(R.id.settingsFragment_Logout_Button);
        version = view.findViewById(R.id.settingsFragment_version_TV);
        walletCheck=view.findViewById(R.id.wallet_btn);
        accountInfo = view.findViewById(R.id.settingFragment_accountSettings);
        personalInfo=view.findViewById(R.id.settingFragment_personalInformation);
        notifications=view.findViewById(R.id.settingFragment_notification);
    }

    private void setListeners() {
        version.setText(String.format("Version: %s", BuildConfig.VERSION_NAME));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        walletCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, WalletCheck.class);
                context.startActivity(intent);
            }
        });
        personalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PInfoFragment pInfoFragment = new PInfoFragment(context,user);
                pInfoFragment.show(getFragmentManager(),"PInfo");
            }
        });
        accountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, AccountInfoActivity.class));
            }
        });

    }
    private void showLogoutDialog() {
        sharedPreferences=context.getSharedPreferences(Constants.SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.logout));
        builder.setMessage("Are you sure to logout?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putString("isLogin",null);
                editor.commit();
//                PrefManager.deleteToken(context);
//                PrefManager.deleteConfirm(context);
//                PrefManager.deleteP_id(context);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(context, SelectionActivity.class));
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
