package com.github.onsdigital.writers.objects;

import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.data.TimeSeriesObject;

import java.util.HashMap;

/**
 * Created by thomasridd on 17/03/15.
 */
public class ListedDataSet {
    public String name;
    public String source;

    public HashMap<String, TimeSeriesObject> listedTimeSeries = new HashMap<>();

    public ListedDataSet(TimeSeriesDataSet timeSeriesDataSet) {
        name = timeSeriesDataSet.name;
        source = timeSeriesDataSet.source;

        for(TimeSeriesObject series: timeSeriesDataSet.timeSeries.values()){
            listedTimeSeries.put(series.taxi, series);
        }
    }
}
