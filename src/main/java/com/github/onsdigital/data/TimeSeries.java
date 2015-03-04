package com.github.onsdigital.data;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeries {

    public String taxi;
    public String cdid;
    public String name;
    //TODO: ALPHA USES A TREE SET - REQUIRES IMPLEMENTING COMPARABLE
    public ArrayList<TimeSeriesPoint> data = new ArrayList<>();
}
