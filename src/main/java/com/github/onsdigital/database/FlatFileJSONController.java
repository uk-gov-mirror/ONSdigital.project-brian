package com.github.onsdigital.database;


import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.readers.SeriesReaderJSON;
import com.github.onsdigital.writers.SeriesWriterJSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by thomasridd on 17/03/15.
 */
public class FlatFileJSONController {

    private static String suffix = ".json";
    private static Path filePath;
    private static boolean doPrettyPrint = true;

    public static void initialise() {
        try {
            filePath = Paths.get(FlatFileJSONController.class.getResource("/flatfiles").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void create(TimeSeries series) throws IOException {
        Path path = filePath.resolve(series.taxi + suffix);

        SeriesWriterJSON.writeSeriesToJSON(series, path, doPrettyPrint);
    }

    public static void update(TimeSeries series) throws IOException {
        Path path = filePath.resolve(series.taxi + suffix);
        if(exists(series.taxi)) {
            SeriesWriterJSON.writeSeriesToJSON(series, path, doPrettyPrint);
        }
    }

    public static void delete(String taxi) throws IOException {
        if(exists(taxi)) {
            Path path = filePath.resolve(taxi + suffix);
            Files.delete(path);
        }
    }

    public static boolean exists(String taxi) {
        Path path = filePath.resolve(taxi + suffix);
        return Files.exists(path);
    }

    public static TimeSeries get(String taxi) {
        Path path = filePath.resolve(taxi + suffix);
        return SeriesReaderJSON.readSeriesFromJSON(path);
    }

    public static void main(String[] args) throws IOException {


    }

}
