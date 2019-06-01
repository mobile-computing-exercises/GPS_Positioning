package com.example.gps_positioning.gpx;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

@Root(name = "trkseg")
public class TrackSegment {

    @ElementList(entry = "trkpt", inline = true, required = false)
    List<TrackPoint> points = new ArrayList<TrackPoint>();

    public List<TrackPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TrackPoint> points) {
        this.points = points;
    }

    public void addPoint(TrackPoint point) {
        this.points.add(point);
    }

}
