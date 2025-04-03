package com.example.smartalertapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.IncidentType;

public class UserReportCategoryActivity extends AppCompatActivity {


    public static final String ARG_PARAM_USERNAME = "USERNAME";
    public static final String ARG_PARAM_CATEGORY_CHOSEN = "CATEGORY";


    String Username;

    ActivityResultLauncher<Intent> resultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_report_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Username = getIntent().getStringExtra(ARG_PARAM_USERNAME);

        CardView fireCardView, earthquakeCardView, floodCardView, tornadoCardView, tsunamiCardView, avelancheCardView;
        fireCardView = findViewById(R.id.cardView_fire);
        earthquakeCardView = findViewById(R.id.cardView_earthquake);
        floodCardView = findViewById(R.id.cardView_flood);
        tornadoCardView = findViewById(R.id.cardView_tornado);
        tsunamiCardView = findViewById(R.id.cardView_tsunami);
        avelancheCardView = findViewById(R.id.cardView_avelanche);

        fireCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.FIRE);
            }
        });

        earthquakeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.EARTHQUAKE);
            }
        });

        floodCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.FLOOD);
            }
        });

        tornadoCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.TORNADO);
            }
        });

        tsunamiCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.TSUNAMI);
            }
        });

        avelancheCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCategory(IncidentType.AVALANCHE);
            }
        });


        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == RESULT_OK){
                            finish();
                        }
                    }
                });

        if (getIntent().hasExtra(ARG_PARAM_CATEGORY_CHOSEN)){
            String incidentTypeID = getIntent().getStringExtra(ARG_PARAM_CATEGORY_CHOSEN);

            Intent intent = new Intent(this, UserReportFormActivity.class);
            intent.putExtra(UserReportFormActivity.ARG_PARAM_USERNAME, Username );
            intent.putExtra(UserReportFormActivity.ARG_PARAM_INCIDENT_TYPE, incidentTypeID );
            resultLauncher.launch(intent);
        }


    }


    public void backButtonPressed(View view){
        finish();
    }


    private void chooseCategory(IncidentType incidentType){
        Intent intent = new Intent(this, UserReportFormActivity.class);
        intent.putExtra(UserReportFormActivity.ARG_PARAM_USERNAME, Username );
        intent.putExtra(UserReportFormActivity.ARG_PARAM_INCIDENT_TYPE, incidentType.getTypeID() );
        resultLauncher.launch(intent);
    }



}