package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        if(findViewById(R.id.settings_frame_layout) != null) {
            if(savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                    .commit();
        }
    }

    /*
    Redirect the user back to the Dashboard page when the back touch button is pressed.
     */
    @Override public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Settings.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}