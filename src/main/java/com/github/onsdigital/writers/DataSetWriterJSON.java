package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.DataFuture;
import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.generators.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by thomasridd on 11/03/15.
 */
public class DataSetWriterJSON {

    public static String dataSetAsJSON(TimeSeriesDataSet timeSeriesDataSet, boolean prettyPrinting) {
        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(timeSeriesDataSet);
    }
    public static String dataSetAsJSON(DataFuture dataFuture, boolean prettyPrinting) {

        List<HashMap<String, String>> summary = new ArrayList<>();

        for(String key: dataFuture.timeSeries.keySet()) {
            HashMap<String, String> series = new HashMap<>();
            series.put("taxi", key);
            if(dataFuture.timeSeries.get(key).isDone()) {
                try {
                    series.put("name", dataFuture.timeSeries.get(key).get().name.trim());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            summary.add(series);
        }

        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(summary);
    }

    public static void main(String[] args) {
        TimeSeriesDataSet timeSeriesDataSet = Sample.quickWalks(10, 1);
        System.out.println(dataSetAsJSON(timeSeriesDataSet,true));
    }

}

