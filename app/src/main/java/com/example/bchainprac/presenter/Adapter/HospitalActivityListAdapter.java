package com.example.bchainprac.presenter.Adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.Hospital;

import java.util.List;

public class HospitalActivityListAdapter extends RecyclerView.Adapter<HospitalActivityListAdapter.HospitalActivityListHolder> {
    private Context context;
    private List<Pair<Hospital,String>> list;
    private ItemClick itemClick;

    public HospitalActivityListAdapter(Context context, List<Pair<Hospital,String>> list,ItemClick itemClick){
        this.context=context;
        this.list=list;
        this.itemClick=itemClick;
    }

    @NonNull
    @Override
    public HospitalActivityListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital_list, parent, false);
        return new HospitalActivityListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalActivityListHolder holder, final int position) {
        Hospital hospital=list.get(position).first;
        holder.txt.setText(hospital.getHospital_name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HospitalActivityListHolder extends RecyclerView.ViewHolder {

        TextView txt;
        public HospitalActivityListHolder(@NonNull View itemView) {
            super(itemView);
            txt=itemView.findViewById(R.id.hospital_name_id);
        }
    }
    public interface ItemClick {

        void onClick(int position);
    }
}
