package com.github.onsdigital.readers;

import com.github.onsdigital.async.Processor;
import com.github.onsdigital.data.DataFuture;
import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.data.TimeSeriesObject;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import org.apache.commons.io.FileUtils;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 10/03/15.
 *
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 *
 */
public class DataSetReaderCSDB {

    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public static TimeSeriesDataSet readFile(Path filePath) throws IOException {

        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();

        try {
            // USE WINDOWS ENCODING TO READ THE FILE BECAUSE IT IS A WIN CSDB
            List<String> lines = FileUtils.readLines(filePath.toFile(), "cp1252");
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
                            TimeSeriesObject series = DataSetReaderCSDB.seriesFromStringList(seriesBuffer);

                            // COMBINE IT WITH AN EXISTING SERIES
                            if (timeSeriesDataSet.timeSeries.containsKey(series.taxi)) {
                                TimeSeriesObject existing = timeSeriesDataSet.timeSeries.get(series.taxi);
                                for (TimeSeriesPoint point : series.points.values()) {
                                    existing.addPoint(point);
                                }

                            } else { // OR CREATE A NEW SERIES
                                timeSeriesDataSet.addSeries(series);
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

        return timeSeriesDataSet;
    }




    /**
     * READS A DATASET FROM A RESOURCE FILE -
     *
     * @param resourceName - THE INTERNAL FILE PATH OF THE RESOURCE
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public static TimeSeriesDataSet readFile(String resourceName) throws IOException, URISyntaxException {
        // FIRST THINGS FIRST - GET THE FILE
        URL resource = TimeSeriesDataSet.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        return readFile(filePath);
    }




    /**
     * CREATES A SUPER DATASET FROM ALL FILES IN A FOLDER
     *
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static DataFuture readDirectory(String resourceName) throws IOException, URISyntaxException {



        // Get the path
        URL resource = DataFuture.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        // Processors exist to walk through each file
        List<Processor> processors = new ArrayList<>();

        // Begins by generating an outline of the series we are expecting from each dataset
        List<Future<DataFuture>> skeletonDatasets = new ArrayList<>();


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath)){ // NOW LOAD THE FILES
            for(Path entry: stream) {
                // System.out.println("Creating processor for " + entry);
                Processor processor = new Processor(entry);

                // Process each dataset to get the skeleton
                skeletonDatasets.add(processor.getSkeleton());

                // Add the processor our list of jobs to be completed
                processors.add(processor);
            }
        }

        // Now build a super dataset from all the sub-sets
        DataFuture dataFuture = new DataFuture();
        for (Future<DataFuture> skeletonDataset : skeletonDatasets) {
            try {
                //System.out.println("Getting ID map..");
                DataFuture promised = skeletonDataset.get();
                for(String key: promised.timeSeries.keySet())
                    dataFuture.addSeries(key, promised.timeSeries.get(key));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Now set our processors running to process all the timeseries in the DataFuture
        for (Processor processor: processors) {
            //System.out.println("Processing ");
            processor.process();
        }

        return dataFuture;
    }




    /**
     *  CONVERTS A SERIES OF STRINGS READ FROM A CSDB FILE BY THE READFILE METHODS TO A SERIES
     *
     * @param lines
     * @return
     */
    private static TimeSeriesObject seriesFromStringList(ArrayList<String> lines) {
        TimeSeriesObject series = new TimeSeriesObject();
        int startInd = 1;
        int year = 1881;
        String mqa = "A";
        int iteration = 1;

        for (String line : lines) {
            try {
                int LineType = Integer.parseInt(line.substring(0, 2));
                if (LineType == 92) { // TOP LINE (SERIES CODE)
                    series.taxi = line.substring(2, 6);

                    String seasonallyAdjusted = line.substring(7,8);
                    if (seasonallyAdjusted.equalsIgnoreCase("U")) {
                        series.seasonallyAdjusted = false;
                    } else {
                        series.seasonallyAdjusted = true;
                    }

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




    /**
     * HELPER METHOD THAT DETERMINES THE CORRECT LABEL BASED ON START DATE, A TIME PERIOD, AND THE ITERATION
     *
     * @param year
     * @param startInd
     * @param mqa
     * @param iteration
     * @return
     */
    private static String DateLabel(int year, int startInd, String mqa, int iteration) {
        if (mqa.equals("Y") || mqa.equals("A")) {
            return (year + iteration - 1) + "";
        } else if(mqa.equals("M")) {
            int finalMonth = (startInd + iteration - 2) % 12;
            int yearsTaken = iteration / 12;
            return (year + yearsTaken) + " " + String.format("%02d", finalMonth + 1);
        } else {
            int finalQuarter = (startInd + iteration - 2) % 4;
            int yearsTaken = iteration / 4;
            return String.format("%d Q%d", year + yearsTaken, finalQuarter + 1);
        }

    }


    public static void main(String[] args) throws IOException, URISyntaxException {


        DataFuture dataFuture = DataSetReaderCSDB.readDirectory("/imports/csdb/");

        // Shut down the pools:
        Processor.shutdown();

        int j = 0;
        for(Future<TimeSeriesObject> timeSeriesFuture : dataFuture.timeSeries.values()) {
            try {
                System.out.println(++j + " " + timeSeriesFuture.get().taxi);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }



}
