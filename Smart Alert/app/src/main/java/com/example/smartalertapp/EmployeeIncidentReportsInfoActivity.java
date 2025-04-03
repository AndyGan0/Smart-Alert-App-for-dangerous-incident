package com.example.smartalertapp;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.RecyclerViewInterface;
import com.example.smartalertapp.Classes.Report;
import com.example.smartalertapp.Classes.RecyclerViewAdapterReport;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EmployeeIncidentReportsInfoActivity extends AppCompatActivity implements RecyclerViewInterface {

    public static final String ARG_PARAM_INCIDENT = "incident";




    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_incident_reports_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Incident currentIncident = (Incident) getIntent().getSerializableExtra(ARG_PARAM_INCIDENT);
        ArrayList<Report> ReportList = (ArrayList<Report>) currentIncident.ReportList;


        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Incidents/" + currentIncident.IncidentID + "/");


        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapterReport(this, this, ReportList, storageReference);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }




    public void goBack(View view){
        finish();
    }


    @Override
    public void onItemClick(int position) {

    }
}