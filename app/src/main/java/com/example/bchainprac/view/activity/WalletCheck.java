package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.model.AssetHolding;
import com.example.bchainprac.R;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.PopUpDialog;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.network.model.wallet;
import com.example.bchainprac.view.activity.admin.AdminModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labters.lottiealertdialoglibrary.ClickListener;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.nandroidex.upipayments.listener.PaymentStatusListener;
import com.nandroidex.upipayments.models.TransactionDetails;
import com.nandroidex.upipayments.utils.UPIPayment;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import www.sanju.motiontoast.MotionToast;

public class WalletCheck extends AppCompatActivity implements PaymentStatusListener {

    LinearLayout l1,l2,l3;
    Button walletAdd,addAmount;
    TextView balance;
    private UPIPayment upiPayment;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase ;
    DatabaseReference reference;

    ProgressViewDialog progressViewDialog;
    Account account;

    AlgodClient client=null;
    String ALGOD_API_ADDR = "http://hackathon.algodev.network";
    Integer ALGOD_PORT = 9100;
    String ALGOD_API_TOKEN = "ef920e2e7e002953f4b29a8af720efe8e4ecc75ff102b165e0472834b25832c1";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_check);

        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);

        Security.removeProvider("BC");
        Security.insertProviderAt(new BouncyCastleProvider(), 0);

        String providerName = "BC";

        if (Security.getProvider(providerName) == null)
        {
            Log.d("algoDebug",providerName + " provider not installed");
        }
        else
        {
            Log.d("algoDebug",providerName + " is installed.");
        }

        client = (AlgodClient) new AlgodClient(ALGOD_API_ADDR,
                ALGOD_PORT, ALGOD_API_TOKEN);

        l1=findViewById(R.id.wallet_no);
        l2=findViewById(R.id.wallet_new);
        l3=findViewById(R.id.wallet_in_progress);
        walletAdd=findViewById(R.id.walletAdd_btn);
        balance=findViewById(R.id.balance_txt);
        addAmount=findViewById(R.id.walletAddMoney_btn);

        balance.setText("0");

        try {
            Log.d("algos",getWalletBalance(new Account("lyrics basket chapter kitchen maple excite indicate venue tumble digital safe twenty maze sun ugly start toast layer random wedding segment slide unable abstract draw").getAddress())+" ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        auth=FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://doctroid-app-default-rtdb.firebaseio.com/");
        reference =firebaseDatabase.getReference("User");

        //firebaseDatabase.getReference("Admin").removeValue();

        reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                int stat=user.getWalletStatus();
                if(stat==0){
                    l1.setVisibility(View.VISIBLE);
                    l2.setVisibility(View.GONE);
                    l3.setVisibility(View.GONE);
                }
                else if(stat==1){
                    l3.setVisibility(View.VISIBLE);
                    l1.setVisibility(View.GONE);
                    l2.setVisibility(View.GONE);
                }
                else{
                    l2.setVisibility(View.VISIBLE);
                    l1.setVisibility(View.GONE);
                    l3.setVisibility(View.GONE);

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            progressViewDialog=new ProgressViewDialog(WalletCheck.this);
                            progressViewDialog.showProgressDialog("Fetching account. Please wait...");
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            progressViewDialog.hideDialog();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            firebaseDatabase.getReference("Wallet").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    wallet w=dataSnapshot.getValue(wallet.class);
                                    try {
                                        String mnemonic=w.getMenomic();
                                        account=createAccountWithMnemonic(mnemonic);
                                        Long bal=getWalletBalance(account.getAddress());
                                        bal=bal/1000;
                                        balance.setText(String.valueOf(bal));
                                    } catch (Exception e) {
                                        CustomToast.darkColor(WalletCheck.this,CustomToastType.ERROR,e.toString());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    CustomToast.darkColor(WalletCheck.this,CustomToastType.ERROR,databaseError.getMessage());
                                }
                            });
                            return null;
                        }
                    }.execute();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CustomToast.darkColor(WalletCheck.this,CustomToastType.ERROR,databaseError.getMessage());
            }
        });

        walletAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopUpDialog dialog=new PopUpDialog(new PopUpDialog.ErrorDialogListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onOkClick() {
                        final Account account=createAccountWithoutMnemonic();
                        if(account!=null) {
                            l1.setVisibility(View.GONE);
                            l3.setVisibility(View.VISIBLE);
                            l2.setVisibility(View.GONE);
                            final wallet w=new wallet(account.getAddress(),account.toMnemonic());
                            user.setWalletStatus(1);
                            reference.child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        List<String> lists=new ArrayList<>();
                                        lists.add("wallet");
                                        try {
                                            lists.add(account.getAddress().encodeAsString());
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }
                                        final AdminModel adminModel=new AdminModel(lists,"user",firebaseUser.getUid());
                                        firebaseDatabase.getReference("Admin").push().addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                adminModel.setId(dataSnapshot.getKey());
                                                firebaseDatabase.getReference("Admin").child(adminModel.getId()).setValue(adminModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            firebaseDatabase.getReference("Wallet").child(firebaseUser.getUid()).setValue(w);
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            });
                            CustomToast.darkColor(WalletCheck.this, CustomToastType.SUCCESS, "The request has been sent.It will be activated with 2-3 working days.");
                        }
                        else{
                            l2.setVisibility(View.GONE);
                            l1.setVisibility(View.VISIBLE);
                            l3.setVisibility(View.GONE);
                            CustomToast.darkColor(WalletCheck.this, CustomToastType.ERROR, "Error while creating new account.Try again after some time");

                        }
                    }

                    @Override
                    public void onCancelClick() {
                    }
                });
                dialog.showMessageDialog("Confirmation message","Are you sure you want to request wallet for your account ?\nNote: You will be getting 10000 Algos in beggning...",WalletCheck.this);
            }
        });

        addAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpDialog dialog=new PopUpDialog(new PopUpDialog.ErrorDialogListener() {
                    @Override
                    public void onOkClick() {
                        startUpiPayment();
                        l1.setVisibility(View.GONE);
                        l2.setVisibility(View.VISIBLE);
                        l3.setVisibility(View.GONE);
                        ///CustomToast.Companion.darkColor(walletCheck.this, CustomToastType.SUCCESS, "The request has been sent.It will be activated with 2-3 working days.");
                    }

                    @Override
                    public void onCancelClick() {
                    }
                });
                dialog.showMessageDialog("Confirmation message","Are you sure you want to add 10000 algos to your wallet.\nNote: it will charge you Rs 10. You cannot pay lesser than this.",WalletCheck.this);
            }
        });
    }

    public   Account createAccountWithMnemonic(String mnemonic){
        Account myAccount1= null;
        try {
            myAccount1 = new Account(mnemonic);
            Log.d("algoDebug"," algo account address: " + myAccount1.getAddress());
            Log.d("algoDebug"," algo account MNEMONIC: " + myAccount1.toMnemonic());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("algoDebug"," Eror while creating new account "+e);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return  myAccount1;
    }

    private void startUpiPayment() {
        long millis = System.currentTimeMillis();
        upiPayment = new UPIPayment.Builder()
                .with(WalletCheck.this)
                .setPayeeVpa(getString(R.string.vpa))
                .setPayeeName(getString(R.string.payee))
                .setTransactionId(Long.toString(millis))
                .setTransactionRefId(Long.toString(millis))
                .setDescription(getString(R.string.transaction_description))
                .setAmount(getString(R.string.amount))
                .build();

        upiPayment.setPaymentStatusListener(this);

        if (upiPayment.isDefaultAppExist()) {
            onAppNotFound();
            return;
        }

        upiPayment.startPayment();
    }

    @Override
    public void onAppNotFound() {
        Toast.makeText(this, "App Not Found", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTransactionCancelled() {

        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionCompleted(@Nullable TransactionDetails transactionDetails) {



        String status = null;
        String approvalRefNo = null;
        if (transactionDetails != null) {
            status = transactionDetails.getStatus();
            Log.d("message",status);
            approvalRefNo = transactionDetails.getApprovalRefNo();
        }
        boolean success = false;
        if (status != null) {
            success = status.equalsIgnoreCase("success") || status.equalsIgnoreCase("submitted");
        }

        if(success){
            List<String> lists=new ArrayList<>();
            lists.add("wallet");
            try {
                lists.add(account.getAddress().encodeAsString());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            final AdminModel adminModel=new AdminModel(lists,"user",firebaseUser.getUid());
            firebaseDatabase.getReference("Admin").push().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    adminModel.setId(dataSnapshot.getKey());
                    firebaseDatabase.getReference("Admin").child(adminModel.getId()).setValue(adminModel);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        int dialogType = success ? DialogTypes.TYPE_SUCCESS : DialogTypes.TYPE_ERROR;
        String title = success ? "Good job!" : "Oops!";
        String description = success ? ("UPI ID :" + approvalRefNo) : "Transaction Failed/Cancelled";
        int buttonColor = success ? Color.parseColor("#00C885") : Color.parseColor("#FB2C56");
        LottieAlertDialog alertDialog = new LottieAlertDialog.Builder(this, dialogType)
                .setTitle(title)
                .setDescription(description+"\n"+"Amount will be reflected in your wallet within 24 hrs.Please be patience!!!")
                .setNoneText("Okay")
                .setNoneTextColor(Color.WHITE)
                .setNoneButtonColor(buttonColor)
                .setNoneListener(new ClickListener() {
                    @Override
                    public void onClick(@NotNull LottieAlertDialog lottieAlertDialog) {
                        lottieAlertDialog.dismiss();
                    }
                })
                .build();
        alertDialog.setCancelable(false);
        alertDialog.show();
        upiPayment.detachListener();
    }

    @Override
    public void onTransactionFailed() {
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTransactionSubmitted() {

        Toast.makeText(this, "Pending | Submitted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionSuccess() {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

    }

    public Account createAccountWithoutMnemonic( ){
        Account myAccount= null;

        try {
            myAccount = new Account();
            Log.d("algoDebug"," algo account address: " + myAccount.getAddress());
            Log.d("algoDebug"," algo account MNEMONIC: " + myAccount.toMnemonic());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("algoDebug"," Error while creating new account "+e);
        }
        return myAccount;
    }

    private AlgodClient connectToNetwork() {
        client = new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);
        return client;
    }

    public  Long getWalletBalance(Address address) throws Exception {
        //client=connectToNetwork();
        com.algorand.algosdk.v2.client.model.Account accountInfo = client.AccountInformation(address).execute().body();
//        Log.d("algoDebug",accountInfo.toString());
        for(AssetHolding asset:accountInfo.assets){
            Log.d("aldoDebug",asset.toString());
        }
        Log.d("algoDebug","Account Balance: "+ accountInfo.amount+" microAlgos");
        return  accountInfo.amount;
    }

}
