package com.example.smartalertapp.Classes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartalertapp.R;

import java.util.ArrayList;

public class RecyclerViewAdapterIncident extends RecyclerView.Adapter<RecyclerViewAdapterIncident.MyViewHolder> {


    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<Incident> IncidentList;


    public RecyclerViewAdapterIncident(RecyclerViewInterface recyclerViewInterface, Context context, ArrayList<Incident> IncidentList) {
        this.recyclerViewInterface = recyclerViewInterface;
        this.context = context;
        this.IncidentList = IncidentList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_holder_incident, parent, false);
        return new RecyclerViewAdapterIncident.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if (IncidentList.get(position).Type == IncidentType.FIRE){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.fire_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_fire));
        }
        else if ((IncidentList.get(position).Type == IncidentType.EARTHQUAKE)){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.earthquake_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_earthquake));
        }
        else if ((IncidentList.get(position).Type == IncidentType.FLOOD)){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.flood_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_flood));
        }
        else if ((IncidentList.get(position).Type == IncidentType.TORNADO)){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.tornado_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_tornado));
        }
        else if ((IncidentList.get(position).Type == IncidentType.TSUNAMI)){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.tsunami_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_tsunami));
        }
        else if ((IncidentList.get(position).Type == IncidentType.AVALANCHE)){
            holder.imageViewType.setImageDrawable(context.getResources().getDrawable(R.drawable.avalanche_icon, context.getTheme()));
            holder.textViewType.setText(context.getString(R.string.incident_type_avalanche));
        }

        holder.textViewLocation.setText(context.getString(R.string.recycler_view_location_longitude_latitude_together, IncidentList.get(position).Center_latitude, IncidentList.get(position).Center_longitude));
        holder.textViewRadius.setText(context.getString(R.string.recycler_view_radius, (IncidentList.get(position).Radius + IncidentList.get(position).Type.get_Extra_radius()) ));
        if (IncidentList.get(position).ReportList.size() == 1){
            holder.textViewReportNum.setText(context.getString(R.string.recycler_view_reports_count_1, IncidentList.get(position).ReportList.size()));
        }
        else{
            holder.textViewReportNum.setText(context.getString(R.string.recycler_view_reports_count_more, IncidentList.get(position).ReportList.size()));
        }
        holder.textViewDangerScore.setText(context.getString(R.string.recycler_view_danger_degree, IncidentList.get(position).Overall_Danger_Score));


        if (IncidentList.get(position).Condition.equals(Incident.CONDITION_ACCEPTED)){
            holder.textViewCondition.setTextColor(Color.rgb(103, 170, 120));
            holder.textViewCondition.setText(context.getString(R.string.incident_condition_accepted));
        }
        else if (IncidentList.get(position).Condition.equals(Incident.CONDITION_REJECTED)){
            holder.textViewCondition.setTextColor(Color.rgb(179, 61, 61));
            holder.textViewCondition.setText(context.getString(R.string.incident_condition_rejected));
        }
        else {
            holder.textViewCondition.setTextColor(Color.rgb(62, 62, 86));
            holder.textViewCondition.setText(context.getString(R.string.incident_condition_pending));
        }

    }

    @Override
    public int getItemCount() {
        return IncidentList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageViewType;

        TextView textViewType, textViewLocation, textViewRadius, textViewCondition, textViewReportNum, textViewDangerScore;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            imageViewType = itemView.findViewById(R.id.imageViewType);
            textViewType = itemView.findViewById(R.id.textViewIncidentType);
            textViewLocation  = itemView.findViewById(R.id.textViewLocation);
            textViewRadius = itemView.findViewById(R.id.textViewRadius);
            textViewCondition  = itemView.findViewById(R.id.textViewCondition);
            textViewReportNum = itemView.findViewById(R.id.textViewNumReports);
            textViewDangerScore = itemView.findViewById(R.id.textViewDangerScore);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

}
