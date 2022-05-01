package com.example.bchainprac.presenter.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.example.bchainprac.network.model.Appoint;
import com.example.bchainprac.presenter.holder.AppointHolder;

import java.util.ArrayList;
import java.util.List;

public class HospitalAppointAdapter  extends RecyclerView.Adapter<HospitalAppointAdapter.HospitalApointHolder> implements Filterable {


    private List<Appoint> AppointList;
    private List<Appoint> searchList;
    private Context context;
    private AppointAdapter.ItemClick itemClick;

    public HospitalAppointAdapter(Context context, List<Appoint> items, AppointAdapter.ItemClick itemClick) {

        this.context = context;
        this.AppointList = items;
        this.itemClick = itemClick;
        this.searchList=new ArrayList<>(items);
    }

    private Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public HospitalApointHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appoint, parent, false);
        return new HospitalApointHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalApointHolder holder,final int position) {
        Appoint appoint = AppointList.get(position);

        holder.appointTitle.setText(appoint.getTitle());
        holder.precautions_en.setText(appoint.getPre_en());
        holder.precautions_ar.setText(appoint.getPre_ar());
        holder.appointDate.setText(appoint.getDate());
        holder.appointTime.setText(appoint.getTime());
        holder.comment.setText(appoint.getComment());

        if (appoint.getStatus().toLowerCase().equals("rejected")) {
            holder.appointNote.setText("Rejected Appointment");
            holder.appointStatus.setImageDrawable(getContext().getDrawable(R.drawable.icon_rejected));
            holder.constraint.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
            holder.appointTime.setTextColor(getContext().getResources().getColor(R.color.colorRed));
            holder.appointDate.setTextColor(getContext().getResources().getColor(R.color.colorRed));
            holder.appointDelete.setVisibility(View.GONE);
        } else if (appoint.getStatus().toLowerCase().equals("accepted")) {
            holder.appointNote.setText("Accepted Appointment");
            holder.appointStatus.setImageDrawable(getContext().getDrawable(R.drawable.icon_accepted));
            holder.constraint.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
            holder.appointTime.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            holder.appointDate.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            holder.appointDelete.setVisibility(View.GONE);
        } else {
            holder.appointNote.setText("Pending Appointment");
            holder.appointStatus.setImageDrawable(getContext().getDrawable(R.drawable.icon_loading));
            holder.constraint.setBackgroundColor(getContext().getResources().getColor(R.color.colorGray));
            holder.appointTime.setTextColor(getContext().getResources().getColor(R.color.colorGray));
            holder.appointDate.setTextColor(getContext().getResources().getColor(R.color.colorGray));
            holder.appointDelete.setVisibility(View.VISIBLE);
        }

        holder.appointDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return AppointList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Appoint> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(searchList);
                } else {
                    String filteredPattern = constraint.toString().toLowerCase().trim();
                    for (Appoint appoint : searchList) {
                        if (appoint.getTitle().toLowerCase().contains(filteredPattern)) {
                            filteredList.add(appoint);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                AppointList.clear();
                AppointList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public void notifyAdapterChanged(List<Appoint> items){
        searchList=new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public class HospitalApointHolder extends RecyclerView.ViewHolder {
        public LinearLayout precautionsLinearLayout;
        public TextView appointTitle;
        public TextView precautions_en;
        public TextView precautions_ar;
        public TextView appointDate;
        public TextView appointTime;
        public TextView appointNote;
        public TextView comment;

        public ConstraintLayout constraint;
        public ImageView appointStatus;
        public ImageView appointDelete;
        public TextView txt;

        public HospitalApointHolder(View view) {
            super(view);

            txt=view.findViewById(R.id.txtHospitalComment);
            appointTitle = view.findViewById(R.id.item_appoint_title);
            appointDate = view.findViewById(R.id.item_appoint_date);
            appointTime = view.findViewById(R.id.item_appoint_time);
            appointStatus = view.findViewById(R.id.item_appoint_icon);
            appointDelete = view.findViewById(R.id.item_appoint_delete);
            appointNote = view.findViewById(R.id.item_appoint_note);
            precautionsLinearLayout = view.findViewById(R.id.item_appoint_precautions_linearLayout);
            precautions_en = view.findViewById(R.id.item_appoint_en_precautions_textView);
            precautions_ar = view.findViewById(R.id.item_appoint_ar_precautions_textView);
            comment = view.findViewById(R.id.item_appoint_comment_textView);

            constraint = view.findViewById(R.id.item_appoint_constraint);
            txt.setText("User Comment");
            appointDelete.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_track_changes));
        }
    }
}
