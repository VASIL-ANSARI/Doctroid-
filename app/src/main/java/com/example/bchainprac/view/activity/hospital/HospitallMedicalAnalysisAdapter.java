package com.example.bchainprac.view.activity.hospital;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.MedicalAnalysis;

import java.util.ArrayList;
import java.util.List;

public class HospitallMedicalAnalysisAdapter extends RecyclerView.Adapter<HospitallMedicalAnalysisAdapter.HospitalMedicalAnalysisHolder> {
    private List<MedicalAnalysis> medicalList;
    private Context context;

    public HospitallMedicalAnalysisAdapter(Context context, List<MedicalAnalysis> items) {

        this.context = context;
        this.medicalList = items;
    }

    @NonNull
    @Override
    public HospitalMedicalAnalysisHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_analysis, parent, false);
        return new HospitalMedicalAnalysisHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HospitalMedicalAnalysisHolder holder,final int position) {
        MedicalAnalysis medicalAnalysis = medicalList.get(position);

        holder.medicalAnalysisTitle.setText(medicalAnalysis.getTitle());
        holder.medicalAnalysisPrice.setText("Price: " + medicalAnalysis.getPrice() + " Algo");
        holder.medicalAnalysisPeriod.setText("Period: " + medicalAnalysis.getPeriod() + " Day(s)");



        holder.medicalAnalysisAppoint.setText(medicalAnalysis.getPrecautions());



        holder.medicalAnalysisLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.medicalAnalysisInfoGroup.getVisibility() == View.GONE) {
                    holder.medicalAnalysisInfoGroup.setVisibility(View.VISIBLE);
                    holder.medicalAnalysisDropArrow.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_drop_down));
                } else {
                    holder.medicalAnalysisInfoGroup.setVisibility(View.GONE);
                    holder.medicalAnalysisDropArrow.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_drop_down_gray));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicalList.size();
    }

    public class HospitalMedicalAnalysisHolder extends RecyclerView.ViewHolder {

        public TextView medicalAnalysisTitle, medicalAnalysisPrice, medicalAnalysisPeriod;

        public LinearLayout medicalAnalysisLinear;
        public ImageView medicalAnalysisDropArrow;
        public CardView medicalAnalysisCardView;
        public Button medicalAnalysisAppoint;
        public Group medicalAnalysisInfoGroup;


        public HospitalMedicalAnalysisHolder(View view) {
            super(view);
            medicalAnalysisTitle = view.findViewById(R.id.item_medicalAnalysis_title);
            medicalAnalysisPrice = view.findViewById(R.id.item_medicalAnalysis_price);
            medicalAnalysisPeriod = view.findViewById(R.id.item_medicalAnalysis_period);

            medicalAnalysisLinear = view.findViewById(R.id.item_medicalAnalysis_linearLayout);
            medicalAnalysisDropArrow = view.findViewById(R.id.item_medicalAnalysis_dropArrow);
            medicalAnalysisCardView = view.findViewById(R.id.item_medicalAnalysis_cardView);
            medicalAnalysisAppoint = view.findViewById(R.id.item_medicalAnalysis_appoint_button);
            medicalAnalysisInfoGroup = view.findViewById(R.id.item_medicalAnalysis_info_group);
        }
    }
}
