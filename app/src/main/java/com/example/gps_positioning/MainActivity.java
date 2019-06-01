package com.example.gps_positioning;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    // Declare GUI-Elements
    private TextView latView;
    private TextView longView;
    private TextView distView;
    private TextView speedView;
    private Button btnStart;
    private Button btnStop;
    private Button btnUpdate;
    private Button btnExit;

    // Permission-Code
    private static final int PERMISSION_REQUEST_CODE = 1;

    // Declaration of main thread and context
    private Handler userInterfaceUpdateHandler;
    private Context context;

    // Attributes needed for RPC
    GPSService GPSService;
    boolean isBound = false;

    // Formatter for trimming doubles
    NumberFormat formatter = new DecimalFormat("#0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init context and main thread
        context = this;
        userInterfaceUpdateHandler = new Handler(Looper.getMainLooper());

        // Check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission()) {
                // Request permissions if necessary
                requestPermission();
            }
        }
        // Initialise the GUI
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh GUI
        userInterfaceUpdateHandler.post(new Runnable() {
            public void run() {
                refreshUI();
            }
        });
    }

    /**
     * This method initialises the GUI
     */
    private void initUI() {
        latView = findViewById(R.id.latView);
        longView = findViewById(R.id.longView);
        distView = findViewById(R.id.distView);
        speedView = findViewById(R.id.speedView);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnExit = findViewById(R.id.btnExit);
        // Enable/Disable button
        btnStart.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnStop.setEnabled(false);
        btnExit.setEnabled(true);
    }

    /**
     *  This method binds the activity to the service
     *
     * @param view
     */
    public void startGPS(View view) {
        // Bind to service
        Intent intent = new Intent(this, GPSService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        // Enable/Disable button
        btnStart.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnStop.setEnabled(true);
        Toast.makeText(context, "GPS started", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method unbinds the activity from the service.
     *
     * @param view
     */
    public void stopGPS(View view) {
        // Unbind from service
        unbindService(connection);
        isBound = false;
        // Enable/Disable button
        btnStart.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnStop.setEnabled(false);
        Toast.makeText(context, "GPS stopped", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method uses the main thread to update the GUI
     * @param view
     */
    public void updateValues(View view) {
        userInterfaceUpdateHandler.post(new Runnable() {
            public void run() {
                refreshUI();
            }
        });
    }

    /**
     * This method closes the activity and therefor also the service.
     *
     * @param view
     */
    public void exitApp(View view) {
        finish();
    }

    /**
     * This method refreshes the GUI
     */
    public void refreshUI() {
        if (isBound) {
            latView.setText("Latitude: " + Double.toString(GPSService.getLatitude()));
            longView.setText("Longitude: " + Double.toString(GPSService.getLongitude()));
            distView.setText("Distance: " + formatter.format(GPSService.getDistance()) + " m");
            speedView.setText("Speed: " + formatter.format(GPSService.getAverageSpeed()) + " km/h");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent i = new Intent(this, GPSService.class);
        stopService(i);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
            GPSService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    /**
     *  This method checks if the app has the needed permissions
     * @return hasPermission
     */
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  This method requests needed permissions for GPS and Storage
     */
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            Toast.makeText(this, "This app requires permission to access your GPS data.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                } else {
                    Log.e("value", "Permission Denied");
                }
                break;
        }
    }
}
