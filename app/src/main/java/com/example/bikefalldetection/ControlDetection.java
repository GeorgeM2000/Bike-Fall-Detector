package com.example.bikefalldetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class ControlDetection extends AppCompatActivity implements SensorEventListener {

    ImageButton start_service, stop_service, abort_timer, send_help;
    TextView textViewSeconds, textViewMilliseconds;
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private Sensor accelerometer, linearAccelerometer;
    private int duration = 30;
    private CountDownTimer countDownTimer = null;
    private final long[] pattern = {0, 100, 1000};
    private SharedPreferences contactPreferences;
    private SharedPreferences settingsPreferences;
    private Boolean detectionMethod;
    private static final int coordinatesStackSize = 20;
    protected static final Stack<Double[]> coordinatesStack = new FixedStack<>(coordinatesStackSize);


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_detection);

        // Buttons.
        start_service = findViewById(R.id.start_service);
        stop_service = findViewById(R.id.stop_service);
        abort_timer = findViewById(R.id.abort_timer);
        send_help = findViewById(R.id.send_help);

        // Fused location provider client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Timer.
        textViewSeconds = findViewById(R.id.seconds);
        textViewMilliseconds = findViewById(R.id.milliseconds);

        // Sensor.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Vibrator.
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /*
            Before the service starts, get the phone numbers from the firebase
            and store them in shared preferences.
        */
        getContacts();

        // When the user clicks the start service icon button
        start_service.setOnClickListener(view -> {

            // Get the settings value.
            settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            detectionMethod = settingsPreferences.getBoolean("mobile_phone_detection", false);

            // If the user has chosen to disable the "mobile phone detection" function...
            if (!detectionMethod) {

                // Check for permissions.
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Check Permissions.", Toast.LENGTH_SHORT).show();
                } else {
                    // Start the service.
                    startBLEService();

                    // Start listening to accelerometer values.
                    sensorManager.registerListener(ControlDetection.this, linearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
            // If the user has chosen to enable the "mobile phone detection" function...
            else {

                // Check for permissions.
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Check Permissions.", Toast.LENGTH_SHORT).show();
                } else {

                    // Start listening to accelerometer values.
                    sensorManager.registerListener(ControlDetection.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }

            Toast.makeText(this, "Service has started", Toast.LENGTH_SHORT).show();
        });

        // When the user clicks the stop service icon button
        stop_service.setOnClickListener(view -> {

            // Check for permissions
            //ActivityCompat.requestPermissions(ControlDetection.this, permissions, 4);

            // Stop service
            stopBLEService();

            // Stop listening to accelerometer values
            sensorManager.unregisterListener(ControlDetection.this);

            Toast.makeText(this, "Service has stopped", Toast.LENGTH_SHORT).show();

        });

        // When the user clicks the abort timer icon button
        abort_timer.setOnClickListener(view -> {

            // If the timer has started.
            if (countDownTimer != null) {

                // Cancel the timer.
                countDownTimer.cancel();
                countDownTimer = null;

                // Cancel the vibrator.
                vibrator.cancel();

                // Reset the duration to 30 sec.
                duration = 30;

                // Set seconds and milliseconds in text view back to 30 sec.
                textViewSeconds.setText("30");
                textViewMilliseconds.setText("00");

                Toast.makeText(this, "Timer aborted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Timer hasn't started", Toast.LENGTH_LONG).show();
            }

        });

        // When the user clicks the send help icon button
        send_help.setOnClickListener(view -> {

            // Get user location and send help message
            sendHelpMessage();
        });
    }

    private ArrayList<Contact> loadContacts() {
        contactPreferences = getApplicationContext().getSharedPreferences("Contact_Preferences", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String json = contactPreferences.getString("Contacts", "");

        Type type = new TypeToken<ArrayList<com.example.bikefalldetection.Contact>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    private void saveContacts(List<com.example.bikefalldetection.Contact> contacts) {
        contactPreferences = getApplicationContext().getSharedPreferences("Contact_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = contactPreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(contacts);

        editor.putString("Contacts", json);
        editor.apply();
    }

    private void getContacts() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("contacts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<com.example.bikefalldetection.Contact> contacts;
                        GenericTypeIndicator<List<com.example.bikefalldetection.Contact>> t = new GenericTypeIndicator<List<com.example.bikefalldetection.Contact>>() {
                        };
                        contacts = task.getResult().getValue(t);

                        // Save contacts
                        saveContacts(contacts);

                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to retrieve contact information.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendHelpMessage() {
        SmsManager smsManager = SmsManager.getDefault();

        // Set the message and the google map address
        String message = "Βοήθεια! Συνέβει ατύχημα σε αυτή την τοποθεσία : " + coordinatesStack.get(coordinatesStack.size() - 1)[0] + " " + coordinatesStack.get(coordinatesStack.size() - 1)[1];
        String googleMapsAddress = "https://www.google.com/maps/search/?api=1&query=" + coordinatesStack.get(coordinatesStack.size() - 1)[0] + "%2C" + coordinatesStack.get(coordinatesStack.size() - 1)[1];

        // Load contacts.
        ArrayList<Contact> contacts = loadContacts();

        for (com.example.bikefalldetection.Contact contact : Objects.requireNonNull(contacts)) {
            smsManager.sendTextMessage(contact.getPhone(), null, message, null, null);
            smsManager.sendTextMessage(contact.getPhone(), null, googleMapsAddress, null, null);
        }
    }

    private void storeUsersCoordinates() {

        // No need to check for permissions. We check for permissions when the user clicks the "start service" button.
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationClient.getLastLocation();

        locationTask.addOnSuccessListener(location -> coordinatesStack.push(new Double[]{location.getLatitude(), location.getLongitude()}));
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


    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Get user coordinates and store them in a stack of fixed size.
        storeUsersCoordinates();
        
        if(!detectionMethod) {
            double alim = Math.sqrt(Math.abs(sensorEvent.values[0] * sensorEvent.values[0]) +
                    Math.abs(sensorEvent.values[1] * sensorEvent.values[1]) +
                    Math.abs(sensorEvent.values[2] * sensorEvent.values[2]));

            // If the linear acceleration variable(alim) exceeds a threshold.
            if(alim > 20.0) {

                // Start timer
                if(countDownTimer == null) {

                    // Start vibrating if the vibrator is available.
                    if(vibrator.hasVibrator()) {
                        vibrator.vibrate(pattern, 0);
                        Log.i("GeorgeMLog", "Vibrator has started.");
                    }

                    countDownTimer = new CountDownTimer(duration * 1000L, 1000) {
                        @Override
                        public void onTick(long l) {
                            runOnUiThread(() -> {


                                @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toSeconds(l) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)),
                                        TimeUnit.MILLISECONDS.toMillis(l) -
                                                TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(l)));


                                final String[] seconds_milliseconds = time.split(":");

                                textViewSeconds.setText(seconds_milliseconds[0]);
                                textViewMilliseconds.setText(seconds_milliseconds[1]);

                            });
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFinish() {
                            // Reset the duration to 30 sec
                            duration = 30;

                            // Set seconds and milliseconds in text view back to 30 sec
                            textViewSeconds.setText("30");
                            textViewMilliseconds.setText("00");

                            // Stop vibrating
                            vibrator.cancel();

                            // Send help message
                            sendHelpMessage();

                        }
                    }.start();
                }
            }
        } else {
            // If the linear acceleration variable(alim) exceeds a threshold.
            if(Math.abs(sensorEvent.values[0]) > 5.0) {

                // Start timer
                if(countDownTimer == null) {

                    // Start vibrating if the vibrator is available.
                    if(vibrator.hasVibrator()) {
                        vibrator.vibrate(pattern, 0);
                        Log.i("GeorgeMLog", "Vibrator has started.");
                    }

                    countDownTimer = new CountDownTimer(duration * 1000L, 1000) {
                        @Override
                        public void onTick(long l) {
                            runOnUiThread(() -> {

                                @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toSeconds(l) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)),
                                        TimeUnit.MILLISECONDS.toMillis(l) -
                                                TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(l)));

                                final String[] seconds_milliseconds = time.split(":");

                                textViewSeconds.setText(seconds_milliseconds[0]);
                                textViewMilliseconds.setText(seconds_milliseconds[1]);

                            });
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFinish() {
                            // Reset the duration to 30 sec
                            duration = 30;

                            // Set seconds and milliseconds in text view back to 30 sec
                            textViewSeconds.setText("30");
                            textViewMilliseconds.setText("00");

                            // Stop vibrating
                            vibrator.cancel();

                            // Send help message
                            sendHelpMessage();

                        }
                    }.start();
                }
            } else {

                // If the timer has started.
                if (countDownTimer != null) {

                    // Cancel the timer.
                    countDownTimer.cancel();
                    countDownTimer = null;

                    // Cancel the vibrator.
                    vibrator.cancel();

                    // Reset the duration to 30 sec.
                    duration = 30;

                    // Set seconds and milliseconds in text view back to 30 sec.
                    textViewSeconds.setText("30");
                    textViewMilliseconds.setText("00");
                }

            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    /*
    Redirect the user back to the Dashboard page when the back touch button is pressed.
     */
    @Override public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ControlDetection.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}