package com.example.bchainprac.presenter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.Medicine;

import java.util.List;

public class HospitalPharmacyAdapter extends RecyclerView.Adapter<HospitalPharmacyAdapter.HospitalPharmacyViewHolder>{
    List<Medicine> medicineList;
    Context context;
    private ItemClick itemClick;

    public HospitalPharmacyAdapter(List<Medicine> medicineList,Context context,ItemClick itemClick){
        this.medicineList=medicineList;
        this.context=context;
        this.itemClick=itemClick;
    }

    @NonNull
    @Override
    public HospitalPharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pharmacy_medicines, parent, false);
        return new HospitalPharmacyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HospitalPharmacyViewHolder holder, final int position) {
        Medicine medicine=medicineList.get(position);
        holder.name.setText(medicine.getName());
        holder.description.setText(medicine.getDescription());
        holder.price.setText(medicine.getPrice());
        holder.qty.setText(medicine.getQuantity());
        holder.brand.setText(medicine.getBrand());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.description.getVisibility()==View.GONE){
                    holder.description.setVisibility(View.VISIBLE);
                    holder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                }
                else{
                    holder.description.setVisibility(View.GONE);
                    holder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.colorHide));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public class HospitalPharmacyViewHolder extends RecyclerView.ViewHolder{

        TextView name,description,price,qty,brand;
        ConstraintLayout constraintLayout;
        LinearLayout linearLayout;

        public HospitalPharmacyViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.medicineName);
            description=itemView.findViewById(R.id.medicineCategory_desc_textView);
            price=itemView.findViewById(R.id.medicine_price);
            qty=itemView.findViewById(R.id.medicine_qty);
            brand=itemView.findViewById(R.id.medicine_brand);

            constraintLayout=itemView.findViewById(R.id.medicineCategory_desc_constraintLayout);
            linearLayout=itemView.findViewById(R.id.medicineCategory_info_icon);


        }
    }

    public interface ItemClick {

        void onClick(int position);
    }
}
