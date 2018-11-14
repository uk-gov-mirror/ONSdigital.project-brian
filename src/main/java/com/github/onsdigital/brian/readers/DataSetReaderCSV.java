package com.github.onsdigital.brian.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.github.davidcarboni.cryptolite.Crypto;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * Created by thomasridd on 10/03/15.
 * <p>
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 */
public class DataSetReaderCSV implements DataSetReader {

    static final DateFormat DF_YY = new SimpleDateFormat("yy");
    static final DateFormat DF_YYYY = new SimpleDateFormat("yyyy");
    static final DateFormat DF_YYYY_MMM = new SimpleDateFormat("yyyy MMM");
    static final DateFormat DF_YYYY_MM = new SimpleDateFormat("yyyy MM");
    static final DateFormat DF_MMM_YY = new SimpleDateFormat("MMM yy");

    static final String YYYY_DASH = "yyyy-";
    static final String YYYY_SLASH = "yyyy/";


    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException {

        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();

        try (InputStream initialStream = Files.newInputStream(filePath)) {
            try (CSVReader reader = new CSVReader(new InputStreamReader(decryptIfNecessary(initialStream, key)))) {

                // Read the file as a list of rows
                List<String[]> rows = reader.readAll();

                // Convert it to a map keyed by row name
                Map<String, String[]> map = rowsAsMap(rows);

                // Get a list of cdids
                Map<String, Integer> cdidMap = cdidMap(map);
                Map<String, TimeSeriesObject> timeSeriesMap = constructTimeSeriesMap(cdidMap);

                // Set metadata
                setMetaData(cdidMap, map, timeSeriesMap);

                // Get values
                getValues(cdidMap, map, timeSeriesMap);

                // Return values to the timeSeries list
                cdidMap.keySet().forEach(cdid -> timeSeriesDataSet.addSeries(timeSeriesMap.get(cdid)));
            }
        } catch (Exception e) {
            // TODO feels like this should throw an exception instead of just doing nothing...
            logEvent(e).path(filePath).error("error while reading CSV file");
        }

        return timeSeriesDataSet;
    }

    /**
     * extract a map of cdid to column number we can use to interpret the rest of the table
     *
     * @param dataMap the map of data as pulled out of file
     * @return
     */
    private Map<String, Integer> cdidMap(Map<String, String[]> dataMap) {
        String[] cdidRow;
        if (dataMap.containsKey("id")) {
            cdidRow = dataMap.get("id");
        } else if (dataMap.containsKey("cdid")) {
            cdidRow = dataMap.get("cdid");
        } else {
            return null;
        }

        Map<String, Integer> map = new HashMap<>();
        for (int i = 1; i < cdidRow.length; i++) {
            map.put(cdidRow[i], Integer.valueOf(i));
        }

        return map;
    }


    /**
     * Set timeseries metadata that can be taken from these spreadsheets
     *
     * @param cdidMap         cdids mapped to row number
     * @param dataMap         the data as read from a spreadsheet (column names unknown)
     * @param seriesObjectMap the target series keyed into a map by cdid
     */
    private void setMetaData(Map<String, Integer> cdidMap, Map<String, String[]> dataMap, Map<String, TimeSeriesObject> seriesObjectMap) {
        if (dataMap.containsKey("id"))
            cdidMap.keySet().forEach(cdid -> seriesObjectMap.get(cdid).taxi = dataMap.get("id")[cdidMap.get(cdid)]);
        if (dataMap.containsKey("cdid"))
            cdidMap.keySet().forEach(cdid -> seriesObjectMap.get(cdid).taxi = dataMap.get("cdid")[cdidMap.get(cdid)]);
        if (dataMap.containsKey("name"))
            cdidMap.keySet().forEach(cdid -> seriesObjectMap.get(cdid).name = dataMap.get("name")[cdidMap.get(cdid)]);
        if (dataMap.containsKey("title"))
            cdidMap.keySet().forEach(cdid -> seriesObjectMap.get(cdid).name = dataMap.get("title")[cdidMap.get(cdid)]);
    }

    private void getValues(Map<String, Integer> cdidMap, Map<String, String[]> dataMap, Map<String, TimeSeriesObject> seriesObjectMap) {

        // For each row
        for (String row : dataMap.keySet()) {

            // If it is a date row
            if (parseDate(row) != null) {

                // For each timeseries
                for (String cdid : cdidMap.keySet()) {

                    // Grab the value
                    String value = dataMap.get(row)[cdidMap.get(cdid)];
                    if (value.trim().length() != 0) {

                        TimeSeriesObject timeSeriesObject = seriesObjectMap.get(cdid);
                        timeSeriesObject.addPoint(new TimeSeriesPoint(row.trim(), value));
                    }
                }
            }
        }
    }

    public static Date parseDate(String date) {
        try {
            String e = StringUtils.lowerCase(StringUtils.trim(date));
            Date result;
            if (TimeSeries.year.matcher(e).matches()) {
                result = DF_YYYY.parse(e);
            } else if (TimeSeries.month.matcher(e).matches()) {
                result = DF_YYYY_MMM.parse(e);
            } else if (TimeSeries.monthNumVal.matcher(e).matches()) {
                result = DF_YYYY_MM.parse(e);
            } else if (TimeSeries.quarter.matcher(e).matches()) {
                Date parsed = DF_YYYY.parse(e);
                Calendar calendar = Calendar.getInstance(Locale.UK);
                calendar.setTime(parsed);
                if (e.endsWith("1")) {
                    calendar.set(2, 0);
                } else if (e.endsWith("2")) {
                    calendar.set(2, 3);
                } else if (e.endsWith("3")) {
                    calendar.set(2, 6);
                } else {
                    if (!e.endsWith("4")) {
                        throw new RuntimeException("Didn\'t detect quarter in " + e);
                    }

                    calendar.set(2, 9);
                }

                result = calendar.getTime();
            } else if (TimeSeries.yearInterval.matcher(e).matches()) {
                result = DF_YYYY.parse(e.substring(YYYY_DASH.length()));
            } else if (TimeSeries.yearPair.matcher(e).matches()) {
                result = DF_YY.parse(e.substring(YYYY_SLASH.length()));
            } else {
                if (!TimeSeries.yearEnd.matcher(e).matches()) {
                    throw new ParseException("Unknown format: \'" + date + "\'", 0);
                }
                result = DF_MMM_YY.parse(e.substring("YE ".length()));
            }

            return result;
        } catch (ParseException var5) {
            return null;
        }
    }

    private Map<String, TimeSeriesObject> constructTimeSeriesMap(Map<String, Integer> cdidMap) {
        Map<String, TimeSeriesObject> timeSeries = new HashMap<>();

        // Create a new timeseries for each cdid
        cdidMap.keySet().forEach(cdid -> timeSeries.put(cdid, new TimeSeriesObject()));

        return timeSeries;
    }


    /**
     * get the row list as a map
     *
     * @param rows
     * @return
     */
    private Map<String, String[]> rowsAsMap(List<String[]> rows) {
        Map<String, String[]> rowMap = new HashMap<>();
        rows.forEach(row -> rowMap.put(row[0].toLowerCase(), row));
        return rowMap;
    }

    private InputStream decryptIfNecessary(InputStream stream, SecretKey key) throws IOException {
        if (key == null) {
            return stream;
        } else {
            return new Crypto().decrypt(stream, key);
        }
    }
}
