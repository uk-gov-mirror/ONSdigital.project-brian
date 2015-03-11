package com.github.onsdigital.readers;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.Line;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomasridd on 10/03/15.
 */
public class TimeSeriesReaderCSDB {

    // CHEWS THROUGH THE CSDB BRIDGE FILES
    public static DataSet readFile(String resourceName) throws IOException {

        HashMap<String, TimeSeries> serieses = new HashMap<>();

        // FIRST THINGS FIRST - GET THE FILE
        URL resource = DataSet.class.getResource(resourceName);
        try {
            Path filePath = Paths.get(resource.toURI());
            List<String> lines = FileUtils.readLines(filePath.toFile());
            lines.add("92"); // THROW A 92 ON THE END

            ArrayList<String> seriesBuffer = new ArrayList<>();

            //WALK THROUGH THE FILE
            for(String line : lines) {
                try {
                    int LineType = Integer.parseInt(line.substring(0, 2));

                    // WHEN WE GET TO A LINE 92 (TIME SERIES BLOCK START)
                    if (LineType == 92) {
                        if (seriesBuffer.size() > 0) {
                            // PARSE THE BLOCK JUST COLLECTED
                            TimeSeries series = TimeSeriesReaderCSDB.seriesFromStringList(seriesBuffer);

                            // COMBINE IT WITH AN EXISTING SERIES
                            if(serieses.containsKey(series.taxi)) {
                                TimeSeries existing = serieses.get(series.taxi);
                                for(TimeSeriesPoint point : series.points.values()) {
                                    existing.addPoint(point);
                                }

                            } else { // OR CREATE A NEW SERIES
                                serieses.put(series.taxi, series);
                            }
                        }
                        seriesBuffer = new ArrayList<>();
                        seriesBuffer.add(line);
                    } else if (LineType > 92) {
                        seriesBuffer.add(line);
                    }
                } catch (NumberFormatException e) {

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        DataSet data = new DataSet();
        data.timeSeries = serieses;

        return data;
    }

    public static TimeSeries seriesFromStringList(ArrayList<String> lines) {
        TimeSeries series = new TimeSeries();
        int startInd = 1;
        int year = 1881;
        String mqa = "A";
        int iteration = 1;

        for(String line: lines) {
            try {
                int LineType = Integer.parseInt(line.substring(0, 2));
                if(LineType == 92) { // TOP LINE (SERIES CODE)
                    series.taxi = line.substring(2,6);

                } else if (LineType == 93) { // SECOND LINE (DESCRIPTION)
                    series.name = line.substring(2);

                } else if (LineType == 96) { // THIRD LINE (START DATE)
                    startInd = Integer.parseInt(line.substring(9,11).trim());
                    mqa = line.substring(2,3);
                    year = Integer.parseInt(line.substring(4, 8));

                } else if (LineType == 97) { // OTHER LINES (APPEND IN BLOCKS)
                    String values = line.substring(2);
                    while(values.length() > 9) {
                        // GET FIRST VALUE
                        String oneValue = values.substring(0, 10).trim();
                        try {
                            TimeSeriesPoint point = new TimeSeriesPoint(DateLabel(year, startInd, mqa, iteration), oneValue);
                            series.addPoint(point);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // TRIM OFF THE BEGINNING
                        values = values.substring(10);
                        iteration += 1;
                    };
                }
            } catch (NumberFormatException e) {

            }
        }

        return series;
    }

    private static String DateLabel(int year, int startInd, String mqa, int iteration) {
        if(mqa.equals("Y") || mqa.equals("A")) {return (year + iteration - 1) + "";}

        // NO GREAT BIT OF CALCULATION HERE
        int yr = year;
        int it = startInd;
        for(int i = 1; i < iteration; i++) {
            it += 1;
            if(mqa.equals("M") & it > 12) {
                it = 1;
                yr += 1;
            } else if (mqa.equals("Q") & it > 4) {
                it = 1;
                yr += 1;
            }
        }

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        if(mqa.equals("M")) {
            return yr + " " + months[it - 1];
        } else {
            return yr + " Q" + it;
        }
    }

    public static void main(String[] args) throws IOException {
        DataSet csvSet = TimeSeriesReaderCSV.readFile("/imports/IOS1.csv");
        DataSet csdbSet = TimeSeriesReaderCSDB.readFile("/imports/IOS1");

        for(TimeSeries t: csvSet.timeSeries.values()) {
            try {
                TimeSeries t2 = csdbSet.timeSeries.get(t.taxi);
                System.out.println(t.taxi + " csv:" + t.points.size() + " points    csdb:" + t2.points.size());
            } catch (Exception e) {
                System.out.println("Problem with " + t.taxi);
            }
        }
    }


}
