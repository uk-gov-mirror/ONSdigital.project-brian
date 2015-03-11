package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.TimeSeriesPoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeries {

    public String taxi;
    public String name;
    public Boolean hasYearly = false;
    public Boolean hasMonthly = false;
    public Boolean hasQuarterly = false;

    public HashMap<String, TimeSeriesPoint> points = new HashMap<>();

    public void addPoint(TimeSeriesPoint point) throws ParseException {
        points.put(point.timeLabel, point);
        if(point.period == TimeSeriesPoint.PERIOD_YEARS){
            hasYearly = true;
        } else if(point.period.equals(TimeSeriesPoint.PERIOD_MONTHS)) {

        }
    }

    public void fillInTheBlanks() {

    }

    public TimeSeriesPoint getPoint(String timeLabel) {
        return points.get(timeLabel);
    }
}
