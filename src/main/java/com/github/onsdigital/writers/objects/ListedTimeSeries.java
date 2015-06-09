package com.github.onsdigital.writers.objects;

import com.github.onsdigital.data.TimeSeriesObject;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.data.objects.TimeSeriesPointComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by thomasridd on 13/03/15.
 */
public class ListedTimeSeries {
    public String taxi;
    public String name;
    public Boolean hasYearly = false;
    public Boolean hasMonthly = false;
    public Boolean hasQuarterly = false;
    public ArrayList<TimeSeriesPoint> points = new ArrayList<>();

    public ListedTimeSeries(TimeSeriesObject series) {
        taxi = series.taxi;
        name = series.name;
        hasMonthly = series.hasMonthly;
        hasQuarterly = series.hasQuarterly;
        hasYearly = series.hasYearly;

        for(TimeSeriesPoint point: series.points.values()){
            points.add(point);
        }

        Collections.sort(points, new TimeSeriesPointComparator());
    }

    public TimeSeriesObject toTimeSeries() {
        TimeSeriesObject series = new TimeSeriesObject();
        series.taxi = taxi;
        series.name = name;
        series.hasQuarterly = hasQuarterly;
        series.hasYearly = hasYearly;
        series.hasMonthly = hasMonthly;

        for(TimeSeriesPoint point: points) {
            series.addPoint(point);
        }

        return series;
    }
}
