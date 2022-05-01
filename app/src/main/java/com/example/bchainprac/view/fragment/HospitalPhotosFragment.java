package com.example.bchainprac.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.network.Image;
import com.example.bchainprac.network.model.Images;
import com.example.bchainprac.presenter.ViewPagerAdapter;
import com.example.bchainprac.utilities.InternetUtilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HospitalPhotosFragment extends Fragment {
    public Context context;
    private DatabaseReference database;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<Image> imageUrls = new ArrayList<>();

    private ProgressBar progressBar;
    private ImageView leftArrow, rightArrow;
    private TextView positionTV, countTV;
    private CardView index;
    private String hid;

    public HospitalPhotosFragment(String hid){
        this.hid=hid;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hospital_photos_fragment, container, false);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();

        initializeComponents(view);
        if (!InternetUtilities.isConnected(context)) {
            CustomToast.darkColor(getContext(), CustomToastType.NO_INTERNET, getString(R.string.check_connection));
        } else {
            getPhotos();
        }
        setListeners();

        return view;
    }


    private void initializeComponents(View view) {
        leftArrow = view.findViewById(R.id.hospitalPhotosFragment_leftArrow);
        rightArrow = view.findViewById(R.id.hospitalPhotosFragment_rightArrow);
        progressBar = view.findViewById(R.id.hospitalPhotosFragment_progressBar);
        positionTV = view.findViewById(R.id.hospitalPhotosFragment_position);
        countTV = view.findViewById(R.id.hospitalPhotosFragment_count);
        index = view.findViewById(R.id.hospitalPhotosFragment_index);

        positionTV.setText("1");

        viewPager = view.findViewById(R.id.hospitalPhotosFragment_viewPager);
        viewPagerAdapter = new ViewPagerAdapter(context, imageUrls);
        viewPager.setAdapter(viewPagerAdapter);

    }
    private void getPhotos() {
        database = FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Images").child(hid);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Images images = dataSnapshot.getValue(Images.class);
                    for (String img : images.getUrls()) {
                        Image image = new Image();
                        image.setImg(img);
                        if (image != null) {
                            imageUrls.add(image);
                            viewPagerAdapter.notifyDataSetChanged();
                        } else {
                            CustomToast.darkColor(getContext(), CustomToastType.INFO, "No photos added yet!");
                        }
                    }

                    countTV.setText(String.valueOf(imageUrls.size()));
                    progressBar.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                    index.setVisibility(View.VISIBLE);
                }
                else{
                    positionTV.setText("0");
                    countTV.setText(String.valueOf(imageUrls.size()));
                    progressBar.setVisibility(View.INVISIBLE);
                    index.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                CustomToast.darkColor(getContext(), CustomToastType.ERROR, databaseError.getMessage());
            }
        });
    }

    private void setListeners() {
        leftArrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = viewPager.getCurrentItem();
                changePosition(--position);
            }
        });
        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewPager.getCurrentItem();
                changePosition(++position);
            }
        });
    }

    private void changePosition(int position) {
        if (position < 0) {
            position = viewPagerAdapter.getCount() - 1;
        } else if (position == viewPagerAdapter.getCount()) {
            position = 0;
        }
        viewPager.setCurrentItem(position, true);
        positionTV.setText(String.valueOf(position + 1));
    }
}
