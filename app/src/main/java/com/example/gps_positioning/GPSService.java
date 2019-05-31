package com.example.gps_positioning;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSService extends Service implements IGPSInterface {

    private final IBinder binder = new LocalBinder();

    private LocationListener locationListener;
    private LocationManager locationManager;

    private Double currentLat = 0.0;
    private Double currentLong = 0.0;
    private Double currentDistance = 0.0;

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
        return 0;
    }

    @Override
    public double getAverageSpeed() {
        return 0;
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

        currentLat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        currentLong = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        currentDistance = 0.0;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentDistance = currentDistance + calcDistance(location.getLatitude(), location.getLongitude(), currentLat, currentLong);
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();
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

}
