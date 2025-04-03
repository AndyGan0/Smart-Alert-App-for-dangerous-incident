package com.example.smartalertapp.Classes;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Incident implements Serializable {

    public static final String CONDITION_ACCEPTED = "ACCEPTED";
    public static final String CONDITION_PENDING = "PENDING";
    public static final String CONDITION_REJECTED = "REJECTED";






    public String IncidentID;
    public IncidentType Type;
    public String Condition; // ACCEPTED OR REJECTED OR PENDING



    public double Center_longitude;
    public double Center_latitude;


    public double Radius;


    public double Overall_Danger_Score;

    public List<Report> ReportList = new ArrayList<>();





    //-------------
    //  Only used by employees to sort the incidents using another metric other than the average danger degree
    //  This metric should be a better metric
    public double danger_degree_metric;

    public void updateDangerDegreeMetric(){
        danger_degree_metric = Overall_Danger_Score * ReportList.size() * Type.getBias();
    }
    //-------------




    public boolean checkIfReportIsInsideTheZone(@NonNull Report report){

        double diff_x = this.Center_longitude - report.longitude;
        double diff_y = this.Center_latitude - report.latitude;

        double incident_and_report_distance = Math.sqrt( Math.pow(diff_x, 2) + Math.pow(diff_y, 2) );

        //  report true if the report is inside the inside zone
        return incident_and_report_distance <= Radius + Type.get_Extra_radius();

    }


    public boolean hasUserReportedThisIncident(String Username){

        boolean ALLOW_USERS_TO_REPORT_INFINITE_TIMES = false;
        if (ALLOW_USERS_TO_REPORT_INFINITE_TIMES) return false;


        for (Report report : ReportList){
            if (report.Username.equals(Username)){
                return true;
            }
        }
        return false;
    }




    public void updateCenterAndRadius(){

        Center_longitude = ReportList.stream()
                .mapToDouble(x -> x.longitude)
                .average()
                .orElse(0);


        Center_latitude = ReportList.stream()
                .mapToDouble(x -> x.latitude)
                .average()
                .orElse(0);

        
        double MaxDistanceFromAnyPoint = -1;
        
        for (Report report : ReportList){
            double diff_x = this.Center_longitude - report.longitude;
            double diff_y = this.Center_latitude - report.latitude;

            double incident_and_report_distance = Math.sqrt( Math.pow(diff_x, 2) + Math.pow(diff_y, 2) );

            if ( incident_and_report_distance > MaxDistanceFromAnyPoint){
                MaxDistanceFromAnyPoint = incident_and_report_distance;
            }
        }


        Radius = MaxDistanceFromAnyPoint;

        return;

    }
    
    
    public boolean isIncidentDead(){

        int Inactive_Hours_To_Die = Type.getHoursNeededToDie() + 1;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Report report : ReportList){
            LocalDateTime timestamp = LocalDateTime.parse(report.timestamp, formatter);

            Duration duration = Duration.between(timestamp, LocalDateTime.now());
            int hoursPassed = (int) duration.toHours();

            if (hoursPassed < Inactive_Hours_To_Die){
                return false;
            }
        }

        return true;
    }


    public void firebase_Move_Incident_To_Inactive(@NonNull FirebaseDatabase database){

        DatabaseReference oldNode = database.getReference("/Incidents/Active").child(IncidentID);
        DatabaseReference newRef = database.getReference("/Incidents/Inactive").child(IncidentID);

        oldNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newRef.setValue(snapshot.getValue())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    oldNode.removeValue();
                                }

                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }





}


