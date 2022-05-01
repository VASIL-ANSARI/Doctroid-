package com.example.bchainprac.customView;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.example.bchainprac.R;

import www.sanju.motiontoast.MotionToast;

public class CustomToast {
    public static void darkColor(Context context,CustomToastType type,String message){
        Log.d("message",type.getValue());
        MotionToast.Companion.darkColorToast((Activity) context,message,type.getValue(),MotionToast.Companion.getGRAVITY_BOTTOM(),MotionToast.Companion.getLONG_DURATION(),Typeface.SANS_SERIF);

    }
}
