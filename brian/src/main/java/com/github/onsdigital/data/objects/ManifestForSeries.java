package com.github.onsdigital.data.objects;

import com.github.onsdigital.data.TimeSeries;

import java.util.ArrayList;

/**
 * Created by Tom.Ridd on 05/03/15.
 */
public class ManifestForSeries {
    public ArrayList<ManifestPoint> updates = new ArrayList<>(); // The updates that are going to be made

    public TimeSeries destination; // The series that is going to be updated (there for the metadata)
    public TimeSeries origin;

    public int updateCount = 0;
    public int createCount = 0;
    public int matchCount = 0;

    /*
    * CALCULATES THE MINIMUM OPERATIONS SERIES TO MERGE SERIES INTO MASTER
     */
    public ManifestForSeries(TimeSeries series, TimeSeries master) {
        this.origin = series;
        this.destination = master;
        this.matchCount = master.points.size();

        for(String key : series.points.keySet() ) {
            TimeSeriesPoint point = series.points.get(key);
            TimeSeriesPoint masterPoint = master.points.get(key);

            if(masterPoint != null) {
                if(point.value.equals(masterPoint.value)) {
                    this.matchCount += 1;
                } else {
                    ManifestPoint update = new ManifestPoint(point, masterPoint);
                    this.updates.add(update);
                    this.updateCount += 1;
                }
            } else {
                ManifestPoint newPoint = new ManifestPoint(point, null);
                this.updates.add(newPoint);
                this.createCount += 1;
            }
        }
    }

    // TWEAKABLE METRIC FOR DETERMINING THE POWER OF THE MATCH
    public double similarity() {
        if( (matchCount + updateCount) > 0) {
            return matchCount / (matchCount + updateCount);
        } else {
            return 1.0;
        }
    }
}
