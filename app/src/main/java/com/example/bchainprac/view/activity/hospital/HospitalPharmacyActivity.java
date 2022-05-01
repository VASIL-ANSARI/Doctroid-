package com.example.bchainprac.view.activity.hospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.dialog.ProgressViewDialog;
import com.example.bchainprac.network.model.Medicine;
import com.example.bchainprac.presenter.Adapter.HospitalPharmacyAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HospitalPharmacyActivity extends AppCompatActivity {

    private static final int MAX_LIMIT = 20;
    RecyclerView recyclerView;
    ImageButton imgBtn;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseUser user;
    private List<Medicine> medicineList;
    private HospitalPharmacyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_pharmacy);
        imgBtn=findViewById(R.id.hospital_medicine_add_btn);
        recyclerView=findViewById(R.id.hospital_medicine_recycler_view);

        database= FirebaseDatabase.getInstance(Constants.FIREBASE_URL);
        ref=database.getReference("Medicines");
        user= FirebaseAuth.getInstance().getCurrentUser();

        imgBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addMedicineCategory();
            }
        });

        medicineList=new ArrayList<>();
        adapter=new HospitalPharmacyAdapter(medicineList, HospitalPharmacyActivity.this, new HospitalPharmacyAdapter.ItemClick() {
            @Override
            public void onClick(int position) {
               final  Medicine md=medicineList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(HospitalPharmacyActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_qty_dialog, null);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();

                Button inc,desc,done,cancel;
                final TextView txt;
                TextView name=dialogView.findViewById(R.id.txtMedicine_name);
                name.setText(md.getName());
                inc=dialogView.findViewById(R.id.inc_btn);
                desc=dialogView.findViewById(R.id.desc_btn);
                cancel=dialogView.findViewById(R.id.cancel_dialog);
                done=dialogView.findViewById(R.id.done_dialog);
                txt=dialogView.findViewById(R.id.quantity_text_view);

                inc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int addQty=Integer.parseInt(txt.getText().toString());
                        if(addQty==MAX_LIMIT){
                            Toast.makeText(HospitalPharmacyActivity.this,"You can't exceed max limit",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            addQty++;
                            txt.setText(String.valueOf(addQty));
                        }
                    }
                });

                desc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int addQty=Integer.parseInt(txt.getText().toString());
                        if(addQty==0){
                            Toast.makeText(HospitalPharmacyActivity.this,"You can't add less than 0 medicine",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            addQty--;
                            txt.setText(String.valueOf(addQty));
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                done.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        String addQty=txt.getText().toString();
                        if(addQty.startsWith("0")){
                            Toast.makeText(HospitalPharmacyActivity.this,"Select valid quantity to add",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            int qty = Integer.parseInt(md.getQuantity());
                            md.setQuantity(String.valueOf(qty + Integer.parseInt(addQty)));
                            ref.child(md.getId()).setValue(md).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        callApi();
                                        alertDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });

                alertDialog.show();
            }
        });
        recyclerView.setAdapter(adapter);

        callApi();


    }

    private void callApi() {
        medicineList.clear();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Medicine medicine=snapshot.getValue(Medicine.class);
                    if(medicine.getHospitalId().equals(user.getUid())){
                        medicineList.add(medicine);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMedicineCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_pharmacy_dialog_box, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        final EditText title=dialogView.findViewById(R.id.medicine_name_item);
        final EditText description =dialogView.findViewById(R.id.medicine_description_item);
        final EditText price=dialogView.findViewById(R.id.medicine_price_item);
        final EditText qty=dialogView.findViewById(R.id.medicine_qty_item);
        final Spinner spinner=dialogView.findViewById(R.id.brand_item);
        Button add=dialogView.findViewById(R.id.add_medicine_category);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().length()==0){
                    createToast("Enter medicine name");
                    title.requestFocus();
                }
                else if(description.getText().toString().length()==0){
                    createToast("Enter description");
                    description.requestFocus();
                }
                else if(price.getText().toString().length()==0){
                    createToast("Enter price");
                    price.requestFocus();
                }
                else if(qty.getText().toString().length()==0 || qty.getText().toString().equals("0")){
                    createToast("Enter  valid quantity");
                    qty.requestFocus();
                }
                else if(spinner.getSelectedItem().toString().equals("NONE")){
                    createToast("Select valid brand");
                }
                else{
                    final Medicine medicine=new Medicine("",title.getText().toString(),price.getText().toString(),description.getText().toString(),qty.getText().toString(),spinner.getSelectedItem().toString(),user.getUid());
                    ref.push().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            medicine.setId(dataSnapshot.getKey());
                            ref.child(medicine.getId()).setValue(medicine);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();
    }

    private void createToast(String s) {
        Toast.makeText(HospitalPharmacyActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
