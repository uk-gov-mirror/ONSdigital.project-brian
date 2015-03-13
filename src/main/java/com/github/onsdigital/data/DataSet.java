package com.github.onsdigital.data;

import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.data.objects.TimeSeriesTable;
import com.github.onsdigital.readers.Csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class DataSet implements Cloneable {
    public String name;
    public String source;

    public HashMap<String, TimeSeries> timeSeries = new HashMap<>();

    @Override
    public DataSet clone(){
        DataSet dataSet = new DataSet();
        dataSet.name = name;
        dataSet.source = source;
        for(TimeSeries series: timeSeries.values()) {
            addSeries(series);
        }
        return dataSet;
    }

    public void addSeries(TimeSeries series) {
        timeSeries.put(series.taxi, series);
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

    public void mergeWith(DataSet newData, boolean newDataTakePrecedent) {
        for(TimeSeries series: newData.timeSeries.values()) {

            if(timeSeries.containsKey(series.taxi)) {
                TimeSeries destSeries = timeSeries.get(series.taxi);
                timeSeries.put(series.taxi, TimeSeries.merge(series, destSeries, newDataTakePrecedent));
            } else {
                addSeries(series);
            }
        }
    }
}
