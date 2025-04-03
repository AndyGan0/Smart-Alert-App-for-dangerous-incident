package com.example.smartalertapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.RecyclerViewAdapterIncident;
import com.example.smartalertapp.Classes.IncidentType;
import com.example.smartalertapp.Classes.RecyclerViewInterface;
import com.example.smartalertapp.Classes.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class EmployeeShowIncidentsListActivity extends AppCompatActivity implements RecyclerViewInterface {

    FirebaseDatabase database;
    DatabaseReference reference;

    Spinner spinner;


    RecyclerView incidentRecyclerViewer;
    RecyclerView.Adapter RecyclerViewerAdapter;

    ArrayList<Incident> AllActiveIncidentsList;
    ArrayList<Incident> FilteredIncidents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_show_incidents_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();

        spinner = findViewById(R.id.spinner);

        ArrayList<String> SpinnerItems = new ArrayList<>();
        SpinnerItems.add(getString(R.string.incident_condition_all));
        SpinnerItems.add(getString(R.string.incident_condition_pending));
        SpinnerItems.add(getString(R.string.incident_condition_accepted));
        SpinnerItems.add(getString(R.string.incident_condition_rejected));
        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SpinnerItems);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(SpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshResults(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        incidentRecyclerViewer = findViewById(R.id.incidentRecyclerViewer);

        AllActiveIncidentsList = new ArrayList<>();
        FilteredIncidents = new ArrayList<>();

        RecyclerViewerAdapter = new RecyclerViewAdapterIncident(this, this, FilteredIncidents);
        incidentRecyclerViewer.setAdapter(RecyclerViewerAdapter);
        incidentRecyclerViewer.setLayoutManager(new LinearLayoutManager(this));

        getIncidentsAndRefresh(null);
    }


    public void backButtonPressed(View view) {
        finish();
    }


    public void getIncidentsAndRefresh(View view) {
        reference = database.getReference("Incidents/Active");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                AllActiveIncidentsList.clear();



                //  Retrieving all incidents
                for (DataSnapshot incident_snapshot : snapshot.getChildren()) {
                    Incident temp = new Incident();
                    temp.IncidentID = incident_snapshot.getKey();
                    temp.Condition = Objects.requireNonNull(incident_snapshot.child("Condition").getValue()).toString();
                    String IncidentTypeID = Objects.requireNonNull(incident_snapshot.child("Type").getValue()).toString();
                    temp.Type = IncidentType.getIncidentTypeWithID(IncidentTypeID);
                    temp.Center_longitude = (double) incident_snapshot.child("Center_longitude").getValue();
                    temp.Center_latitude = (double) incident_snapshot.child("Center_latitude").getValue();


                    Object value = incident_snapshot.child("Radius").getValue();
                    if (value instanceof Double) {
                        temp.Radius = (Double) value;
                    } else if (value instanceof Long) {
                        temp.Radius = ((Long) value).doubleValue();
                    }


                    value = incident_snapshot.child("Overall_Danger_Score").getValue();
                    if (value instanceof Double) {
                        temp.Overall_Danger_Score = (Double) value;
                    } else if (value instanceof Long) {
                        temp.Overall_Danger_Score = ((Long) value).doubleValue();
                    }

                    for (DataSnapshot report_snapshot : incident_snapshot.child("ReportList").getChildren()) {

                        Report temp_report = new Report();
                        temp_report.ReportID = report_snapshot.getKey();
                        temp_report.Username = Objects.requireNonNull(report_snapshot.child("Username").getValue()).toString();
                        temp_report.longitude = (double) report_snapshot.child("longitude").getValue();
                        temp_report.latitude = (double) report_snapshot.child("latitude").getValue();
                        temp_report.timestamp = Objects.requireNonNull(report_snapshot.child("timestamp").getValue()).toString();
                        temp_report.comment = Objects.requireNonNull(report_snapshot.child("comment").getValue()).toString();
                        temp_report.danger_score = (int) (long) report_snapshot.child("danger_score").getValue();
                        temp_report.containsImage = (boolean) report_snapshot.child("containsImage").getValue();

                        temp.ReportList.add(temp_report);
                    }

                    temp.updateDangerDegreeMetric();
                    AllActiveIncidentsList.add(temp);
                }

                //  Checking if any incident should become inactive (dead incidents)
                Iterator<Incident> iterator = AllActiveIncidentsList.iterator();
                while (iterator.hasNext()) {
                    Incident incident = iterator.next();

                    if (incident.isIncidentDead()) {
                        //  If no user has reported anything for the elapsed hour needed to die then make incident dead
                        //  Elapsed hours needed to die depend on the incident type

                        incident.firebase_Move_Incident_To_Inactive(database);
                        iterator.remove();
                    }
                }



                AllActiveIncidentsList.sort((Incident1, Incident2) -> Double.compare(Incident2.danger_degree_metric, Incident1.danger_degree_metric));

                if (spinner.getSelectedItemPosition() == 0){
                    refreshResults(0);
                }
                else{
                    spinner.setSelection(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }





    public void refreshResults(int position){
        //  position 0 = ALL
        //  position 1 = PENDING
        //  position 2 = ACCEPTED
        //  position 3 = REJECTED

        FilteredIncidents.clear();

        if (position == 1){     // Pending
            AllActiveIncidentsList.stream()
                    .filter(incident -> incident.Condition.equals(Incident.CONDITION_PENDING))
                    .forEach(FilteredIncidents::add);
        }
        else if (position == 2){    // Accepted
            AllActiveIncidentsList.stream()
                    .filter(incident -> incident.Condition.equals(Incident.CONDITION_ACCEPTED))
                    .forEach(FilteredIncidents::add);
        }
        else if (position == 3){    // Rejected
            AllActiveIncidentsList.stream()
                    .filter(incident -> incident.Condition.equals(Incident.CONDITION_REJECTED))
                    .forEach(FilteredIncidents::add);
        }
        else {
            //  All selected in spinner
            FilteredIncidents.addAll(AllActiveIncidentsList);
        }

        RecyclerViewerAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(EmployeeShowIncidentsListActivity.this, EmployeeIncidentInfoActivity.class);
        intent.putExtra(EmployeeIncidentInfoActivity.ARG_PARAM_INCIDENT, FilteredIncidents.get(position));
        startActivity(intent);
    }




    private boolean firstTimeResumed = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (firstTimeResumed){
            firstTimeResumed = false;
        }
        else{
            getIncidentsAndRefresh(null);
        }
    }
}