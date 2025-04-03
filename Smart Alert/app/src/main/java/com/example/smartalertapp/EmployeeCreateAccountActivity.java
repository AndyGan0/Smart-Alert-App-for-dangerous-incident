package com.example.smartalertapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EmployeeCreateAccountActivity extends AppCompatActivity {

    public static final String ARG_PARAM_PASSWORD= "password";




    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference reference;
    EditText EditTextFirstName, EditTextLastName, EditTextUsername, EditTextEmail, EditTextPassword, EditTextConfirmPassword;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();

        EditTextFirstName = findViewById(R.id.editTextRegisterFirstName);
        EditTextLastName = findViewById(R.id.editTextRegisterLastName);
        EditTextUsername = findViewById(R.id.editTextRegisterUsername);
        EditTextEmail = findViewById(R.id.editTextRegisterEmail);
        EditTextPassword = findViewById(R.id.editTextRegisterPassword);
        EditTextConfirmPassword = findViewById(R.id.editTextRegisterPassword2);

    }

    public void backButtonPressed(View view){
        finish();
    }


    public void registerButtonClick(View view){
        //  Checking the values

        String FirstName = EditTextFirstName.getText().toString();
        String LastName = EditTextLastName.getText().toString();
        String Username = EditTextUsername.getText().toString();
        String Email = EditTextEmail.getText().toString();
        String Password = EditTextPassword.getText().toString();
        String ConfirmPassword = EditTextConfirmPassword.getText().toString();

        if (FirstName.isEmpty() ){
            showMessage(getString(R.string.error), getString(R.string.error_first_name_empty) );
            return;
        }
        else if (LastName.isEmpty()) {
            showMessage(getString(R.string.error), getString(R.string.error_last_name_empty) );
            return;
        }
        else if (Username.isEmpty()) {
            showMessage(getString(R.string.error), getString(R.string.error_username_empty) );
            return;
        }
        else if (Email.isEmpty()) {
            showMessage(getString(R.string.error), getString(R.string.error_email_empty) );
            return;
        }
        else if ( Password.isEmpty() ) {
            showMessage(getString(R.string.error), getString(R.string.error_password_empty) );
            return;
        }

        if (Password.length() <4){
            showMessage(getString(R.string.error), getString(R.string.error_small_password) );
            return;
        }

        if (! Password.equals(ConfirmPassword)){
            showMessage(getString(R.string.error), getString(R.string.error_password_confirm) );
            return;
        }

        //  Checking if username already exists in database
        reference = database.getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot_user : snapshot.getChildren()) {
                    if (snapshot_user.child("Username").getValue().toString().equals(Username)){
                        //  Username already exists
                        showMessage(getString(R.string.error), getString(R.string.error_username_taken));
                        return;
                    }
                }

                completeRegistration(Email, Password, FirstName, LastName, Username);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void completeRegistration(String Email, String Password, String FirstName, String LastName, String Username){

        String originalUserEmail = user.getEmail();

        // Create a new user
        auth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //  New user created successfully
                            //  Save user info in database
                            user = auth.getCurrentUser();
                            if (user != null){

                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(Username)
                                        .build();
                                user.updateProfile(request);

                                String uid = auth.getCurrentUser().getUid();

                                Map<String,Object> ChildrenMap = new HashMap<>();
                                ChildrenMap.put("Email", Email);
                                ChildrenMap.put("FirstName", FirstName);
                                ChildrenMap.put("LastName", LastName);
                                ChildrenMap.put("Username", Username);
                                ChildrenMap.put("isEmployee", true);
                                reference.child(uid).updateChildren(ChildrenMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EmployeeCreateAccountActivity.this);
                                                    String password = preferences.getString("password",null);

                                                    if (password == null){
                                                        auth.signOut();
                                                        finish();
                                                    }


                                                    auth.signInWithEmailAndPassword(originalUserEmail, password)
                                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Re-authentication successful
                                                                        user = auth.getCurrentUser();
                                                                        String currentUserEmail = user.getEmail();
                                                                        Toast.makeText(getApplicationContext(), getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                                                                        finish();

                                                                    }
                                                                    else {
                                                                        showMessage(getString(R.string.error), task.getException().getLocalizedMessage() );
                                                                    }
                                                                }
                                                            });

                                                }
                                                else{
                                                    showMessage(getString(R.string.error), task.getException().getLocalizedMessage() );
                                                }

                                            }
                                        });

                            }


                        }
                        else {
                            showMessage(getString(R.string.error), task.getException().getLocalizedMessage() );
                        }
                    }
                });


    }




    private void showMessage(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }


}