package com.github.onsdigital.data;

import java.util.ArrayList;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeriesTable {

    public String source;  // WORKBOOK
    public String source2; // WORKSHEET

    public String name; // TABLE NAME
    public String taxi; // TAXONOMY-BASED ID

    public ArrayList<TimeSeries> series = new ArrayList<>();
}
