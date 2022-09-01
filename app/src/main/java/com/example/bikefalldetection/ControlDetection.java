package com.example.bikefalldetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.List;
import java.util.Objects;

public class ControlDetection extends AppCompatActivity implements SensorEventListener {

    ImageButton start_service, stop_service, abort_timer, send_help;
    boolean timer_state = false;
    String[] permissions = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION", "android.permission.ACCESS_FOREGROUND_LOCATION", "android.permission.SEND_SMS", "android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"};
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    Sensor accelerometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_detection);

        start_service = findViewById(R.id.start_service);
        stop_service = findViewById(R.id.stop_service);
        abort_timer = findViewById(R.id.abort_timer);
        send_help = findViewById(R.id.send_help);

        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        start_service.setOnClickListener(view -> {

            // Check for permissions
            //ActivityCompat.requestPermissions(ControlDetection.this, permissions, 4);

            // Start the service
            //startBLEService();

            // Start listening to accelerometer values
            sensorManager.registerListener(ControlDetection.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        });

        stop_service.setOnClickListener(view -> {

            // Check for permissions
            //ActivityCompat.requestPermissions(ControlDetection.this, permissions, 4);

            // Stop service
            //stopBLEService();

            // Stop listening to accelerometer values
            sensorManager.unregisterListener(ControlDetection.this);

        });

        abort_timer.setOnClickListener(view -> {

        });

        send_help.setOnClickListener(view -> {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task<Location> locationTask = fusedLocationClient.getLastLocation();

            SmsManager smsManager = SmsManager.getDefault();
            locationTask.addOnSuccessListener(location -> {
                double latitude, longitude;

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                String message = "Βοήθεια! Συνέβει ατύχημα σε αυτή την τοποθεσία : " + latitude + " " + longitude;
                String googleMapsAddress = "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(latitude) + "%2C" + String.valueOf(longitude);

                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("contacts")
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                List<BikeFallDetectorService.Contact> contacts;
                                GenericTypeIndicator<List<BikeFallDetectorService.Contact>> t = new GenericTypeIndicator<List<BikeFallDetectorService.Contact>>() {};
                                contacts = task.getResult().getValue(t);

                                for(BikeFallDetectorService.Contact contact: Objects.requireNonNull(contacts)) {
                                    smsManager.sendTextMessage(contact.getPhone_number(), null, message, null, null);
                                    smsManager.sendTextMessage(contact.getPhone_number(), null, googleMapsAddress, null, null);
                                }
                            } else {
                                Toast.makeText(this, "Failed to retrieve contact information.", Toast.LENGTH_LONG).show();
                            }
                        });
            });
        });
    }

    private void startBLEService() {
        Intent intent = new Intent(this, BikeFallDetectorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    private void stopBLEService() {
        Intent intent = new Intent(this, BikeFallDetectorService.class);
        stopService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantsResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantsResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Check Permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double alim = Math.sqrt(Math.abs(sensorEvent.values[0] * sensorEvent.values[0]) +
                Math.abs(sensorEvent.values[1] * sensorEvent.values[1]) +
                Math.abs(sensorEvent.values[2] * sensorEvent.values[2]));

        if(alim > 200.0) {
            // Start timer

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}