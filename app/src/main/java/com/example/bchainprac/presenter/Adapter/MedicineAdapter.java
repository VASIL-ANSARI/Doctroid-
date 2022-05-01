package com.example.bchainprac.presenter.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.app.Constants;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.network.model.MedicalAnalysis;
import com.example.bchainprac.network.model.Medicine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> implements Filterable {
    List<Medicine> medicineList;
    Context context;
    List<Medicine> searchList;
    List<String> Ids;

    ItemClick itemClick;
    public MedicineAdapter(List<Medicine> medicineList, Context context,ItemClick itemClick,List<String> Ids) {
        this.medicineList = medicineList;
        this.context = context;
        this.itemClick=itemClick;
        this.Ids=Ids;
        searchList = new ArrayList<>(medicineList);
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine,parent,false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MedicineViewHolder holder, final int position) {
        Medicine medicine=medicineList.get(position);
        if(Ids.contains(medicine.getId())){
            holder.img.setVisibility(View.VISIBLE);
        }
        holder.title.setText(medicine.getName());
        holder.description.setText(medicine.getDescription());
        holder.price.setText(medicine.getPrice());
        holder.expiryDate.setText(medicine.getBrand());
        holder.qty.setText(medicine.getQuantity());
       // final String name=medicine.getName();

//        FirebaseDatabase.getInstance(Constants.FIREBASE_URL).getReference("Hospital").child(medicine.getHospitalId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    Hospital h = dataSnapshot.getValue(Hospital.class);
//                    holder.title.setText(name + " (" + h.getHospital_name() + ") ");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position,holder.title.getText().toString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //searchList = new ArrayList<>(medicineList);
            List<Medicine> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(searchList);
            } else {
                Log.d("message",constraint.toString() + searchList.size());
                String filteredPattern = constraint.toString().toLowerCase().trim();
                for (Medicine medicalAnalysis : searchList) {
                    Log.d("message",medicalAnalysis.getName().toLowerCase());
                    if (medicalAnalysis.getName().toLowerCase().contains(filteredPattern)) {
                        filteredList.add(medicalAnalysis);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            medicineList.clear();
            medicineList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
    public void notifyAdapterDataSetChanged(List<Medicine> medicines) {
        searchList=new ArrayList<>(medicines);
        notifyDataSetChanged();
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder{


        TextView title,description,price,qty,expiryDate;
        Button btn;
        ImageView img;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);

            title=itemView.findViewById(R.id.medicine_title);
            description=itemView.findViewById(R.id.medicine_description);
            price=itemView.findViewById(R.id.medicine_price);
            qty=itemView.findViewById(R.id.medicine_qty);
            expiryDate=itemView.findViewById(R.id.medicine_expiry_date);
            img=itemView.findViewById(R.id.idUserMedicine);

            btn=itemView.findViewById(R.id.request_medicine);

        }
    }

    public interface ItemClick{
        void onClick(int position,String s);
    }

}
