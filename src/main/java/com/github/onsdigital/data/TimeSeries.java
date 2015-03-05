package com.github.onsdigital.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeries {

    public String taxi;
    public String name;

    public ArrayList<TimeSeriesPoint> data = new ArrayList<>();
    public HashMap<String, TimeSeriesPoint> points = new HashMap<>();

    public void addPoint(TimeSeriesPoint point) throws ParseException {
        points.put(point.timeLabel, point);
    }

}
