package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.algod.client.ApiException;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.algod.TransactionParams;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.AssetHolding;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Assets;
import com.example.bchainprac.network.model.Medicine;
import com.example.bchainprac.network.model.User;
import com.example.bchainprac.network.model.wallet;
import com.example.bchainprac.presenter.Adapter.MedicineAdapter;
import com.example.bchainprac.utilities.InternetUtilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Text;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class MedicineActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicineAdapter adapter;
    List<Medicine> medicineList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    LinearLayout layout;
    SearchView searchView;
    private List<Long> assetIds;
    private List<String> medicineIds;

    AlgodClient client=null;
    Account acct2;
    String ALGOD_API_ADDR = "http://hackathon.algodev.network";
    Integer ALGOD_PORT = 9100;
    String ALGOD_API_TOKEN = "ef920e2e7e002953f4b29a8af720efe8e4ecc75ff102b165e0472834b25832c1";
    String mnemonic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mnemonic=null;
        assetIds=null;
        medicineIds=new ArrayList<>();
        client = (AlgodClient) new AlgodClient(ALGOD_API_ADDR,
                ALGOD_PORT, ALGOD_API_TOKEN);


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



        recyclerView=findViewById(R.id.recyclerIdMedicine);
        layout=findViewById(R.id.medicine_searchLinear);
        searchView=findViewById(R.id.medicine_searchView);
        medicineList=new ArrayList<>();
        MedicineAdapter.ItemClick itemClick=new MedicineAdapter.ItemClick() {
            @Override
            public void onClick(int position,String s) {
                showAlertDialog(medicineList.get(position),s);
            }
        };
        adapter=new MedicineAdapter(medicineList,MedicineActivity.this,itemClick,medicineIds);
        recyclerView.setAdapter(adapter);

        final ProgressViewDialog progressViewDialog=new ProgressViewDialog(MedicineActivity.this);
        progressViewDialog.showProgressDialog("Fetching Medicines...");
        fetchMnemonic();

        database=FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
        reference=database.getReference("Medicines");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                medicineList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    layout.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.VISIBLE);
                    Medicine medicine=snapshot.getValue(Medicine.class);
                    medicineList.add(medicine);
                    adapter.notifyAdapterDataSetChanged(medicineList);
                }
                progressViewDialog.hideDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }


        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==202 && resultCode== Activity.RESULT_OK){
            String result=data.getStringExtra("result");
            if(result!=null){
                Log.d("message",result);
                callAPI(result);
            }
            Toast.makeText(MedicineActivity.this,"Asset Created Successfully!!",Toast.LENGTH_LONG).show();

        }
        else{
            Toast.makeText(MedicineActivity.this,"Asset Creation Failed!!",Toast.LENGTH_LONG).show();
        }
    }

    private void callAPI(String result) {
        final String[] strs=result.split("//");
        reference.child(strs[2]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Medicine md=dataSnapshot.getValue(Medicine.class);
                int left=Integer.parseInt(md.getQuantity())-Integer.parseInt(strs[1]);
                md.setQuantity(String.valueOf(left));
                medicineIds.add(md.getId());
                adapter.notifyDataSetChanged();
                reference.child(md.getId()).setValue(md).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final Assets assets=new Assets(strs[0],strs[2],FirebaseAuth.getInstance().getCurrentUser().getUid());
                        database.getReference("Assets").push().addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                assets.setId(dataSnapshot.getKey());
                                database.getReference("Assets").child(assets.getId()).setValue(assets);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        CustomToast.darkColor(MedicineActivity.this,CustomToastType.SUCCESS,"Asset Created. Info "+strs[0]);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CustomToast.darkColor(MedicineActivity.this,CustomToastType.ERROR,"Session Failed!!");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public  void getWalletBalance(Address address) throws Exception {
        assetIds=new ArrayList<>();
        //client=connectToNetwork();
        com.algorand.algosdk.v2.client.model.Account accountInfo = client.AccountInformation(address).execute().body();

        for(AssetHolding asset:accountInfo.assets){
            Log.d("message",asset.amount+" "+asset.assetId);
            assetIds.add(asset.assetId);
           // Log.d("aldoDebug",asset.toString());
        }
        Log.d("algoDebug","Account Balance: "+ accountInfo.amount+" microAlgos");
    }

    private void fetchMnemonic() {
//        final ProgressViewDialog progressViewDialog=new ProgressViewDialog(MedicineActivity.this);
//        progressViewDialog.showProgressDialog("Loading...");

        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Wallet").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            final wallet w = dataSnapshot.getValue(wallet.class);
                            FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User u=dataSnapshot.getValue(User.class);
                                    if(u.getWalletStatus()==2){
                                        mnemonic = w.getMenomic();
                                        try {
                                            getWalletBalance(createAccountWithMnemonic(mnemonic).getAddress());
                                            if(assetIds!=null){
                                                FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Assets").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                                            Assets assets=snapshot.getValue(Assets.class);
                                                            Long iid=Long.parseLong(assets.getAssetId().split(" ")[1]);
                                                            Log.d("message",iid+" ");
                                                            if(assetIds.contains(iid) && assets.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                                medicineIds.add(assets.getMedicineId());
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        //progressViewDialog.hideDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void showAlertDialog(final Medicine medicine,String s) {
        if(mnemonic!=null){
            Log.d("message",mnemonic);
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.buy_medicine_dialog, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        TextView name=dialogView.findViewById(R.id.medicine_name);
        final EditText qty=dialogView.findViewById(R.id.qty_buy);
        final CircularProgressButton add=dialogView.findViewById(R.id.buy_medicine);
        name.setText(s);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qty.getText().toString().isEmpty() || qty.getText().toString().startsWith("0")){
                    Toast.makeText(MedicineActivity.this,"Enter valid Quantity",Toast.LENGTH_LONG).show();
                }
                else if(Integer.parseInt(qty.getText().toString())>Integer.parseInt(medicine.getQuantity())){
                    Toast.makeText(MedicineActivity.this,"Quantity Limit Exceeded",Toast.LENGTH_LONG).show();
                }
                else{
                    Medicine pp=new Medicine(medicine.getId(),medicine.getName(),medicine.getPrice(),medicine.getDescription(),qty.getText().toString(),medicine.getBrand(),medicine.getHospitalId());
                    if(mnemonic==null){
                        CustomToast.darkColor(MedicineActivity.this,CustomToastType.INFO,"No active wallet found!!");
                    }
                    else{
                            int amount=Integer.parseInt(pp.getQuantity())*Integer.parseInt(pp.getPrice());
                            Uri uri = Uri.parse("https://www.example.com/"+ pp.getId()+"/"+pp.getQuantity()+"/"+mnemonic+"/"+amount);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivityForResult(intent,202);
                            alertDialog.dismiss();
                    }

                }

            }
        });

        alertDialog.show();
    }
}