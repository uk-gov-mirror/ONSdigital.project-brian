package com.github.onsdigital.data;

import com.github.onsdigital.readers.Csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Tom.Ridd on 03/03/15.
 * <p/>
 * THIS VERSION OF THE ONS DATA MODEL STRUCTURE IS BUILT TO
 * AID THE CONVERSION PROCESS
 * THE CASCADE DataSet->TimeSeriesTable->TimeSeries->TimeSeriesPoint REFLECTS THE WORKSHEET BASED READ IN
 * REQUIREMENTS FOR OUR DATA
 * <p/>
 * DATASET IS WORKBOOK LEVEL
 */
public class DataSet {
    public String name;
    public String source;

    public ArrayList<TimeSeries> timeSeries = new ArrayList<>();
    public ArrayList<TimeSeriesTable> timeTables = new ArrayList<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        Csv csv = new Csv("/imports/Construction 1.csv");
        csv.read(0);

        for (Map<String, String> row : csv) {
            for (String key : row.keySet()) {
                System.out.println(row.get(key));
            }
        }
    }


}
