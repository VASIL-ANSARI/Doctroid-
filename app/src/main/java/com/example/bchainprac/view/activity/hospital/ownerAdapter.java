package com.example.bchainprac.view.activity.hospital;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.ConfirmSignUpForm;

import java.util.List;
import java.util.zip.Inflater;

public class ownerAdapter extends RecyclerView.Adapter<ownerAdapter.ownerViewHolder>{

    private Context context;
    private List<ConfirmSignUpForm> owners;
    private ItemClick itemClick;
    public ownerAdapter(Context context, List<ConfirmSignUpForm> owners,ItemClick itemClick){
        this.context=context;
        this.owners=owners;
        this.itemClick=itemClick;
    }
    @NonNull
    @Override
    public ownerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View img_view= LayoutInflater.from(context).inflate(R.layout.item_owner,parent,false);
        return new ownerViewHolder(img_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ownerViewHolder holder, final int position) {
        ConfirmSignUpForm form=owners.get(position);

        holder.name.setText(form.getName());
        holder.designation.setText(form.getOwnerType());
        holder.city.setText(form.getCity());
        holder.dob.setText(form.getDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return owners.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ownerViewHolder extends RecyclerView.ViewHolder{
        TextView name,designation,dob,city;

        public ownerViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.item_medicineList_name);
            designation=itemView.findViewById(R.id.item_medicineList_price);
            dob=itemView.findViewById(R.id.item_dob_owner);
            city=itemView.findViewById(R.id.item_city_owner);
        }
    }

    public interface ItemClick {

        void onClick(int position);
    }
}
