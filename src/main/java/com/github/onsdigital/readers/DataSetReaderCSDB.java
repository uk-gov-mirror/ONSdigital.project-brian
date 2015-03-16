package com.github.onsdigital.readers;

import com.github.davidcarboni.ResourceUtils;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomasridd on 10/03/15.
 *
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 *
 */
public class DataSetReaderCSDB {

    /**
     * READS A DATASET FROM A RESOURCE FILE
     *
     * @param filePath - THE INTERNAL FILE PATH OF THE RESOURCE
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public static DataSet readFile(Path filePath) throws IOException {

        DataSet dataSet = new DataSet();

        try {

            List<String> lines = FileUtils.readLines(filePath.toFile());
            lines.add("92"); // THROW A 92 ON THE END

            ArrayList<String> seriesBuffer = new ArrayList<>();

            //WALK THROUGH THE FILE
            for (String line : lines) {
                try {
                    int LineType = Integer.parseInt(line.substring(0, 2));

                    // WHEN WE GET TO A LINE 92 (TIME SERIES BLOCK START)
                    if (LineType == 92) {
                        if (seriesBuffer.size() > 0) {
                            // PARSE THE BLOCK JUST COLLECTED
                            TimeSeries series = DataSetReaderCSDB.seriesFromStringList(seriesBuffer);

                            // COMBINE IT WITH AN EXISTING SERIES
                            if (dataSet.timeSeries.containsKey(series.taxi)) {
                                TimeSeries existing = dataSet.timeSeries.get(series.taxi);
                                for (TimeSeriesPoint point : series.points.values()) {
                                    existing.addPoint(point);
                                }

                            } else { // OR CREATE A NEW SERIES
                                dataSet.addSeries(series);
                            }
                        }
                        seriesBuffer = new ArrayList<>();
                        seriesBuffer.add(line);
                    } else if (LineType > 92) {
                        seriesBuffer.add(line);
                    }
                } catch (NumberFormatException e) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataSet;
    }

    /**
     * READS A DATASET FROM A RESOURCE FILE
     *
     * @param resourceName - THE INTERNAL FILE PATH OF THE RESOURCE
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public static DataSet readFile(String resourceName) throws IOException {
        // FIRST THINGS FIRST - GET THE FILE
        Path filePath = ResourceUtils.getPath(resourceName);
        return readFile(filePath);
    }


    /**
     * CREATES A SUPER DATASET FROM ALL FILES IN A FOLDER
     *
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static DataSet readDirectory(String resourceName) throws IOException {

        DataSet dataSet = new DataSet();

        // FIRST THINGS FIRST - GET THE PATH
        Path filePath = ResourceUtils.getPath(resourceName);
        long startTime = System.nanoTime();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath)){
            for(Path entry: stream) {

                dataSet = dataSet.mergeWith(DataSetReaderCSDB.readFile(entry), true);
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 10000000;
                System.out.println("Time " + entry.getFileName() + ": " + duration + "ms");
            }
        }

        return dataSet;
    }

    private static TimeSeries seriesFromStringList(ArrayList<String> lines) {
        TimeSeries series = new TimeSeries();
        int startInd = 1;
        int year = 1881;
        String mqa = "A";
        int iteration = 1;

        for (String line : lines) {
            try {
                int LineType = Integer.parseInt(line.substring(0, 2));
                if (LineType == 92) { // TOP LINE (SERIES CODE)
                    series.taxi = line.substring(2, 6);

                } else if (LineType == 93) { // SECOND LINE (DESCRIPTION)
                    series.name = line.substring(2);

                } else if (LineType == 96) { // THIRD LINE (START DATE)
                    startInd = Integer.parseInt(line.substring(9, 11).trim());
                    mqa = line.substring(2, 3);
                    year = Integer.parseInt(line.substring(4, 8));

                } else if (LineType == 97) { // OTHER LINES (APPEND IN BLOCKS)
                    String values = line.substring(2);
                    while (values.length() > 9) {
                        // GET FIRST VALUE
                        String oneValue = values.substring(0, 10).trim();

                        TimeSeriesPoint point = new TimeSeriesPoint(DateLabel(year, startInd, mqa, iteration), oneValue);
                        series.addPoint(point);

                        // TRIM OFF THE BEGINNING
                        values = values.substring(10);
                        iteration += 1;
                    }
                }
            } catch (NumberFormatException e) {

            }
        }

        return series;
    }

    private static String DateLabel(int year, int startInd, String mqa, int iteration) {
        if (mqa.equals("Y") || mqa.equals("A")) {
            return (year + iteration - 1) + "";
        }

        // NO GREAT BIT OF CALCULATION HERE
        int yr = year;
        int it = startInd;
        for (int i = 1; i < iteration; i++) {
            it += 1;
            if (mqa.equals("M") & it > 12) {
                it = 1;
                yr += 1;
            } else if (mqa.equals("Q") & it > 4) {
                it = 1;
                yr += 1;
            }
        }

        if (mqa.equals("M")) {
//            return yr + " " + months[it - 1];
            return yr + " " + String.format("%02d", it);
        } else {
            return yr + " Q" + it;
        }
    }

    public static void main(String[] args) throws IOException {

        DataSet dataSet = DataSetReaderCSDB.readDirectory("/imports/csdb");

    }


}
