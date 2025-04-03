package com.example.smartalertapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.LanguageManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class StartUpActivity extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseUser user;


    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        database = FirebaseDatabase.getInstance();


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();





        //  Adding permissions
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }



        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), 123);
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LanguageManager.loadSelectedLanguage(StartUpActivity.this);
                    if (user != null){
                        //  User is already logged in
                        goToMainMenu();
                    }
                    else {
                        Intent intent = new Intent(StartUpActivity.this, StartUpLoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 1000);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 123){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LanguageManager.loadSelectedLanguage(StartUpActivity.this);
                    if (user != null){
                        //  User is already logged in
                        goToMainMenu();
                    }
                    else {
                        Intent intent = new Intent(StartUpActivity.this, StartUpLoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 1000);
        }


    }

    public void goToMainMenu(){
        String uid = user.getUid();
        reference = database.getReference("Users").child(uid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = (String) snapshot.child("Username").getValue();

                Boolean userIsEmployee = (Boolean) snapshot.child("isEmployee").getValue();
                if (userIsEmployee == null) {
                    userIsEmployee = false;
                }

                Boolean finalUserIsEmployee = userIsEmployee;
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {

                        if (task.isSuccessful()){
                            String token = task.getResult();
                            reference.child("FirebaseMessagingToken").setValue(token).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    new AlertDialog.Builder(StartUpActivity.this)
                                            .setTitle(getString(R.string.error))
                                            .setMessage(getString(R.string.error_push_notifications))
                                            .show();
                                }
                            });

                        }
                        else {
                            new AlertDialog.Builder(StartUpActivity.this)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_push_notifications))
                                    .show();
                        }


                        if (finalUserIsEmployee){

                            Intent intent = new Intent(StartUpActivity.this, EmployeeMainMenuActivity.class);
                            intent.putExtra(EmployeeMainMenuActivity.ARG_PARAM_USERNAME, username);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(StartUpActivity.this, UserMainMenuActivity.class);
                            intent.putExtra(UserMainMenuActivity.ARM_PARAM_USERNAME, username);
                            startActivity(intent);
                            finish();
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