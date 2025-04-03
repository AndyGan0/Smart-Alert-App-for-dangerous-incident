package com.example.smartalertapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.IncidentType;
import com.example.smartalertapp.Classes.Report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserStatsActivity extends AppCompatActivity {

    SQLiteDatabase database;

    ArrayList<Incident> AllincidentList;
    ArrayList<Incident> FilteredIncidentList;

    Spinner spinner;


    TextView textViewFire, textViewEarthquake, textViewFlood, textViewTornado, textViewTsunami,textViewAvalanche;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinner = findViewById(R.id.spinner);

        ArrayList<String> SpinnerItems = new ArrayList<>();
        SpinnerItems.add(getString(R.string.AllOption));
        SpinnerItems.add(getString(R.string.thisMonthOption));
        SpinnerItems.add(getString(R.string.thisYearOption));
        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SpinnerItems);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(SpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterItems(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        database = openOrCreateDatabase("smartalert.db", MODE_PRIVATE, null);
        database.execSQL("Create table if not exists NotifiedIncidents(" +
                "IncidentID Text Primary Key," +
                "TypeID Text," +
                "timestamp Text)");


        String query = "Select * from NotifiedIncidents";

        Cursor cursor = database.rawQuery(query, null);

        AllincidentList = new ArrayList<>();
        FilteredIncidentList = new ArrayList<>();

        while (cursor.moveToNext()){
            Incident temp = new Incident();

            temp.IncidentID = cursor.getString(0);

            String TypeID = cursor.getString(1);
            temp.Type = IncidentType.getIncidentTypeWithID(TypeID);

            Report tempReport = new Report();
            tempReport.timestamp = cursor.getString(2);
            temp.ReportList.add(tempReport);

            AllincidentList.add(temp);
        }


        FilteredIncidentList.addAll(AllincidentList);


        textViewFire = findViewById(R.id.textViewFireCount);
        textViewEarthquake = findViewById(R.id.textViewEarthquakeCount);
        textViewFlood = findViewById(R.id.textViewFloodCount);
        textViewTornado = findViewById(R.id.textViewTornadoCount);
        textViewTsunami = findViewById(R.id.textViewTsunamiCount);
        textViewAvalanche = findViewById(R.id.textViewAvalancheCount);

        refreshStats();



    }




    private void filterItems(int option){
        //  Option == 0 : All
        //  Option == 1 : This month
        //  Option == 2 : This Year


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (option == 0){
            FilteredIncidentList.clear();
            FilteredIncidentList.addAll(AllincidentList);
        }
        else if (option == 1) {

            LocalDateTime datetime_month = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);

            FilteredIncidentList.clear();
            FilteredIncidentList = (ArrayList<Incident>) AllincidentList.stream().filter(incident -> {
                                                LocalDateTime incidentTime = LocalDateTime.parse(incident.ReportList.get(0).timestamp, formatter);
                                                return incidentTime.isAfter(datetime_month);
                                            })
                                            .collect(Collectors.toList());

        }
        else{
            LocalDateTime datetime_year = LocalDateTime.now().minus(1, ChronoUnit.YEARS);

            FilteredIncidentList.clear();
            FilteredIncidentList = (ArrayList<Incident>) AllincidentList.stream().filter(incident -> {
                        LocalDateTime incidentTime = LocalDateTime.parse(incident.ReportList.get(0).timestamp, formatter);
                        return incidentTime.isAfter(datetime_year);
                    })
                    .collect(Collectors.toList());

        }


        refreshStats();
    }




    private void refreshStats(){

        int countFire = 0;
        int countEarthquake = 0;
        int countFlood = 0;
        int countTornado = 0;
        int countTsunami = 0;
        int countAvalanche = 0;

        for (Incident incident: FilteredIncidentList){
            if (incident.Type == IncidentType.FIRE){
                countFire++;
            }
            else if (incident.Type == IncidentType.EARTHQUAKE){
                countEarthquake++;
            }
            else if (incident.Type == IncidentType.FLOOD){
                countFlood++;
            }
            else if (incident.Type == IncidentType.TORNADO){
                countTornado++;
            }
            else if (incident.Type == IncidentType.TSUNAMI){
                countTsunami++;
            }
            else if (incident.Type == IncidentType.AVALANCHE){
                countAvalanche++;
            }
        }

        textViewFire.setText(String.valueOf(countFire));
        textViewEarthquake.setText(String.valueOf(countEarthquake));
        textViewFlood.setText(String.valueOf(countFlood));
        textViewTornado.setText(String.valueOf(countTornado));
        textViewTsunami.setText(String.valueOf(countTsunami));
        textViewAvalanche.setText(String.valueOf(countAvalanche));

    }






    public void backButtonPressed(View view){
        finish();
    }


}