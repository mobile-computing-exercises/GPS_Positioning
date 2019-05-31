package com.example.gps_positioning;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class GPSService extends Service implements IGPSInterface {

    private final IBinder binder = new LocalBinder();

    private LocationListener locationListener;
    private LocationManager locationManager;

    private Double currentLat = 0.0;
    private Double currentLong = 0.0;
    private Double currentDistance = 0.0;
    private Double avgSpeed = 0.0;

    private long startTime;

    private File saveFile;

    private Context context;

    @Override
    public double getLatitude() {
        return currentLat;
    }

    @Override
    public double getLongitude() {
        return currentLong;
    }

    @Override
    public double getDistance() {
        return currentDistance;
    }

    @Override
    public double getAverageSpeed() {
        return avgSpeed;
    }

    public class LocalBinder extends Binder {
        GPSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        // Init managers
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        context = this;

        currentLat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        currentLong = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        currentDistance = 0.0;
        startTime = Calendar.getInstance().getTimeInMillis();
        saveFile = getPublicDownloadStorageDir();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentDistance = currentDistance + calcDistance(currentLat, currentLong, location.getLatitude(), location.getLongitude());
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();
                avgSpeed = calcSpeed();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

    }

    private static double calcDistance(double lastLat, double lastLng, double currentLat, double currentLng) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(currentLat-lastLat);
        double dLng = Math.toRadians(currentLng-lastLng);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lastLat)) * Math.cos(Math.toRadians(currentLat)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private double calcSpeed(){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        Long l = currentTime - startTime;
        Double timePassed = l.doubleValue();
        return (currentDistance/1000) / (timePassed/3600000);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicDownloadStorageDir() {
        File gpxFile = null;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            gpxFile = new File(root, "Route.gpx");
            FileWriter writer = new FileWriter(gpxFile);
            writer.append("Textx");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gpxFile;
    }

}
