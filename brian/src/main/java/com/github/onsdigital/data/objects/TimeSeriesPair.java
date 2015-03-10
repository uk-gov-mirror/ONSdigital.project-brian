package com.github.onsdigital.data.objects;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;

/**
 * Created by thomasridd on 06/03/15.
 */
public class TimeSeriesPair {
    public TimeSeries series;
    public TimeSeries masterSeries;
    double mapPower = -1;

    public TimeSeriesPair(TimeSeries fromSeries, TimeSeries masterSeries){
        this.series = fromSeries;
        this.masterSeries = masterSeries;
    }

    public static TimeSeriesPair getBestPairing(final TimeSeries fromSeries, DataSet master) {
        TimeSeries bestSeries = null;
        double mapPower = -1;

        // ITERATE THROUGH THE MASTER TO GET THE BEST MATCH FOR THE fromSeries
        for(TimeSeries masterSeries: master.timeSeries) {
            // MANIFEST FINDS THE NUMBER OF POINTS THAT WOULD BE NEEDED TO MERGE FROM INTO MASTER
            // SIMILARITY IGNORES ADDITIONS
            UpdateForSeries manifestForPair = new UpdateForSeries(fromSeries, masterSeries);
            if((bestSeries == null) || (manifestForPair.similarity() > mapPower)) {
                bestSeries = masterSeries;
                mapPower = manifestForPair.similarity();
            }
        }

        TimeSeriesPair bestPair = new TimeSeriesPair(fromSeries, bestSeries);
        bestPair.mapPower = mapPower;

        return bestPair;
    }

}
