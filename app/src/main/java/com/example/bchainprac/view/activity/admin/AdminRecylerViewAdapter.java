package com.example.bchainprac.view.activity.admin;

import android.app.Activity;
import android.app.Notification;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.net.Uri;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.network.model.ConfirmSignUpForm;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class AdminRecylerViewAdapter extends RecyclerView.Adapter<AdminRecylerViewAdapter.AdminRecylerViewHolder> {

    ArrayList<AdminModel> adminModelList;
    Context context;
    private OkHttpClient mClient = new OkHttpClient();
    private String phoneNo;
    private String body;

    public AdminRecylerViewAdapter(ArrayList<AdminModel> adminModelList,Context context){
        this.adminModelList=adminModelList;
        this.context=context;
    }

    @NonNull
    @Override
    public AdminRecylerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin,parent,false);
        return new AdminRecylerViewAdapter.AdminRecylerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdminRecylerViewHolder holder, final int position) {
        final AdminModel adminModel=adminModelList.get(position);
        final List<String> lists=adminModel.getLists();
        //Log.d("message", String.valueOf(adminModel.isDone()));
        if(adminModel.isDone()){
            holder.btn.setText("");
            holder.btn.setEnabled(false);
            holder.btn.setBackgroundColor(context.getColor(R.color.white));
            holder.btn.setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(R.drawable.ic_check_green),null,null,null);
        }
        if(adminModel.getType().equals("hospital")) {
            if(adminModel.getLists().get(0).equals("owner verification")){
                holder.img.setImageDrawable(context.getDrawable(R.drawable.icon_account_setting));
                holder.title.setText(lists.get(0).toUpperCase());
                holder.description.setText("Verify the hospital owner profile: "+lists.get(1)+","+lists.get(2)+"\n"+"Hospital Id : "+lists.get(3));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adminModel.setDone(true);
                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").child(adminModel.getId()).setValue(adminModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Owners").child(adminModel.getSenderId()).child("verified").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                holder.btn.setText("");
                                                holder.btn.setEnabled(false);
                                                holder.btn.setBackgroundColor(context.getColor(R.color.white));
                                                holder.btn.setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(R.drawable.ic_check_green),null,null,null);
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(lists.get(3)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Hospital h=dataSnapshot.getValue(Hospital.class);
                                                        h.incOwner();
                                                        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(lists.get(3)).child("ownerVerified").setValue(h.getOwnerVerified());
                                                        if(h.getOwnerVerified()==5){
                                                            Toast.makeText(context,"All Owners verified. You can now access your account",Toast.LENGTH_LONG).show();
                                                            sendMessage(h.getHospital_phone(),"All Owners verified. You can not access your account",false);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    });

                                }
                            }
                        });

                    }
                });
            }
            else {
                holder.img.setImageDrawable(context.getDrawable(R.drawable.icon_hospital));
                holder.title.setText(lists.get(0));
                holder.description.setText("Verify the hospital profile \nand send SMS to phone \n" + lists.get(1)+" and UID is \n"+lists.get(2));
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AsyncTask<Void, Void, Boolean>() {

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                if (holder.btn != null)
                                    holder.btn.startAnimation();
                            }

                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                sendMessage(lists.get(1),lists.get(2),true);
                                return true;
                                //sendSMS(lists.get(1), lists.get(2));
                            }

                            @Override
                            protected void onPostExecute(Boolean aVoid) {
                                //Log.d("message",aVoid+"");
                                if(aVoid) {
                                    Bitmap bitmap = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_check_green));
                                    holder.btn.doneLoadingAnimation(Color.WHITE, bitmap);
                                    adminModel.setDone(true);
                                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").child(adminModel.getId()).setValue(adminModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                            HashMap<String,Object> map=new HashMap<>();
