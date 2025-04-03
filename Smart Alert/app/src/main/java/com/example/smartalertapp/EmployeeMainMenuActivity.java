package com.example.smartalertapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartalertapp.Classes.LanguageManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmployeeMainMenuActivity extends AppCompatActivity {

    public static final String ARG_PARAM_USERNAME = "USERNAME";



    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase database;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        String Username = getIntent().getStringExtra(ARG_PARAM_USERNAME);
        TextView welcomeTextView = findViewById(R.id.textViewWelcome);
        welcomeTextView.setText(getString(R.string.welcome_user, Username));

        database = FirebaseDatabase.getInstance();


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null){
            showMessage(getString(R.string.error), getString(R.string.error_sign_in));
            Intent intent = new Intent(EmployeeMainMenuActivity.this, StartUpLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }



    public void changeLanguageToEnglish(View view){
        LanguageManager.setLocal(this, LanguageManager.English_langCode);
    }

    public void changeLanguageToGreek(View view){
        LanguageManager.setLocal(this, LanguageManager.Greek_langCode);
    }


    public void signOut(View view){
        DatabaseReference ref = database.getReference("Users").child(user.getUid()).child("FirebaseMessagingToken");
        ref.removeValue();

        auth.signOut();
        Intent intent = new Intent(this, StartUpLoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void moveToIncidentsList(View view){
        Intent intent = new Intent(this, EmployeeShowIncidentsListActivity.class);
        startActivity(intent);
    }


    public void moveToAccountCreation(View view){
        Intent intent = new Intent(this, EmployeeCreateAccountActivity.class);
        startActivity(intent);
    }




    private void showMessage(String title, String message){
        new AlertDialog.Builder(EmployeeMainMenuActivity.this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

}