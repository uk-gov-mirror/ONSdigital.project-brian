package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.writers.objects.ListedTimeSeries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by thomasridd on 11/03/15.
 */
public class SeriesWriterJSON {

    public static String seriesAsJSON(TimeSeries series) {
        return Serialiser.serialise(series);
    }
    public static String seriesAsJSON(TimeSeries series, boolean prettyPrinting) {
        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(series);
    }

    public static String seriesAsSortedJSON(TimeSeries series) {
        return seriesAsSortedJSON(series, false);
    }
    public static String seriesAsSortedJSON(TimeSeries series, boolean prettyPrinting) {
        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(new ListedTimeSeries(series));
    }

    public static void writeToJsonFile(TimeSeries series, File file) throws IOException {
        FileUtils.writeStringToFile(file, seriesAsJSON(series));
    }
    public static void writeToJsonFile(TimeSeries series, File file, boolean prettyPrinting) throws IOException {
        FileUtils.writeStringToFile(file, seriesAsJSON(series, prettyPrinting));
    }

    public static void main(String[] args) throws ParseException, IOException {
        TimeSeries series = new TimeSeries();
        series.addPoint(new TimeSeriesPoint("2014 Sep", "300"));
        series.addPoint(new TimeSeriesPoint("2014 Q3", "200"));
        series.addPoint(new TimeSeriesPoint("2013", "100"));
        series.fillInTheBlanks();

        System.out.println(SeriesWriterJSON.seriesAsJSON(series));

        writeToJsonFile(series, new File("./series.json"),true);
    }
}
