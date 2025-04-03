package com.example.smartalertapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.Incident;
import com.example.smartalertapp.Classes.IncidentType;
import com.example.smartalertapp.Classes.PushNotificationSender;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class EmployeeIncidentInfoActivity extends AppCompatActivity {

    public static final String ARG_PARAM_INCIDENT = "incident";


    FirebaseDatabase database;
    DatabaseReference ref_incident;


    Incident CurrentIncident;


    ImageView imageViewType;
    TextView textViewType, textViewCondition, textViewLatitude, textViewLongitude, textViewRadius, textViewReportCount, textViewDangerScore;

    Button buttonAccept, buttonReject, buttonPending;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_incident_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CurrentIncident = (Incident) getIntent().getSerializableExtra(ARG_PARAM_INCIDENT);

        database = FirebaseDatabase.getInstance();
        String path = "/Incidents/Active/" + CurrentIncident.IncidentID + "/Condition";
        ref_incident = database.getReference(path);

        imageViewType = findViewById(R.id.imageViewType);
        textViewType = findViewById(R.id.textViewType);
        textViewCondition = findViewById(R.id.textViewCondition);
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        textViewRadius = findViewById(R.id.textViewRadius);
        textViewReportCount = findViewById(R.id.textViewReportsCount);
        textViewDangerScore = findViewById(R.id.textViewDangerScore);
        buttonAccept = findViewById(R.id.ButtonAccept);
        buttonReject = findViewById(R.id.buttonReject);
        buttonPending = findViewById(R.id.buttonPending);


        if (CurrentIncident == null) {
            finish();
        }

        if (CurrentIncident.Type.getTypeID().equals(IncidentType.FIRE.getTypeID())  ){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.fire_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_fire));
        }
        else if (CurrentIncident.Type.getTypeID().equals(IncidentType.EARTHQUAKE.getTypeID())){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.earthquake_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_earthquake));
        }
        else if (CurrentIncident.Type.getTypeID().equals(IncidentType.FLOOD.getTypeID())){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.flood_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_flood));
        }
        else if (CurrentIncident.Type.getTypeID().equals(IncidentType.TORNADO.getTypeID())){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.tornado_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_tornado));
        }
        else if (CurrentIncident.Type.getTypeID().equals(IncidentType.TSUNAMI.getTypeID())){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.tsunami_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_tsunami));
        }
        else if (CurrentIncident.Type.getTypeID().equals(IncidentType.AVALANCHE.getTypeID())){
            imageViewType.setImageDrawable(this.getResources().getDrawable(R.drawable.avalanche_icon, this.getTheme()));
            textViewType.setText(getString(R.string.incident_type_avalanche));
        }

        refreshViewsForCondition();

        textViewLatitude.setText( String.format("%.6f", CurrentIncident.Center_latitude) );
        textViewLongitude.setText( String.format("%.6f", CurrentIncident.Center_longitude) );
        textViewRadius.setText( String.format("%.6f", CurrentIncident.Radius + CurrentIncident.Type.get_Extra_radius()) );
        textViewReportCount.setText(MessageFormat.format("{0}", CurrentIncident.ReportList.size()));
        textViewDangerScore.setText(String.format("%.2f", CurrentIncident.Overall_Danger_Score));

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCondition(Incident.CONDITION_ACCEPTED);
            }
        });

        buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCondition(Incident.CONDITION_REJECTED);
            }
        });

        buttonPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCondition(Incident.CONDITION_PENDING);
            }
        });
    }




    private void changeCondition(String Condition){
        if (!Condition.equals(Incident.CONDITION_ACCEPTED) && !Condition.equals(Incident.CONDITION_REJECTED) && !Condition.equals(Incident.CONDITION_PENDING)) {
            finish();
        }

        if (Condition.equals(Incident.CONDITION_ACCEPTED)){
            final EditText input = new EditText(this);
            input.setHint(getString(R.string.enter_instructions));

            // Create an AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.enter_instructions))
                    .setView(input)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String InputInstructions = input.getText().toString();

                            ref_incident.setValue(Condition);
                            CurrentIncident.Condition = Condition;
                            refreshViewsForCondition();

                            sendNotifications(InputInstructions);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel(); // Dismiss the dialog
                        }
                    })
                    .show();
        }
        else{
            ref_incident.setValue(Condition);
            CurrentIncident.Condition = Condition;
            refreshViewsForCondition();
        }

    }



    private void sendNotifications(String message){
        //  Send Notification

        //  Obtain all tokens of users that are not employees

        DatabaseReference ref_users = database.getReference("Users");
        ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<String> TokensList = new ArrayList<>();
                for (DataSnapshot user_snapshot: snapshot.getChildren()){
                    if (!((boolean) user_snapshot.child("isEmployee").getValue())){
                        //  If user is not employee, add the token to the list
                        Object token = user_snapshot.child("FirebaseMessagingToken").getValue();
                        if (token != null){
                            TokensList.add( token.toString());
                        }
                    }
                }


                new Thread(() -> {
                    PushNotificationSender.sendPushNotification(TokensList, message, CurrentIncident, EmployeeIncidentInfoActivity.this );
                }).start();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }







    private void refreshViewsForCondition(){

        if (CurrentIncident.Condition.equals(Incident.CONDITION_ACCEPTED)){
            textViewCondition.setTextColor(Color.rgb(103, 170, 120));
            textViewCondition.setText(getString(R.string.incident_condition_accepted));
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonReject.setVisibility(View.INVISIBLE);
            buttonPending.setVisibility(View.VISIBLE);
        }
        else if (CurrentIncident.Condition.equals(Incident.CONDITION_REJECTED)){
            textViewCondition.setTextColor(Color.rgb(179, 61, 61));
            textViewCondition.setText(getString(R.string.incident_condition_rejected));
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonReject.setVisibility(View.INVISIBLE);
            buttonPending.setVisibility(View.VISIBLE);
        }
        else {
            textViewCondition.setTextColor(Color.rgb(62, 62, 86));
            textViewCondition.setText(getString(R.string.incident_condition_pending));
            buttonAccept.setVisibility(View.VISIBLE);
            buttonReject.setVisibility(View.VISIBLE);
            buttonPending.setVisibility(View.INVISIBLE);
        }



    }



    public void showReports(View view){
        Intent intent = new Intent(this, EmployeeIncidentReportsInfoActivity.class);
        intent.putExtra(EmployeeIncidentReportsInfoActivity.ARG_PARAM_INCIDENT, (Serializable) CurrentIncident);
        startActivity(intent);
    }











    public void backButtonPressed(View view){
        finish();
    }


}