package com.example.smartalertapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartalertapp.Classes.LanguageManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class StartUpLoginActivity extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference reference;


    EditText EditTextEmail, EditTextPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_up_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView TextViewGoToRegister = findViewById(R.id.TextViewGoRegister);
        TextViewGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartUpLoginActivity.this, StartUpRegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });






        database = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null){
            //  User is already logged in
            goToMainMenu(null);
        }


        EditTextEmail = findViewById(R.id.editTextLoginEmail);
        EditTextPassword = findViewById(R.id.editTextLoginPassword);




    }



    public void changeLanguageToEnglish(View view){
        LanguageManager.setLocal(this, LanguageManager.English_langCode);
    }

    public void changeLanguageToGreek(View view){
        LanguageManager.setLocal(this, LanguageManager.Greek_langCode);
    }




    public void signIn(View view){

        String email = EditTextEmail.getText().toString();
        String password = EditTextPassword.getText().toString();

        if (email.isEmpty()){
            showMessage(getString(R.string.error), getString(R.string.error_email_empty));
            return;
        }
        if (password.isEmpty()){
            showMessage(getString(R.string.error), getString(R.string.error_password_empty));
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            goToMainMenu(password);
                        }
                        else {
                            showMessage(getString(R.string.error), task.getException().getLocalizedMessage() );
                        }
                    }
                });
    }




    private void goToMainMenu(String password){

        //  If user is not logged in, show error
        user = auth.getCurrentUser();
        if (user == null){
            //  User is not logged in
            showMessage(getString(R.string.error), getString(R.string.error_sign_in));
        }

        //  Get User Type (Employee or User)
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
                                    new AlertDialog.Builder(StartUpLoginActivity.this)
                                            .setTitle(getString(R.string.error))
                                            .setMessage(getString(R.string.error_push_notifications))
                                            .show();
                                }
                            });
                        }
                        else {
                            new AlertDialog.Builder(StartUpLoginActivity.this)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_push_notifications))
                                    .show();
                        }



                        if (finalUserIsEmployee){
                            if( password != null){
                                SharedPreferences preferences;
                                preferences = PreferenceManager.getDefaultSharedPreferences(StartUpLoginActivity.this);

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("password", password);
                                editor.apply();
                            }

                            Intent intent = new Intent(StartUpLoginActivity.this, EmployeeMainMenuActivity.class);
                            intent.putExtra(EmployeeMainMenuActivity.ARG_PARAM_USERNAME, username);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(StartUpLoginActivity.this, UserMainMenuActivity.class);
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





    private void showMessage(String title, String message){
        new AlertDialog.Builder(StartUpLoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }



}