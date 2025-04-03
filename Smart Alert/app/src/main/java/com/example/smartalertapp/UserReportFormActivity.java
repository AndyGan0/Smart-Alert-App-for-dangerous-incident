package com.example.smartalertapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.IncidentType;
import com.example.smartalertapp.Classes.Report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserReportFormActivity extends AppCompatActivity implements LocationListener {


    public static final String ARG_PARAM_USERNAME = "USERNAME";
    public static final String ARG_PARAM_INCIDENT_TYPE = "INCIDENT_TYPE";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 80321;




    private IncidentType incidentType;
    private String Username;



    SeekBar seekBar;
    EditText editTextComment;


    LocationManager locationManager;


    FirebaseDatabase database;
    DatabaseReference reference;
    StorageReference storageReference;





    ImageView imageViewPhoto;
    Button imageDeleteButton;
    Uri ChosenImage;
    ActivityResultLauncher<Intent> resultLauncherImage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_report_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Username = getIntent().getStringExtra(ARG_PARAM_USERNAME);
        String IncidentTypeID = getIntent().getStringExtra(ARG_PARAM_INCIDENT_TYPE);

        seekBar = findViewById(R.id.seekBar);
        editTextComment = findViewById(R.id.EditTextComment);

        TextView textViewIncidentType = findViewById(R.id.textViewIncidentType);


        incidentType = IncidentType.getIncidentTypeWithID(IncidentTypeID);
        if ( incidentType  == IncidentType.FIRE ){
            textViewIncidentType.setText(getString(R.string.incident_type_fire));
        }
        else if ( incidentType  == IncidentType.EARTHQUAKE ){
            textViewIncidentType.setText(getString(R.string.incident_type_earthquake));
        }
        else if ( incidentType  == IncidentType.FLOOD ){
            textViewIncidentType.setText(getString(R.string.incident_type_flood));
        }
        else if ( incidentType  == IncidentType.TORNADO ){
            textViewIncidentType.setText(getString(R.string.incident_type_tornado));
        }
        else if ( incidentType  == IncidentType.TSUNAMI ){
            textViewIncidentType.setText(getString(R.string.incident_type_tsunami));
        }
        else if ( incidentType  == IncidentType.AVALANCHE ){
            textViewIncidentType.setText(getString(R.string.incident_type_avalanche));
        }
        else{
            finish();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("/Incidents/");


        ChosenImage = null;
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        imageDeleteButton = findViewById(R.id.buttonDelete);
        resultLauncherImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == RESULT_OK){
                            ChosenImage = o.getData().getData();
                            if (ChosenImage != null) {
                                imageViewPhoto.setImageURI(ChosenImage);
                                imageDeleteButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

    }



    public void backButtonPressed(View view){
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }


    public void choosePhoto(View view){
        Intent intentImagePicker = new Intent(Intent.ACTION_PICK);
        intentImagePicker.setType("image/*");
        resultLauncherImage.launch(intentImagePicker);
    }

    public void removePhoto(View view){
        ChosenImage = null;
        imageViewPhoto.setImageResource(R.drawable.baseline_file_upload_24);
        imageDeleteButton.setVisibility(View.INVISIBLE);
    }




    public void submitReport(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION){
            if ( grantResults[0] ==  PackageManager.PERMISSION_GRANTED ) {
                submitReport(null);
            }
            else{
                showMessage(getString(R.string.error), getString(R.string.error_location_permission));
            }
        }
    }





    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationManager.removeUpdates(this);

        Report report = new Report();
        report.Username = Username;
        report.longitude = location.getLongitude();
        report.latitude = location.getLatitude();
        report.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( Calendar.getInstance().getTime() );
        report.comment = editTextComment.getText().toString();
        report.danger_score = seekBar.getProgress() + 1;

        completeReport(report);
    }




    public void completeReport(Report NewReport){

        reference = database.getReference("Incidents/Active");

        List<Incident> AllincidentsList = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                //  Retrieving all incidents
                for (DataSnapshot incident_snapshot : snapshot.getChildren()) {
                    Incident temp = new Incident();
                    temp.IncidentID = incident_snapshot.getKey();
                    temp.Condition = Objects.requireNonNull(incident_snapshot.child("Condition").getValue()).toString();
                    String IncidentTypeID = Objects.requireNonNull(incident_snapshot.child("Type").getValue()).toString();
                    temp.Type = IncidentType.getIncidentTypeWithID(IncidentTypeID);
                    temp.Center_longitude = (double) incident_snapshot.child("Center_longitude").getValue();
                    temp.Center_latitude = (double) incident_snapshot.child("Center_latitude").getValue();

                    Object value =  incident_snapshot.child("Radius").getValue();
                    if (value instanceof Double) {
                        temp.Radius = (Double) value;
                    }
                    else if (value instanceof Long) {
                        temp.Radius = ((Long) value).doubleValue();
                    }

                    value = incident_snapshot.child("Overall_Danger_Score").getValue();
                    if (value instanceof Double) {
                        temp.Overall_Danger_Score = (Double) value;
                    }
                    else if (value instanceof Long) {
                        temp.Overall_Danger_Score = ((Long) value).doubleValue();
                    }

                    for (DataSnapshot report_snapshot : incident_snapshot.child("ReportList").getChildren()) {

                        Report temp_report = new Report();
                        temp_report.ReportID = report_snapshot.getKey();
                        temp_report.Username = Objects.requireNonNull(report_snapshot.child("Username").getValue()).toString();
                        temp_report.longitude = (double)  report_snapshot.child("longitude").getValue();
                        temp_report.latitude = (double)  report_snapshot.child("latitude").getValue();
                        temp_report.timestamp = Objects.requireNonNull(report_snapshot.child("timestamp").getValue()).toString();
                        temp_report.comment = Objects.requireNonNull(report_snapshot.child("comment").getValue()).toString();
                        temp_report.danger_score = (int) (long) report_snapshot.child("danger_score").getValue();
                        temp_report.containsImage = (boolean) report_snapshot.child("containsImage").getValue();

                        temp.ReportList.add(temp_report);
                    }

                    AllincidentsList.add(temp);
                }

                List<Incident> MatchingIncidentsList = new ArrayList<>(AllincidentsList);


                //  removing dead incidents
                Iterator<Incident> iterator = MatchingIncidentsList.iterator();
                while (iterator.hasNext()) {
                    Incident incident = iterator.next();

                    if (incident.isIncidentDead()) {
                        //  If no user has reported anything for the elapsed hour needed to die then make incident dead
                        //  Elapsed hours needed to die depend on the incident type

                        incident.firebase_Move_Incident_To_Inactive(database);
                        iterator.remove();
                    }
                }



                //  remove incidents that have different type
                MatchingIncidentsList.removeIf( x -> (x.Type != incidentType ) );

                //  remove incidents that are too far away from the report
                MatchingIncidentsList.removeIf( x -> !(x.checkIfReportIsInsideTheZone(NewReport)) );


                DatabaseReference reference_all_active_incidents = database.getReference("Incidents/Active");

                if (MatchingIncidentsList.isEmpty()){
                    //  No such incident has been reported yet. Create new incident



                    //  Creating the report Map
                    Map<String, Object> childrenMapReport = new HashMap<>();
                    childrenMapReport.put("Username", NewReport.Username);
                    childrenMapReport.put("longitude", NewReport.longitude);
                    childrenMapReport.put("latitude", NewReport.latitude);
                    childrenMapReport.put("timestamp", NewReport.timestamp);
                    childrenMapReport.put("comment", NewReport.comment);
                    childrenMapReport.put("danger_score", NewReport.danger_score);
                    childrenMapReport.put("containsImage", ChosenImage != null);


                    //  Creating the incident map
                    Map<String, Object> childrenMapIncident = new HashMap<>();
                    childrenMapIncident.put("Type", incidentType.getTypeID());
                    childrenMapIncident.put("Condition", Incident.CONDITION_PENDING);
                    childrenMapIncident.put("Center_longitude", NewReport.longitude);
                    childrenMapIncident.put("Center_latitude", NewReport.latitude);
                    childrenMapIncident.put("Radius", 0);
                    childrenMapIncident.put("Overall_Danger_Score", NewReport.danger_score);
                    childrenMapIncident.put("ReportList", new HashMap<>());


                    DatabaseReference reference_new_incident = reference_all_active_incidents.push();
                    reference_new_incident.updateChildren(childrenMapIncident);

                    DatabaseReference reference_new_report = reference_new_incident.child("ReportList").push();
                    reference_new_report.updateChildren(childrenMapReport)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){
                                        if (ChosenImage == null){
                                            endFormSuccessffuly();
                                        }
                                        else{
                                            saveImageForReport(reference_new_incident.getKey(), reference_new_report.getKey());
                                        }
                                    }



                                }
                            });





                    return;

                }

                if (MatchingIncidentsList.size() > 1){
                    //  New report was found in the zone of multiple incidents
                    //  There are multiple incidents that have to be combined into one
                    //  Merge all the incidents into one

                    boolean Condition_ACCEPTED_found = false;
                    boolean Condition_REJECTED_found = false;

                    Map<String, Object> MargedReportsListMap = new HashMap<>();

                    //  Merging all reports to make a map
                    for (int i = 0; i<MatchingIncidentsList.size(); i++){
                        //  for every incident
                        if (MatchingIncidentsList.get(i).Condition.equals( Incident.CONDITION_ACCEPTED )) Condition_ACCEPTED_found = true;
                        else if (MatchingIncidentsList.get(i).Condition.equals( Incident.CONDITION_REJECTED )) Condition_REJECTED_found = true;


                        for (Report r : MatchingIncidentsList.get(i).ReportList ){
                            //  for every report create a map and add into merged reports
                            Map<String, Object> tempReport = new HashMap<>();

                            tempReport.put("Username", r.Username);
                            tempReport.put("longitude", r.longitude);
                            tempReport.put("latitude", r.latitude);
                            tempReport.put("timestamp", r.timestamp);
                            tempReport.put("comment", r.comment);
                            tempReport.put("danger_score", r.danger_score);
                            tempReport.put("containsImage", r.containsImage);

                            MargedReportsListMap.put(r.ReportID, tempReport );
                        }
                    }


                    //  Setting merged reports into the primary incident
                    DatabaseReference reference_primary_incident = reference_all_active_incidents.child( MatchingIncidentsList.get(0).IncidentID );
                    reference_primary_incident.child("ReportList").updateChildren(MargedReportsListMap);


                    //  Delete secondary incidents
                    DatabaseReference reference_secondary_incident;
                    for (int i = 1; i<MatchingIncidentsList.size(); i++){
                        reference_secondary_incident = reference_all_active_incidents.child( MatchingIncidentsList.get(i).IncidentID );
                        reference_secondary_incident.removeValue();
                    }


                    //  We have to move images in cloud storage as well
                    StorageReference storage_ref_primary_incident = storageReference.child(MatchingIncidentsList.get(0).IncidentID);
                    for (int i = 1; i<MatchingIncidentsList.size(); i++){

                        StorageReference storage_ref_secondary_incident = storageReference.child(MatchingIncidentsList.get(i).IncidentID);

                        storage_ref_secondary_incident.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
                            @Override
                            public void onComplete(@NonNull Task<ListResult> task) {

                                if (task.isSuccessful()){
                                    for (StorageReference file: task.getResult().getItems()){

                                        StorageReference storage_ref_destination = storage_ref_primary_incident.child( file.getName() );

                                        file.getBytes(Long.MAX_VALUE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<byte[]> task) {

                                                        if (task.isSuccessful()){

                                                            InputStream inputStream = new ByteArrayInputStream(task.getResult());
                                                            storage_ref_destination.putStream(inputStream).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                    if (task.isSuccessful()){
                                                                        file.delete();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });



                                    }
                                }
                            }
                        });


                    }



                    //  moving reports from secondary to primary in MatchingIncidentsList and deleting secondary incidents
                    for (int i = 1; i<MatchingIncidentsList.size(); i++){
                        for (Report r : MatchingIncidentsList.get(1).ReportList ){
                            MatchingIncidentsList.get(0).ReportList.add(r);
                        }
                        MatchingIncidentsList.remove(1);
                    }

                    //  Decide condition
                    if (Condition_ACCEPTED_found) reference_primary_incident.child("Condition").setValue( Incident.CONDITION_ACCEPTED);
                    else reference_primary_incident.child("Condition").setValue( Incident.CONDITION_PENDING );







                    //  now we only have 1 merged incident in the database
                    //  treat the case as 1 incident.

                }


                //  The incident has been reported again. Add the new report to it


                //  If user has reported this incident again, then show error
                if (MatchingIncidentsList.get(0).hasUserReportedThisIncident(Username)){
                    new AlertDialog.Builder(UserReportFormActivity.this)
                            .setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.error_already_reported))
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent intent = new Intent( );
                                    setResult( Activity.RESULT_CANCELED, intent );
                                    finish();
                                }
                            })
                            .show();
                    return;
                }


                Map<String, Object> ReportChildMap = new HashMap<>();
                ReportChildMap.put("Username", NewReport.Username);
                ReportChildMap.put("longitude", NewReport.longitude);
                ReportChildMap.put("latitude", NewReport.latitude);
                ReportChildMap.put("timestamp", NewReport.timestamp);
                ReportChildMap.put("comment", NewReport.comment);
                ReportChildMap.put("danger_score", NewReport.danger_score);
                ReportChildMap.put("containsImage", ChosenImage != null);



                DatabaseReference reference_old_incident = reference_all_active_incidents.child( MatchingIncidentsList.get(0).IncidentID );
                DatabaseReference reference_new_report = reference_old_incident.child("ReportList").push();
                reference_new_report.updateChildren(ReportChildMap);

                MatchingIncidentsList.get(0).ReportList.add(NewReport);

                //  Calculating Overall_Danger_Score, center_longitude, center_latitude


                double Overall_Danger_Score = MatchingIncidentsList.get(0).ReportList.stream()
                        .mapToInt(x -> x.danger_score)
                        .average()
                        .orElse(0);


                //  Calculating and updating the center and the radius
                MatchingIncidentsList.get(0).updateCenterAndRadius();



                reference_old_incident.child("Overall_Danger_Score").setValue(Overall_Danger_Score);
                reference_old_incident.child("Center_longitude").setValue(MatchingIncidentsList.get(0).Center_longitude);
                reference_old_incident.child("Center_latitude").setValue(MatchingIncidentsList.get(0).Center_latitude);
                reference_old_incident.child("Radius").setValue(MatchingIncidentsList.get(0).Radius)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    if (ChosenImage == null){
                                        endFormSuccessffuly();
                                    }
                                    else{
                                        saveImageForReport(reference_old_incident.getKey(), reference_new_report.getKey());
                                    }
                                }
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void saveImageForReport(String IncidentID, String ReportID){

        if (ChosenImage != null){
            storageReference = storageReference.child(IncidentID).child(ReportID);
            storageReference.putFile(ChosenImage)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            endFormSuccessffuly();
                        }
                    });


        }

    }



    private void endFormSuccessffuly(){

        new AlertDialog.Builder(UserReportFormActivity.this)
                .setTitle(getString(R.string.success))
                .setMessage(getString(R.string.report_submitted))
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Intent intent = new Intent( );
                        setResult( Activity.RESULT_OK, intent );
                        Toast.makeText(UserReportFormActivity.this, getString(R.string.report_submitted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .show();
    }







    void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }


}