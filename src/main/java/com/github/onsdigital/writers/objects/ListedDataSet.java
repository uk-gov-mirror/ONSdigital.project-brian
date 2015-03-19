package com.github.onsdigital.writers.objects;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.data.objects.TimeSeriesPointComparator;

import java.util.Collections;
import java.util.HashMap;

/**
 * Created by thomasridd on 17/03/15.
 */
public class ListedDataSet {
    public String name;
    public String source;

    public HashMap<String, TimeSeries> listedTimeSeries = new HashMap<>();

    public ListedDataSet(DataSet dataSet) {
        name = dataSet.name;
        source = dataSet.source;

        for(TimeSeries series: dataSet.timeSeries.values()){
            listedTimeSeries.put(series.taxi, series);
        }
    }
}
