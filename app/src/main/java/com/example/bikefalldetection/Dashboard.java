package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Dashboard extends AppCompatActivity {

    CardView cardContacts;
    CardView cardControlCenter;
    CardView cardMedicalCare;
    CardView cardHelp;
    CardView cardSettings;
    CardView cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        cardContacts = findViewById(R.id.contacts);
        cardControlCenter = findViewById(R.id.control_center);
        cardMedicalCare = findViewById(R.id.medical_care);
        cardHelp = findViewById(R.id.help);
        cardSettings = findViewById(R.id.settings);
        cardLogout = findViewById(R.id.logout);

        // When the user clicks the contacts icon, open the contacts page.
        cardContacts.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, Contacts.class);
            startActivity(intent);
            finish();
        });

        // When the user clicks the control center icon, open the control center page.
        cardControlCenter.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, ControlDetection.class);
            startActivity(intent);
            finish();
        });


        // When the user clicks the logout icon, logout the user.
        cardLogout.setOnClickListener(view -> {
            SharedPreferences userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences contactPreferences = getApplicationContext().getSharedPreferences("Contact_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor contactEditor = contactPreferences.edit();
            SharedPreferences.Editor userEditor = userPreferences.edit();

            // Get the User ID(UID)
            String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Sign out
            FirebaseAuth.getInstance().signOut();

            contactEditor.putString("Contacts", "");
            contactEditor.apply();


            // Set logged in status to true.
            userEditor.putBoolean("Log_In_State", true);

            // Store current time and date in shared preferences.
            userEditor.putString("Log_In_Time", "");
            userEditor.putString("Log_In_Date", "");
            userEditor.apply();

            startActivity(new Intent(Dashboard.this, MainActivity.class));
            finish();
        });



    }
}