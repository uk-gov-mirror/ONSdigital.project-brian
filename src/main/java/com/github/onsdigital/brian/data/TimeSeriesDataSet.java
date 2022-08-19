package com.github.onsdigital.brian.data;

import java.util.LinkedHashMap;

/**
 * Created by Tom.Ridd on 03/03/15.
 *
 * THIS VERSION OF THE ONS DATA MODEL STRUCTURE IS BUILT TO
 * AID THE CONVERSION PROCESS
 * THE CASCADE DataSet->TimeSeriesTable->TimeSeries->TimeSeriesPoint REFLECTS THE WORKSHEET BASED READ IN
 * REQUIREMENTS FOR OUR DATA
 *
 * DATASET IS WORKBOOK LEVEL
 */
public class TimeSeriesDataSet implements Cloneable {
    public String name;
    public String source;

    public LinkedHashMap<String, TimeSeriesObject> timeSeries = new LinkedHashMap<>();

    @Override
    public TimeSeriesDataSet clone(){
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        timeSeriesDataSet.name = name;
        timeSeriesDataSet.source = source;
        for(TimeSeriesObject series: timeSeries.values()) {
            timeSeriesDataSet.addSeries(series);
        }
        return timeSeriesDataSet;
    }

    public void addSeries(TimeSeriesObject series) {
        timeSeries.put(series.taxi, series);
    }

    public TimeSeriesObject getSeries(String taxi) {
        return timeSeries.get(taxi);
    }

}
