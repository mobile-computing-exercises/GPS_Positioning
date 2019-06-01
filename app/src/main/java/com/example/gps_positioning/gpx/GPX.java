package com.example.gps_positioning.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "gpx")
public class GPX {

    @Attribute(name = "version", required = false)
    public double version = 1.1;
    @Attribute(name = "creator", required = false)
    public String creator = "GPSService";
    @Element(name = "trk", required = false)
    public Track track = new Track();

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
