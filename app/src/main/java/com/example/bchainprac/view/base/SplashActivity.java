package com.example.bchainprac.view.base;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.race604.drawable.wave.WaveDrawable;

public class SplashActivity extends BaseActivity {

    View view;

    public SplashActivity() {
        super(R.layout.activity_splash, false);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {

        view = findViewById(R.id.view);
        waveLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToLogin();
            }
        }, Constants.SPLASH_TIME_OUT);
    }

    private void waveLoading() {

        WaveDrawable colorWave = new WaveDrawable(this, R.drawable.back_circle);
        view.setBackground(colorWave);
        colorWave.setIndeterminate(true);
    }


    private void navigateToLogin() {
        Intent i = new Intent(SplashActivity.this, SliderActivity.class);
        startActivity(i);
        finish();
    }
}
