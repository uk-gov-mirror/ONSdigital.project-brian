package com.github.onsdigital.data.objects;

import com.github.onsdigital.data.TimeSeriesObject;

import java.util.HashMap;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeriesTable {

    public String source;  // WORKBOOK
    public String source2; // WORKSHEET

    public String name; // TABLE NAME
    public String taxi; // TAXONOMY-BASED ID

    public HashMap<String , TimeSeriesObject> serieses = new HashMap<>();

    public void addSeries(TimeSeriesObject series) {
        serieses.put(series.taxi, series);
    }
}
