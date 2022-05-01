package com.example.bchainprac.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.bchainprac.R;
import com.example.bchainprac.app.App;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.utilities.InternetUtilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalAboutFragment extends Fragment {
    public Context context;
    private TextView hospitalName, hospitalLocation,
            hospitalPhone, hospitalWebsite, hospitalFacebook, hospitalEmail;

    private ProgressBar progressBar;

    private LinearLayout location_layout, phone_layout, website_layout, facebook_layout, email_layout;

    private View lineView;
    private Hospital hid;
    private List<String> managers;

    public HospitalAboutFragment(Hospital hid, List<String> managers){
        this.hid=hid;
        this.managers=managers;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hospital_about_fragment, container, false);

        context = getActivity().getApplicationContext();

        initUI(view);
        if (!InternetUtilities.isConnected(context)) {
            CustomToast.darkColor(getContext(), CustomToastType.NO_INTERNET, getString(R.string.check_connection));
        } else {
            getHospital();
        }

        return view;
    }

    private void initUI(View view) {
        lineView = view.findViewById(R.id.hospitalAboutFragment_view);
        progressBar = view.findViewById(R.id.hospitalAboutFragment_progressBar);

        hospitalName = view.findViewById(R.id.hospitalAboutFragment_hospitalName_TV);
        hospitalLocation = view.findViewById(R.id.hospitalAboutFragment_hospitalLocation_TV);
        hospitalPhone = view.findViewById(R.id.hospitalAboutFragment_hospitalPhone_TV);
        hospitalWebsite = view.findViewById(R.id.hospitalAboutFragment_hospitalWebsite_TV);
        hospitalFacebook = view.findViewById(R.id.hospitalAboutFragment_hospitalFacebook_TV);
        hospitalEmail = view.findViewById(R.id.hospitalAboutFragment_hospitalEmail_TV);

        location_layout = view.findViewById(R.id.location_layout);
        phone_layout = view.findViewById(R.id.phone_layout);
        website_layout = view.findViewById(R.id.website_layout);
        facebook_layout = view.findViewById(R.id.facebook_layout);
        email_layout = view.findViewById(R.id.emails_layout);

        lineView.setVisibility(view.INVISIBLE);

        location_layout.setVisibility(view.INVISIBLE);
        phone_layout.setVisibility(view.INVISIBLE);
        website_layout.setVisibility(view.INVISIBLE);
        facebook_layout.setVisibility(view.INVISIBLE);
        email_layout.setVisibility(view.INVISIBLE);
    }

    private void getHospital() {

        hospitalName.setText(hid.getHospital_name());
        hospitalLocation.setText(hid.getHospital_location());
        hospitalPhone.setText(hid.getHospital_phone());
        hospitalWebsite.setText("Not Available");
        hospitalFacebook.setText("Not Available");

        progressBar.setVisibility(View.GONE);
        lineView.setVisibility(View.VISIBLE);
        location_layout.setVisibility(View.VISIBLE);
        phone_layout.setVisibility(View.VISIBLE);
        website_layout.setVisibility(View.VISIBLE);
        facebook_layout.setVisibility(View.VISIBLE);
        email_layout.setVisibility(View.VISIBLE);

        DatabaseReference ref=FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners");
        String emails = "";
        emails += "⦿ Email: " + (hid.getHospital_email().equals("")?"NA":hid.getHospital_email()) + "\n";
        emails += "⦿ Website Link: " + (hid.getHospital_website().equals("")?"NA":hid.getHospital_website()) + "\n";
        emails += "⦿ Facebook Link: " + (hid.getHospital_facebook().equals("")?"NA":hid.getHospital_facebook()) + "\n";
        emails += "• General Manager: " + managers.get(2) + "\n";
        emails += "• Administration Manager: " + managers.get(1) + "\n";
        emails += "• IT Manager: " + managers.get(0)+ "\n";
        emails += "• Marketing Manager: " + managers.get(3) + "\n";
        emails += "• Purchasing Manager: " +managers.get(4) + "\n";


        hospitalEmail.append(emails);

    }
}
