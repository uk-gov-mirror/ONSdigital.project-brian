package com.github.onsdigital.data;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 19/03/15.
 */
public class DataFuture {
    public String name;
    public String source;

    public HashMap<String, Future<TimeSeriesObject>> timeSeries = new HashMap<>();


    public void addSeries(String taxi, Future<TimeSeriesObject> series) {
        timeSeries.put(taxi, series);
    }

    public TimeSeriesObject getSeries(String taxi)  {
        try {
            return timeSeries.get(taxi).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
