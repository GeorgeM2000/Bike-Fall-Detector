package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SignUp extends AppCompatActivity {


    TextInputEditText textInputEditTextFullname, textInputEditTextUsername, textInputEditTextPassword, textInputEditTextEmail;
    Button buttonSignUp;
    TextView textViewLogin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextFullname = findViewById(R.id.fullname);
        textInputEditTextPassword = findViewById(R.id.password);
        textInputEditTextUsername = findViewById(R.id.username);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();

        // If user clicks on the login text, redirect him to the login page.
        textViewLogin.setOnClickListener(view -> {
            startActivity(new Intent(SignUp.this, Login.class));
            finish();
        });


        // If the user clicks the sign up button.
        buttonSignUp.setOnClickListener(view -> {
            // Get user information from text fields.
            String full_name, username, password, email;
            full_name = String.valueOf(textInputEditTextFullname.getText());
            username = String.valueOf(textInputEditTextUsername.getText()).replaceAll(" ", "");
            email = String.valueOf(textInputEditTextEmail.getText()).replaceAll(" ", "");
            password = String.valueOf(textInputEditTextPassword.getText());

            // Check if all of the fields are filled.
            if(!full_name.equals("") && !email.equals("") && !password.equals("") && !username.equals("")) {
                // Start progressBar.
                progressBar.setVisibility(View.VISIBLE);

                // Create user.
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {

                            // Add user information to the database.
                            if(task.isSuccessful()) {   // If the process is successful.
                                HashMap<String, Object> userData = new HashMap<String, Object>(){{
                                    put("email", email);
                                    put("full_name", full_name);
                                    put("username", username);
                                }};

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(userData)
                                        .addOnCompleteListener(task1 -> {
                                            if(task1.isSuccessful()) {
                                                // Redirect user to login page.
                                                Toast.makeText(SignUp.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignUp.this, Login.class));
                                                finish();
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(SignUp.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(SignUp.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SignUp.this, "All input fields are required.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}