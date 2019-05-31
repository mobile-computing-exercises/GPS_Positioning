package com.example.gps_positioning;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView latView;
    private TextView longView;
    private TextView distView;
    private TextView speedView;

    private Button btnStart;
    private Button btnStop;
    private Button btnUpdate;

    private static final int PERMISSION_REQUEST_CODE = 1;

    private BroadcastReceiver broadcastReceiver;

    private String currentLat = "";
    private String currentLong = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission()) {
                requestPermission();
            }
        }

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getExtras().get("latitude") != null) {
                        currentLat = (String) intent.getExtras().get("latitude");
                    }
                    if (intent.getExtras().get("longitude") != null) {
                        currentLong = (String) intent.getExtras().get("longitude");
                    }
                }
            };
        }

        // Register receiver for all Intents
        registerReceiver(broadcastReceiver, new IntentFilter("current_coordinates"));

    }

    private void initUI() {
        latView = findViewById(R.id.latView);
        longView = findViewById(R.id.longView);
        distView = findViewById(R.id.distView);
        speedView = findViewById(R.id.speedView);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    public void startGPS(View view) {
        Intent i = new Intent(this, GPSService.class);
        startService(i);
    }

    public void stopGPS(View view) {
        Intent i = new Intent(this, GPSService.class);
        stopService(i);
    }

    public void updateValues(View view) {
        latView.setText(currentLat);
        longView.setText(currentLong);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            Toast.makeText(this, "This app requires permission to access your GPS data.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                    Intent i = new Intent(this, GPSService.class);
                    startService(i);
                } else {
                    Log.e("value", "Permission Denied");
                }
                break;
        }
    }
}