//                                            map.put("verified",true);
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(adminModel.getSenderId()).child("verified").setValue(true);
                                            }
                                        }
                                    });
                                }
                                else{
                                    holder.btn.revertAnimation();
                                    //CustomToast.darkColor((Activity)context,CustomToastType.ERROR,"Error sending message");
                                }
                            }
                        }.execute();

                    }
                });
            }
        }
        else if(adminModel.getType().equals("user")){
            holder.img.setImageDrawable(context.getDrawable(R.drawable.icon_user));
            holder.title.setText(lists.get(0));
            holder.description.setText("Add amount to user wallet with address : \n"+lists.get(1));
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            holder.btn.startAnimation();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            setClipboard(context,lists.get(1));
                            String url = "https://bank.testnet.algorand.network/";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            Bitmap bitmap = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_check_green));
                            holder.btn.doneLoadingAnimation(Color.WHITE,bitmap);
                            FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("User").child(adminModel.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User u=dataSnapshot.getValue(User.class);
                                    u.setWalletStatus(2);
                                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("User").child(adminModel.getSenderId()).setValue(u);
                                    //FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Wallet").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    adminModel.setDone(true);
                                    FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Admin").child(adminModel.getId()).setValue(adminModel);
                                    //setButton(holder);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }.execute();
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setClipboard(final Context context, final String text) {
        ContextCompat.getMainExecutor(context).execute(new Runnable() {
            @Override
            public void run() {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(text);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                    clipboard.setPrimaryClip(clip);
                }
            }
        });
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    final String ACCOUNT_SID="ACa04ecd54604939a6f7a701c413e1ee7f";
    final String AUTH_TOKEN="efb14325b5ea01972e66e712e02023c5";

    private void sendMessage(String phone,String uid,Boolean f) {
        String body;
        if(f){
            body ="Your hospital profile is verified .Unique code is : "+uid;
        }
        else{
            body=uid;
        }
        String from = "+16292194532";
        String to = phone;

        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP
        );

        Map<String, String> data = new HashMap<>();
        data.put("From", from);
        data.put("To", to);
        data.put("Body", body);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twilio.com/2010-04-01/").addConverterFactory(GsonConverterFactory.create())
                .build();
        TwilioApi api = retrofit.create(TwilioApi.class);

        api.sendMessage(ACCOUNT_SID, base64EncodedCredentials, data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) Log.d("message", "onResponse->success");
                else Log.d("message", "onResponse->failure");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("message",call.isExecuted()+" "+call);
                Log.d("message", "onFailure");
            }
        });
    }

    interface TwilioApi {
        @FormUrlEncoded
        @POST("Accounts/{ACCOUNT_SID}/SMS/Messages")
        Call<ResponseBody> sendMessage(
                @Path("ACCOUNT_SID") String accountSId,
                @Header("Authorization") String signature,
                @FieldMap Map<String, String> metadata
        );
    }


    public boolean sendSMS(String phone,String uid){

        try {
            // Construct data

            Log.d("message",phone.substring(1));
            // Construct data
            String apiKey = "apikey=" + "NzU1MzU1NzI1OTMzMzc1OTc4NGU0YTc5MzQ3ODM2NTQ=";
            String message = "&message=" + "Hi there, thank you for sending your first test message from Textlocal. Get 20% off today with our code: "+uid;
            String sender = "&sender=" + "600010";
            String numbers = "&numbers=" + phone.substring(1);

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.d("message",line);
                if(line.contains("failure"))
                    return false;
            }
            rd.close();

            return true;
        } catch (Exception e) {
            System.out.println("Error SMS "+e);
            return false;
        }
    }
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phone, null, msg, null, null);
//        }
//        catch (Exception e) {
//            if(e!=null)
//            CustomToast.darkColor(context, CustomToastType.ERROR,e.toString());
//        }

    @Override
    public int getItemCount() {
        return adminModelList.size();
    }

    public class AdminRecylerViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView title,description;
        CircularProgressButton btn;

        public AdminRecylerViewHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.img_admin);
            title=itemView.findViewById(R.id.txtTitle);
            description=itemView.findViewById(R.id.txtDescription);
            btn=itemView.findViewById(R.id.btnAccept);
        }
    }
}
