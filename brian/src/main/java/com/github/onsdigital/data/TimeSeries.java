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
            hasMonthly = true;
        } else if(point.period.equals((TimeSeriesPoint.PERIOD_QUARTERS))) {
            hasQuarterly = true;
        }
    }

    // FILLS GAPS IN A SERIES WITH HOLES
    public void fillInTheBlanks() {
        ArrayList <String> periods = new ArrayList<>();
        if(hasYearly == true) { periods.add(TimeSeriesPoint.PERIOD_YEARS);}
        if(hasMonthly == true) { periods.add(TimeSeriesPoint.PERIOD_MONTHS);}
        if(hasQuarterly == true) { periods.add(TimeSeriesPoint.PERIOD_QUARTERS);}

        for(String period: periods) {
            try {
                TimeSeriesPoint minPoint = new TimeSeriesPoint("2049", "");
                TimeSeriesPoint maxPoint = new TimeSeriesPoint("1800", "");
                for(TimeSeriesPoint point: points.values()) {
                    if(point.period.equals(period) & point.startDate.after(maxPoint.startDate)) {
                        maxPoint = point;
                    }
                    if(point.period.equals(period) & point.startDate.before(minPoint.startDate)) {
                        minPoint = point;
                    }
                }

                // USING THE TimeSeriesPoint.nextTimeLabel FUNCTION STEP THROUGH OUR SERIES
                TimeSeriesPoint curPoint = new TimeSeriesPoint(TimeSeriesPoint.nextTimeLabel(minPoint.timeLabel), "");
                while(curPoint.startDate.before(maxPoint.startDate)) {
                    if(points.containsKey(curPoint.timeLabel) == false) {
                        points.put(curPoint.timeLabel, curPoint);
                    }
                    curPoint = new TimeSeriesPoint(TimeSeriesPoint.nextTimeLabel(curPoint.timeLabel), "");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }
    }

    public TimeSeriesPoint getPoint(String timeLabel) {
        return points.get(timeLabel);
    }

    public static void main(String[] args) throws ParseException {
        TimeSeries series = new TimeSeries();

        // Given
        //... a time series with holes in
        series.addPoint(new TimeSeriesPoint("2014 Jan", "100"));
        series.addPoint(new TimeSeriesPoint("2014 Oct", "100"));
        series.addPoint(new TimeSeriesPoint("2013 Nov", "100"));
        series.fillInTheBlanks();

        // When
        //... we search for points that we didn't add but exist
        String[] filledPoints = {"2013 Dec", "2014 Feb"};

        // Then
        //... we expect them to exist
        for(String timeLabel: filledPoints) {
            if(!series.points.containsKey(timeLabel)) {
                System.out.println("Expected point missing for: " + timeLabel);
            }
        }
    }
}
