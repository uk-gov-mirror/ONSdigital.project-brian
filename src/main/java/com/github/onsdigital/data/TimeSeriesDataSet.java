package com.github.onsdigital.data;

import java.util.HashMap;

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

    public HashMap<String, TimeSeriesObject> timeSeries = new HashMap<>();

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

    public String newId(String prefix) {
        for(int i = 1; i<100000; i++){
            String id = String.format("%s%05d", prefix, i);
            if(!timeSeries.containsKey(id)) {
                return id;
            }
        }
        return "";
    }

    public TimeSeriesDataSet mergeWith(TimeSeriesDataSet newData, boolean newDataTakePrecedent) {
        for(TimeSeriesObject series: newData.timeSeries.values()) {

            if(timeSeries.containsKey(series.taxi)) {
                TimeSeriesObject destSeries = timeSeries.get(series.taxi);
                timeSeries.put(series.taxi, TimeSeriesObject.merge(series, destSeries, newDataTakePrecedent));
            } else {
                addSeries(series);
            }
        }
        return this;
    }
}
