package com.example.bchainprac.view.activity.hospital;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.bchainprac.R;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.network.Image;
import com.example.bchainprac.network.model.Category;
import com.example.bchainprac.presenter.Adapter.MainAdapter;
import com.example.bchainprac.presenter.ViewPagerAdapter;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.view.activity.AppointmentActivity;
import com.example.bchainprac.view.activity.HospitalActivity;
import com.example.bchainprac.view.activity.MedicalActivity;
import com.example.bchainprac.view.activity.MedicineActivity;
import com.example.bchainprac.view.activity.ResultActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HospitalMainFragment extends Fragment {
    public Context context;
    private DatabaseReference database;

    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private ArrayList<Category> categoryList;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private List<Image> imageUrl=new ArrayList<>();
    private LinearLayout dots;
    private int currentPosition = 0;

    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospital_home, container, false);

        context = Objects.requireNonNull(getActivity()).getApplicationContext();

        initializeComponents(view);
        setListeners();
        return view;
    }

    private void initializeComponents(View view) {
        progressBar = view.findViewById(R.id.hmainFragment_progressBar);
        dots = view.findViewById(R.id.hmainFragment_dots);
        viewPager = view.findViewById(R.id.hmainFragment_viewPager);

        recyclerView = view.findViewById(R.id.hmainFragment_recyclerView);
        categoryList = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(Objects.requireNonNull(getActivity()).getBaseContext(), 2));
        recyclerView.setHasFixedSize(true);

        MainAdapter.ItemClick  itemClick=new MainAdapter.ItemClick() {
            @Override
            public void onClick(int position) {
                String categoryName = categoryList.get(position).getName();
                if (!InternetUtilities.isConnected(getContext())) {
                    CustomToast.darkColor(getActivity(), CustomToastType.NO_INTERNET, getString(R.string.check_connection));
                } else {
                    switch (categoryName) {
                        case "Medical Analysis":
                            startActivity(new Intent(getActivity(), HospitalMedicalActivity.class));
                            break;
                        case "View Appointment":
                            startActivity(new Intent(getActivity(), HospitalViewAppointment.class));
                            break;
                        case "Pharmacy":
                            startActivity(new Intent(getActivity(), HospitalPharmacyActivity.class));
                            break;
                    }
                }
            }
        };
        mainAdapter = new MainAdapter(categoryList, getContext(),itemClick);
        recyclerView.setAdapter(mainAdapter);

        imageUrls.add("https://firebasestorage.googleapis.com/v0/b/doctroid-app.appspot.com/o/h-1.jpg?alt=media&token=509378cb-d21b-49d8-93c9-ca6b50983d68");
        imageUrls.add("https://firebasestorage.googleapis.com/v0/b/doctroid-app.appspot.com/o/h-2.jpg?alt=media&token=8466220f-cb58-4434-bcb8-85d65ffed80d");
        imageUrls.add("https://firebasestorage.googleapis.com/v0/b/doctroid-app.appspot.com/o/h-3.jpg?alt=media&token=7df22c84-b61f-4e93-8118-d231b2949729");
        imageUrls.add("https://firebasestorage.googleapis.com/v0/b/doctroid-app.appspot.com/o/icon-1.jpg?alt=media&token=74f54715-5695-4679-bc87-047f6951aae9");

        for(String s:imageUrls){
            Image i=new Image();
            i.setImg(s);
            imageUrl.add(i);
        }
        viewPagerAdapter = new ViewPagerAdapter(context, imageUrl);
        viewPager.setAdapter(viewPagerAdapter);

    }

    private void setListeners() {

        categoryList.add(new Category("Medical Analysis", R.drawable.icon_2_medical_analysis));
        categoryList.add(new Category("View Appointment", R.drawable.icon_3_appointment));
        categoryList.add(new Category("Pharmacy", R.drawable.icon_5_medicine));

        prepareDots();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPosition = position;
            }
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if (position == viewPagerAdapter.getCount()) {
                    currentPosition = 0;
                }
                prepareDots();
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        createSlideShow();
    }

    private void createSlideShow() {

        Timer timer = new Timer();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                currentPosition++;
                if (currentPosition >= viewPagerAdapter.getCount()) {
                    currentPosition = 0;
                }
                viewPager.setCurrentItem(currentPosition, true);
            }
        };

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 2000, 5000);
    }

    private void prepareDots() {

        if (dots.getChildCount() > 0) {
            dots.removeAllViews();
        }

        ImageView[] dotsIV = new ImageView[viewPagerAdapter.getCount()];

        for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
            dotsIV[i] = new ImageView(context);

            if (i == currentPosition) {
                dotsIV[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.slider_active_dot));
            } else {
                dotsIV[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.slider_inactive_dot));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(8, 0, 8, 0);
            dots.addView(dotsIV[i], layoutParams);

        }
    }
}

