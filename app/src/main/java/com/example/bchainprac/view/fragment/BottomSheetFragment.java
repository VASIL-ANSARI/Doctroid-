package com.example.bchainprac.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.utils.Utils;
import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.AppointRequest;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.utilities.PrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.security.Security;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.view.View.inflate;

public class BottomSheetFragment extends BottomSheetDialogFragment implements
        TimePickerFragment.TimeSet {

    private BottomSheetBehavior bottomSheetBehavior;
    private ProgressViewDialog progressViewDialog;

    private LinearLayout precautions;
    private TextView medicalTitle, precautions_en, precautions_ar, comment;

    private String c_id;
    private int appointDay;
    private int appointMonth;
    private String dateSTR = "Empty";
    private String appointPeriod = "Empty";
    private String timeSTR = "Empty";
    private String commentSTR = "Empty";
    private Button dateBTN, timeBTN;
    CircularProgressButton requestBTN;
    String mnemonic;
    private String h_id;

    private MedicalAnalysis medicalAnalysis;
    private Context context;

    public BottomSheetFragment(MedicalAnalysis medicalAnalysis, String c_id, Context context, String mnemonic,String h_id) {
        this.medicalAnalysis = medicalAnalysis;
        this.c_id = c_id;
        this.context = context;
        this.mnemonic = mnemonic;
        this.h_id=h_id;
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = inflate(getContext(), R.layout.fragment_appoint, null);

        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        initView(view);
        return bottomSheet;
    }

    private void initView(View view) {

        medicalTitle = view.findViewById(R.id.appoint_medicalAnalysis_title);
        precautions = view.findViewById(R.id.appoint_precautions_linearLayout);
        precautions_en = view.findViewById(R.id.appoint_en_precautions_textView);
        precautions_ar = view.findViewById(R.id.appoint_ar_precautions_textView);
        comment = view.findViewById(R.id.appoint_comment_editText);

        dateBTN = view.findViewById(R.id.appoint_date_BTN);
        timeBTN = view.findViewById(R.id.appoint_time_BTN);
        timeBTN.setEnabled(false);
        timeBTN.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.back_solid_gray));
        timeBTN.setTextColor(Objects.requireNonNull(getContext()).getResources().getColor(R.color.colorWhite));

        requestBTN = view.findViewById(R.id.appoint_request_button);

    }

    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        setListeners();
    }

    private void setListeners() {
        medicalTitle.setText(medicalAnalysis.getTitle());

        if (medicalAnalysis.getPrecautions().toLowerCase().equals("empty")) {
            precautions.setVisibility(View.GONE);
        } else {
            precautions.setVisibility(View.VISIBLE);
            precautions_en.setText(medicalAnalysis.getPrecautions());
        }

        requestBTN.setText("Request (Price: " + medicalAnalysis.getPrice() + " Algos)");

        dateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment.DateSet dateSet = new DatePickerFragment.DateSet() {
                    @Override
                    public void onDateSet(int year, int month, int day) {
                        appointDay = day;
                        appointMonth = month;
                        dateSTR = day + "/" + ++month + "/" + year;
                        Log.d("message", dateSTR);
                        dateBTN.setText("Date: " + dateSTR);
                        timeBTN.setEnabled(true);
                        timeBTN.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.back_solid_white));
                        timeBTN.setTextColor(Objects.requireNonNull(getContext()).getResources().getColor(R.color.colorPrimaryDark));
                        timeBTN.setText("Select Time");
                        timeSTR = "Empty";
                    }
                };
                DialogFragment datePicker = new DatePickerFragment(dateSet, "appoint");
                datePicker.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "Date Picker");
            }
        });
        timeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int currentDay = c.get(Calendar.DAY_OF_MONTH);
                int currentMonth = c.get(Calendar.MONTH);
                int currentHour = c.get(Calendar.HOUR_OF_DAY);
                int currentMinute = c.get(Calendar.MINUTE);

                if (appointMonth == currentMonth && appointDay == currentDay) {
                    RangeTimePickerFragment rangeTimePicker = new RangeTimePickerFragment(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int appointHour, int minute) {
                            String min;
                            if (minute <= 9) {
                                min = "0" + minute;
                            } else {
                                min = "" + minute;
                            }
                            //-----------------------------------------
                            if (appointHour >= 12) {
                                appointPeriod = "PM";
                                appointHour -= 12;
                                if (appointHour == 0) {
                                    appointHour = 12;
                                }
                            } else {
                                appointPeriod = "AM";
                                if (appointHour == 0) {
                                    appointHour = 12;
                                }
                            }
                            //-----------------------------------------
                            timeSTR = appointHour + ":" + min + " " + appointPeriod;
                            timeBTN.setText("Time: " + timeSTR);
                        }
                    }, currentHour + 1, currentMinute, false);
                    rangeTimePicker.setMin(currentHour + 1, 0);
                    rangeTimePicker.show();
                } else {
                    RangeTimePickerFragment rangeTimePicker = new RangeTimePickerFragment(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int appointHour, int minute) {
                            String min;
                            if (minute <= 9) {
                                min = "0" + minute;
                            } else {
                                min = "" + minute;
                            }
                            //-----------------------------------------
                            if (appointHour >= 12) {
                                appointPeriod = "PM";
                                appointHour -= 12;
                                if (appointHour == 0) {
                                    appointHour = 12;
                                }
                            } else {
                                appointPeriod = "AM";
                                if (appointHour == 0) {
                                    appointHour = 12;
                                }
                            }
                            //-----------------------------------------
                            timeSTR = appointHour + ":" + min + " " + appointPeriod;
                            timeBTN.setText("Time: " + timeSTR);
                        }
                    }, currentHour, currentMinute, false);
                    rangeTimePicker.show();
                }
            }
        });
        requestBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSTR.equals("Empty") || timeSTR.equals("Empty")) {
                    CustomToast.darkColor(getContext(), CustomToastType.ERROR, "Please pick data and time!");
                }else if(mnemonic==null){
                    CustomToast.darkColor(getContext(), CustomToastType.INFO, "No wallet attached to your account.Go to wallet settings to activate it.");
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Payment Confirmation Message");
                    builder.setMessage("Are you sure you want to book this appointment and pay " + medicalAnalysis.getPrice() + " Algos ? Note: Transaction fee of 1 Algo will be deducted.");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int amount=Integer.parseInt(medicalAnalysis.getPrice())*1000;
                                    Uri uri = Uri.parse("https://www.example.com/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+mnemonic+"/"+amount);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivityForResult(intent,101);
                                }
                            });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101 && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra("result");
            if(result!=null){
                Log.d("message",result);
            }
            Toast.makeText(context,"Transaction Done!!",Toast.LENGTH_SHORT).show();
            callAPI();
        }
        else{
            Toast.makeText(context,"Transaction Failed!!",Toast.LENGTH_LONG).show();
        }
    }

    private void callAPI() {
        progressViewDialog = new ProgressViewDialog(getContext());
        progressViewDialog.isShowing();
        progressViewDialog.setDialogCancelable(false);
        progressViewDialog.setCanceledOnTouchOutside(false);
        progressViewDialog.showProgressDialog("Requesting an appointment...");

        String p_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (comment.getText().length() == 0) {
            commentSTR = "No Comment Added.";
        } else {
            commentSTR = comment.getText().toString();
        }

        final AppointRequest appointRequest = new AppointRequest(c_id, medicalAnalysis.getId(), p_id,
                "Pending", timeSTR, dateSTR, commentSTR,h_id,"empty");

        final DatabaseReference ref = FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Appointments");
        ref.push().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ref.child(dataSnapshot.getKey()).setValue(appointRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context,"The request has been sent. Check Appointment status in appointment section!",Toast.LENGTH_LONG).show();
                            //CustomToast.darkColor(getContext(), CustomToastType.INFO, "The request has been sent. Check Appointment status in appointment section");
                            progressViewDialog.hideDialog();
                            dismiss();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        ref.push().addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(appointRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError t) {
//                CustomToast.darkColor(getContext(), CustomToastType.ERROR, Objects.requireNonNull(t.getMessage()));
//            }
//        });
    }

    @Override
    public void onTimeSet(int appointHour, int minute) {

        String min;
        if (minute <= 9) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }

        Calendar c = Calendar.getInstance();
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMonth = c.get(Calendar.MONTH);
        int currentPeriod = c.get(Calendar.AM_PM);

        // ------------------------------------------------

        if (appointMonth == currentMonth) {

            if (appointDay == currentDay) {

                if (appointHour >= 12) {
                    appointPeriod = "PM";
                    appointHour -= 12;
                    if (appointHour == 0) {
                        appointHour = 12;
                    }
                } else {
                    appointPeriod = "AM";
                    if (appointHour == 0) {
                        appointHour = 12;
                    }
                }

                if (appointPeriod.equals("AM") && currentPeriod == Calendar.AM) {

                    if (appointHour > currentHour) {
                        timeSTR = appointHour + ":" + min + " " + appointPeriod;
                        timeBTN.setText("Time: " + timeSTR);
                    } else {
                        CustomToast.darkColor(getContext(), CustomToastType.WARNING, "Please choose an incoming hour!");
                        timeBTN.setText("Select Time");
                        timeSTR = "Empty";
                    }

                } else if (appointPeriod.equals("AM") && currentPeriod == Calendar.PM) {

                    CustomToast.darkColor(getContext(), CustomToastType.WARNING, "Please choose an incoming hour!");
                    timeBTN.setText("Select Time");
                    timeSTR = "Empty";

                } else if (appointPeriod.equals("PM") && currentPeriod == Calendar.AM) {

                    timeSTR = appointHour + ":" + min + " " + appointPeriod;
                    timeBTN.setText("Time: " + timeSTR);

                } else if (appointPeriod.equals("PM") && currentPeriod == Calendar.PM) {

                    if (appointHour > currentHour) {
                        timeSTR = appointHour + ":" + min + " " + appointPeriod;
                        timeBTN.setText("Time: " + timeSTR);
                    } else {
                        CustomToast.darkColor(getContext(), CustomToastType.WARNING, "Please choose an incoming hour!");
                        timeBTN.setText("Select Time");
                        timeSTR = "Empty";
                    }

                }

            } else {
                if (appointHour >= 12) {
                    appointPeriod = "PM";
                    appointHour -= 12;
                    if (appointHour == 0) {
                        appointHour = 12;
                    }
                } else {
                    appointPeriod = "AM";
                    if (appointHour == 0) {
                        appointHour = 12;
                    }
                }
                timeSTR = appointHour + ":" + min + " " + appointPeriod;
                timeBTN.setText("Time: " + timeSTR);
            }
        } else {
            if (appointHour >= 12) {
                appointPeriod = "PM";
                appointHour -= 12;
                if (appointHour == 0) {
                    appointHour = 12;
                }
            } else {
                appointPeriod = "AM";
                if (appointHour == 0) {
                    appointHour = 12;
                }
            }
            timeSTR = appointHour + ":" + min + " " + appointPeriod;
            timeBTN.setText("Time: " + timeSTR);
        }
    }

}
