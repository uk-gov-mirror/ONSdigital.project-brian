package com.github.onsdigital.brian.data;

import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;

import java.util.ArrayList;
import java.util.HashMap;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeriesObject {

    public String taxi;
    public String name;
    public boolean hasYearly = false;
    public boolean hasMonthly = false;
    public boolean hasQuarterly = false;

    public transient boolean shouldHaveYearly = false;
    public transient boolean shouldHaveMonthly = false;
    public transient boolean shouldHaveQuarterly = false;

    public HashMap<String, TimeSeriesPoint> points = new HashMap<>();

    /**
     * INSERT A POINT
     *
     * @param point
     */
    public void addPoint(TimeSeriesPoint point) {
        points.put(point.timeLabel, point);
        if(point.period == TimeSeriesPoint.PERIOD_YEARS){
            hasYearly = true;
        } else if(point.period.equals(TimeSeriesPoint.PERIOD_MONTHS)) {
            hasMonthly = true;
        } else if(point.period.equals((TimeSeriesPoint.PERIOD_QUARTERS))) {
            hasQuarterly = true;
        }
    }

    /**
     * ACCESS POINT FOR A TIME LABEL
     *
     * @param timeLabel
     * @return
     */
    public TimeSeriesPoint getPoint(String timeLabel) {
        return points.get(timeLabel);
    }

    @Override
    public String toString() {
        String str = "";
        for(String key: points.keySet()) {
            TimeSeriesPoint point = points.get(key);
            str = String.format("%s(%s, %s) ", str, point.timeLabel, point.value);
        }
        return str;
    }
    /**
     * FILL BREAKS IN CONTINUITY WITH BLANKS
     */
    public void fillInTheBlanks() {
        ArrayList <String> periods = new ArrayList<>();
        if(hasYearly) { periods.add(TimeSeriesPoint.PERIOD_YEARS);}
        if(hasMonthly) { periods.add(TimeSeriesPoint.PERIOD_MONTHS);}
        if(hasQuarterly) { periods.add(TimeSeriesPoint.PERIOD_QUARTERS);}

        for(String period: periods) {

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
        }
    }

    /**
     * REVERSE fillInTheBlanks
     *
     * @return TIMESERIES WHERE ALL POINTS CONTAIN VALUES
     */
    public TimeSeriesObject noBlanks() {
        TimeSeriesObject series = new TimeSeriesObject();
        series.taxi = taxi;
        series.name = name;
        for(TimeSeriesPoint point: points.values()) {
            if(point.value.length() > 0) {
                series.addPoint(point);
            }
        }
        return series;
    }

    /**
     *
     * MERGES TWO TIMESERIES
     *
     * @param left timeseries 1
     * @param right timeseries 2
     * @param leftTakesPrecedent which timeseries is definitive
     * @return
     */
    public static TimeSeriesObject merge(TimeSeriesObject left, TimeSeriesObject right, boolean leftTakesPrecedent) {
        TimeSeriesObject merged = leftTakesPrecedent ? left.noBlanks() : right.noBlanks();
        TimeSeriesObject add = leftTakesPrecedent ? right.noBlanks() : left.noBlanks();
        for(TimeSeriesPoint point: add.points.values()) {
            if(merged.points.containsKey(point.timeLabel) == false) {
                merged.addPoint(point);
            }
        }

        merged.fillInTheBlanks();
        return merged;
    }

    public static void main(String[] args) {
        TimeSeriesObject series = new TimeSeriesObject();

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

    public boolean isComplete() {
        return (hasMonthly == shouldHaveMonthly) & (hasQuarterly == shouldHaveQuarterly) & (hasYearly == shouldHaveYearly);
    }
}
