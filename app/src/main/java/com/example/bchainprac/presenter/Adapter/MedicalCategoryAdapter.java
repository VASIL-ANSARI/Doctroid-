package com.example.bchainprac.presenter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.MedicalCategory;
import com.example.bchainprac.network.model.Medicine;
import com.example.bchainprac.presenter.holder.MedicalCategoryHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MedicalCategoryAdapter extends RecyclerView.Adapter<MedicalCategoryHolder> implements Filterable {


    private List<MedicalCategory> medicalList;
    private List<MedicalCategory> searchList;
    private Context context;
    private ItemClick itemClick;

    public MedicalCategoryAdapter(Context context, List<MedicalCategory> items, ItemClick itemClick) {

        this.context = context;
        this.medicalList = items;
        this.itemClick = itemClick;
        searchList = new ArrayList<>(medicalList);
    }

    public void addItem(MedicalCategory medicalCategory) {
        medicalList.add(medicalCategory);
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        medicalList.remove(position);
        notifyDataSetChanged();
    }

    private Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public MedicalCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_category, parent, false);
        return new MedicalCategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MedicalCategoryHolder holder,final int position) {

        MedicalCategory medicalCategory = medicalList.get(position);

        holder.medicalText.setText(medicalCategory.getName());
        holder.medicalDesc.setText(medicalCategory.getDesc());

        Picasso.with(context)
                .load(medicalList.get(position).getImage())
                .fit()
                .error(R.drawable.icon_no_connection)
                .into(holder.medicalImage);

        holder.medicalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.medicalDesc.getVisibility() == View.GONE) {
                    holder.medicalDesc.setVisibility(View.VISIBLE);
                    holder.medicalConstraint.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.medicalDesc.setVisibility(View.GONE);
                    holder.medicalConstraint.setBackgroundColor(getContext().getResources().getColor(R.color.colorHide));
                }
            }
        });

        holder.medicalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position);
            }
        });

    }

    public void notifyAdapterDataSetChanged(List<MedicalCategory> medicines) {
        searchList=new ArrayList<>(medicines);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return medicalList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<MedicalCategory> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(searchList);
            } else {
                String filteredPattern = constraint.toString().toLowerCase().trim();
                for (MedicalCategory category : searchList) {
                    if (category.getName().toLowerCase().contains(filteredPattern)) {
                        filteredList.add(category);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            medicalList.clear();
            medicalList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public interface ItemClick {

        void onClick(int position);
    }
}
