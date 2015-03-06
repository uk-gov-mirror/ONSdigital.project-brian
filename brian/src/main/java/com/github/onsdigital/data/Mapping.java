package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.TimeSeriesPair;

import java.util.ArrayList;

/**
 * Created by thomasridd on 06/03/15.
 *
 * A MAP THAT PAIRS UP THE SERIES FROM ONE DATASET TO THOSE IN ANOTHER.
 *
 * THE IDEA IS FOR A LEFT OUTER JOIN SO LANGUAGE HAS BEEN PHRASED ACCORDINGLY
 *
 */
public class Mapping {
    public ArrayList<TimeSeriesPair> map = new ArrayList<>();

    public static Mapping getSuggestedMapping(DataSet dataSet, DataSet master) {
        Mapping mapping = new Mapping();

        // MAP EVERY SERIES IN dataset TO A SERIES IN masterdataset
        mapping.map = new ArrayList<>();
        for(TimeSeries series: dataSet.timeSeries) {
            mapping.map.add(TimeSeriesPair.getBestPairing(series, master));
        }
        return mapping;
    }
}
