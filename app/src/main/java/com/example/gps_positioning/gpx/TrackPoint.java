package com.example.gps_positioning.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "trkpt")
public class TrackPoint {

    @Attribute(name = "lat")
    private double lat;

    @Attribute(name = "lon")
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

}
