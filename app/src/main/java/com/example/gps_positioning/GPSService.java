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
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;

import com.example.gps_positioning.gpx.GPX;
import com.example.gps_positioning.gpx.TrackPoint;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class GPSService extends Service implements IGPSInterface {

    // Binder
    private final IBinder binder = new LocalBinder();
    private Context context;
    // Location
    private LocationListener locationListener;
    private LocationManager locationManager;
    // Location values
    private Double currentLat = 0.0;
    private Double currentLong = 0.0;
    private Double currentDistance = 0.0;
    private Double avgSpeed = 0.0;
    // Starttime of Service
    private long startTime;
    // Savefile of coordinates
    private File saveFile;

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
        // Init manager and context
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        context = this;

        // Set initial values
        currentLat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        currentLong = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        currentDistance = 0.0;
        startTime = Calendar.getInstance().getTimeInMillis();
        // Init savefile (create and fill)
        saveFile = initSaveFile();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentDistance = currentDistance + calcDistance(currentLat, currentLong, location.getLatitude(), location.getLongitude());
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();
                avgSpeed = calcSpeed();
                updateFile(saveFile, coordinatesToTrackPoint(currentLat, currentLong), readGpxFromFile(saveFile));
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

    /**
     * This method calculates the distance of two geographical poinst in meters
     *
     * @param lastLat
     * @param lastLng
     * @param currentLat
     * @param currentLng
     * @return distanceInMeters
     */
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

    /**
     * This method calculates the average speed of the user
     * @return speed
     */
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

    /**
     * This method initialises the gpx file that is saved to the external storage.
     * When initialising, the old .gpx will be overwritten.
     * @return
     */
    public File initSaveFile() {
        File gpxFile = null;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "MobileComputing");
            if (!root.exists()) {
                root.mkdirs();
            }
            gpxFile = new File(root, "Route.gpx");
            // Old file will be overwritten
            FileWriter writer = new FileWriter(gpxFile, false);
            // Initialise with initial content
            writer.append(pojoToXml(new GPX()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gpxFile;
    }

    /**
     * This method uses the SimpleFramework to create an XML-String from a POJO.
     * String will be returned and can be saved to a file for example.
     *
     * @return xml
     */
    private String pojoToXml(GPX gpxContent) {
        StringWriter writer = new StringWriter();
        Serializer serializer = new Persister();

        // Try to map pojo to xml
        try {
            serializer.write(gpxContent, writer);
            String xml = writer.getBuffer().toString();
            return xml;
        } catch (Exception e) {
            // If error occurs don't return empty string
            return "";
        }
    }

    /**
     * This method updates an gpx object with a new track-point.
     *
     * @param gpxContent
     * @param newPoint
     * @return
     */
    private GPX updatePojo(GPX gpxContent, TrackPoint newPoint) {
        gpxContent.getTrack().getSegment().getPoints().add(newPoint);
        return gpxContent;
    }

    /**
     * This method updates the gpx File by replacing it with updated values.
     *
     * @param gpxFile
     * @param point
     * @param gpxContent
     */
    private void updateFile(File gpxFile, TrackPoint point, GPX gpxContent) {
        try {
            FileWriter writer = new FileWriter(gpxFile, false);
            // Initialise with initial content
            writer.append(pojoToXml(updatePojo(gpxContent, point)));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method uses the SimpleFramework to map an XML/GPX-File to a POJO.
     *
     * @param file
     * @return pojo
     */
    private GPX readGpxFromFile(File file) {
        try {
            Serializer serializer = new Persister();
            return serializer.read(GPX.class, file);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method creates a TrackPoint from new coordinates.
     *
     * @param lat
     * @param lng
     * @return newPoint
     */
    private TrackPoint coordinatesToTrackPoint(double lat, double lng) {
        TrackPoint newPoint = new TrackPoint();
        newPoint.setLat(lat);
        newPoint.setLng(lng);
        return newPoint;
    }

}
