package com.example.gps_positioning.gpx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "trk")
public class Track {

    @Element(name = "trkseg")
    private TrackSegment segment = new TrackSegment();

    public TrackSegment getSegment() {
        return segment;
    }

    public void setSegment(TrackSegment segment) {
        this.segment = segment;
    }

}
