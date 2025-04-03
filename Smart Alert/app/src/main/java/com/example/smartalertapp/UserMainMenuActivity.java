package com.example.smartalertapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.IncidentType;
import com.example.smartalertapp.Classes.LanguageManager;
import com.example.smartalertapp.Classes.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserMainMenuActivity extends AppCompatActivity {

    public static final String ARM_PARAM_USERNAME = "USERNAME";

    FirebaseAuth auth;
    FirebaseUser user;

    String Username;

    FirebaseDatabase database;

    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Username = getIntent().getStringExtra(ARM_PARAM_USERNAME);
        TextView welcomeTextView = findViewById(R.id.textViewWelcome);
        String welcomeMessage = getString(R.string.welcome_user, Username);
        welcomeTextView.setText(welcomeMessage);

        database = FirebaseDatabase.getInstance();


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            showMessage(getString(R.string.error), getString(R.string.error_sign_in));
            Intent intent = new Intent(UserMainMenuActivity.this, StartUpLoginActivity.class);
            startActivity(intent);
            finish();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkForCloseIncidents();


    }


    public void changeLanguageToEnglish(View view) {
        LanguageManager.setLocal(this, LanguageManager.English_langCode);
    }

    public void changeLanguageToGreek(View view) {
        LanguageManager.setLocal(this, LanguageManager.Greek_langCode);
    }


    private static final int requestCodeSpeechRecognition = 4134;

    public void recognizeSpeech(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please let us know what incident you want to report.");
        startActivityForResult(intent, requestCodeSpeechRecognition);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == requestCodeSpeechRecognition && resultCode == RESULT_OK) {
            IncidentType incidentType = null;

            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (String match : matches) {
                match = match.toUpperCase();
                if (match.contains(IncidentType.FIRE.getTypeID())) {
                    incidentType = IncidentType.FIRE;
                    break;
                } else if (match.contains(IncidentType.EARTHQUAKE.getTypeID())) {
                    incidentType = IncidentType.EARTHQUAKE;
                    break;
                } else if (match.contains(IncidentType.FLOOD.getTypeID())) {
                    incidentType = IncidentType.FLOOD;
                    break;
                } else if (match.contains(IncidentType.TSUNAMI.getTypeID())) {
                    incidentType = IncidentType.TSUNAMI;
                    break;
                } else if (match.contains(IncidentType.TORNADO.getTypeID())) {
                    incidentType = IncidentType.TORNADO;
                    break;
                } else if (match.contains(IncidentType.AVALANCHE.getTypeID())) {
                    incidentType = IncidentType.AVALANCHE;
                    break;
                }
            }

            if (incidentType != null) {
                Intent intent = new Intent(this, UserReportCategoryActivity.class);
                intent.putExtra(UserReportCategoryActivity.ARG_PARAM_USERNAME, Username);
                intent.putExtra(UserReportCategoryActivity.ARG_PARAM_CATEGORY_CHOSEN, incidentType.getTypeID());
                startActivity(intent);
            }
        }
    }

    public void SignOut(View view) {
        DatabaseReference ref = database.getReference("Users").child(user.getUid()).child("FirebaseMessagingToken");
        ref.removeValue();

        auth.signOut();
        Intent intent = new Intent(this, StartUpLoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void reportIncidentButton(View view) {
        Intent intent = new Intent(this, UserReportCategoryActivity.class);
        intent.putExtra(UserReportCategoryActivity.ARG_PARAM_USERNAME, Username);
        startActivity(intent);
    }


    public void showStats(View view) {
        Intent intent = new Intent(this, UserStatsActivity.class);
        startActivity(intent);
    }


    private void checkForCloseIncidents() {
        DatabaseReference reference = database.getReference("Incidents/Active");

        List<Incident> AllincidentsList = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //  Retrieving all incidents
                for (DataSnapshot incident_snapshot : snapshot.getChildren()) {
                    Incident temp = new Incident();
                    temp.IncidentID = incident_snapshot.getKey();
                    temp.Condition = Objects.requireNonNull(incident_snapshot.child("Condition").getValue()).toString();
                    if(!temp.Condition.equals(Incident.CONDITION_ACCEPTED)){
                        continue;
                    }

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

                    AllincidentsList.add(temp);
                }

                if (ActivityCompat.checkSelfPermission(UserMainMenuActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserMainMenuActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        DecimalFormat df = new DecimalFormat("#.#####");
                        df.setRoundingMode(RoundingMode.HALF_UP);


                        for (Incident incident : AllincidentsList){

                            boolean userIsInZone = false;

                            double diff_x = incident.Center_longitude - location.getLongitude();
                            double diff_y = incident.Center_latitude - location.getLatitude();

                            double incident_and_user_distance = Math.sqrt( Math.pow(diff_x, 2) + Math.pow(diff_y, 2) );

                            userIsInZone = incident_and_user_distance <= incident.Radius + incident.Type.get_Extra_radius();

                            if (userIsInZone){

                                String title = "";
                                if (incident.Type == IncidentType.FIRE){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_fire));
                                }
                                else if (incident.Type == IncidentType.EARTHQUAKE){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_earthquake));
                                }
                                else if (incident.Type == IncidentType.FLOOD){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_flood));
                                }
                                else if (incident.Type == IncidentType.TORNADO){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_tornado));
                                }
                                else if (incident.Type == IncidentType.TSUNAMI){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_tsunami));
                                }
                                else if (incident.Type == IncidentType.AVALANCHE){
                                    title = getString(R.string.notification_title , getString(R.string.incident_type_avalanche));
                                }

                                String Body;
                                Optional<Report> latestObject = incident.ReportList.stream().max(Comparator.comparing(obj -> LocalDateTime.parse(obj.timestamp, formatter) ) );

                                if (latestObject.isPresent()){
                                    Body = getString(R.string.recycler_view_latitude_label, incident.Center_latitude) + "\n" +
                                            getString(R.string.recycler_view_longitude_label, incident.Center_longitude) + "\n" +
                                            getString(R.string.incident_radius) + df.format(incident.Radius)  + "\n" +
                                            latestObject.get().timestamp + "\n\n";
                                }
                                else{
                                    Body = getString(R.string.recycler_view_latitude_label, incident.Center_latitude) + "\n" +
                                            getString(R.string.recycler_view_longitude_label, incident.Center_longitude) + "\n" +
                                            getString(R.string.incident_radius) + df.format(incident.Radius) + "\n";
                                }

                                showMessage(title, Body);
                            }

                        }

                    }
                }, Looper.getMainLooper());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMessage(String title, String message){
        if (!UserMainMenuActivity.this.isFinishing() && !UserMainMenuActivity.this.isDestroyed()) {
            new AlertDialog.Builder(UserMainMenuActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .show();
        }
    }


}