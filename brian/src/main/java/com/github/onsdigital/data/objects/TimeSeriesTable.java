package com.github.onsdigital.data.objects;

import com.github.onsdigital.data.TimeSeries;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeriesTable {

    public String source;  // WORKBOOK
    public String source2; // WORKSHEET

    public String name; // TABLE NAME
    public String taxi; // TAXONOMY-BASED ID

    public HashMap<String , TimeSeries> serieses = new HashMap<>();

    public void addSeries(TimeSeries series) {
        serieses.put(series.taxi, series);
    }
}
