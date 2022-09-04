package com.example.bikefalldetection;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BikeFallDetectorService extends Service {

    private static final UUID fallServiceUUID = UUID.fromString("00005321-0000-1000-8000-00805f9b34fb");
    private static final UUID fallCharacteristicUUID = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    public BluetoothCentralManager central;
    //private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    //private int currentTimeCounter = 0;
    private FusedLocationProviderClient fusedLocationClient;
    SharedPreferences contactPreferences;


    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
            super.onServicesDiscovered(peripheral);

            BluetoothGattCharacteristic fallCharacteristic = peripheral.getCharacteristic(fallServiceUUID, fallCharacteristicUUID);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
            peripheral.setNotify(Objects.requireNonNull(fallCharacteristic), true);
        }

        @Override
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status) {
            super.onCharacteristicUpdate(peripheral, value, characteristic, status);
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser;
            if (characteristicUUID.equals(fallCharacteristicUUID)) {
                parser = new BluetoothBytesParser(value);

                int flags = parser.getIntValue(0x11);
                if (flags == 1) {
                    // Send message
                    sendHelpMessage();
                }
            }
        }


    };

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral) {
            super.onConnectedPeripheral(peripheral);

            // Reconnect to this device when it becomes available again.
            handler.postDelayed(() -> central.autoConnectPeripheral(peripheral, peripheralCallback), 1000);

            Toast.makeText(getApplicationContext(), "Connected.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull HciStatus status) {
            super.onConnectionFailed(peripheral, status);

            Toast.makeText(getApplicationContext(), "Connection failed. Trying to reconnect.", Toast.LENGTH_LONG).show();

            // Reconnect
            central.connectPeripheral(peripheral, peripheralCallback);

        }

        @Override
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
            super.onDiscoveredPeripheral(peripheral, scanResult);

            Toast.makeText(getApplicationContext(), "Device discovered", Toast.LENGTH_LONG).show();

            // Stop scanning because a device has been discovered.
            central.stopScan();

            // Connect the device.
            central.connectPeripheral(peripheral, peripheralCallback);
        }
    };



    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void createNotificationChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("BLE Service", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);

        }
    }

    private void startScan() {
        handler.postDelayed(() -> central.scanForPeripheralsWithServices(new UUID[]{fallServiceUUID}), 2000);
    }

    private ArrayList<Contact> loadContacts() {
        contactPreferences = getApplicationContext().getSharedPreferences("Contact_Preferences", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String json = contactPreferences.getString("Contacts", "");

        Type type = new TypeToken<ArrayList<Contact>>() {}.getType();

        return gson.fromJson(json, type);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create a notification channel.
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, ControlDetection.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "BLE Service")
                    .setContentTitle("BLE Bike Fall Detection")
                    .setContentText("In case of an accident the app will alert all selected people.")
                    .setContentIntent(pendingIntent)
                    .build();
        }

        startForeground(1, notification);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        central = new BluetoothCentralManager(getApplicationContext(), bluetoothCentralManagerCallback, handler);
        startScan();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendHelpMessage() {

        // No need to check for permissions
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationClient.getLastLocation();

        SmsManager smsManager = SmsManager.getDefault();
        locationTask.addOnSuccessListener(location -> {
            double latitude, longitude;

            // Get user latitude and longitude
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            // Set the message and the google map address
            String message = "Βοήθεια! Συνέβει ατύχημα σε αυτή την τοποθεσία : " + latitude + " " + longitude;
            String googleMapsAddress = "https://www.google.com/maps/search/?api=1&query=" + latitude + "%2C" + longitude;

            // Load the contacts
            ArrayList<Contact> contacts = loadContacts();

            // Send a message to each contact
            for(Contact contact: contacts) {
                smsManager.sendTextMessage(contact.getPhone(), null, message, null, null);
                smsManager.sendTextMessage(contact.getPhone(), null, googleMapsAddress, null, null);
            }

        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
