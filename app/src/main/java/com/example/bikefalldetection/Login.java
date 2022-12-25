package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.LocalTime;

public class Login extends AppCompatActivity {

    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();

        // When user clicks the Sign Up text, redirect him to the login page.
        textViewSignUp.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, SignUp.class));
            finish();
        });

        // When user clicks the login button, start the login process.
        buttonLogin.setOnClickListener(view -> login());
    }


    private void login() {

        // Get values from input fields.
        String email = String.valueOf(textInputEditTextEmail.getText()).replaceAll(" ", "");
        String password = String.valueOf(textInputEditTextPassword.getText()).replaceAll(" ", "");

        // Check if password and email fields are filled.
        if(!email.equals("") && !password.equals("")) {
            // Start progressBar.
            progressBar.setVisibility(View.VISIBLE);

            // Sign in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, task -> {
                if(task.isSuccessful()) {   // If login is successful, change the login status.
                    // Get current time and date.
                    String currentTime = null;
                    String currentDate = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        currentTime = LocalTime.now().toString();
                        currentDate = LocalDate.now().toString();
                    }

                    SharedPreferences userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPreferences.edit();

                    // Set logged in status to true.
                    editor.putBoolean("Log_In_State", true);

                    // Store current time and date in shared preferences.
                    editor.putString("Log_In_Time", currentTime);
                    editor.putString("Log_In_Date", currentDate);
                    editor.apply();

                    startActivity(new Intent(Login.this, Dashboard.class));
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Login.this, "All input fields are required.", Toast.LENGTH_SHORT).show();
        }
    }

}