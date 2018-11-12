package com.github.onsdigital.brian.readers;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.onsdigital.brian.async.Processor;
import com.github.onsdigital.brian.data.DataFuture;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

/**
 * Created by thomasridd on 10/03/15.
 * <p>
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 */
public class DataSetReaderCSDB implements DataSetReader {

    private static final Pattern p = Pattern.compile("^[0-9]{2}$");

    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public TimeSeriesDataSet readFile2(Path filePath, SecretKey key) throws IOException {
        logEvent().path(filePath).info("reading CSDB file");

        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();

        // NESTED TRY WITH RESOURCES TO GET A NOT NECESSARY ENCRYPTED FILE STREAM
        try (InputStream initialStream = Files.newInputStream(filePath)) {
            try (InputStream inputStream = decryptIfNecessary(initialStream, key)) {

                List<String> lines = IOUtils.readLines(inputStream, "cp1252");
                lines.add("92"); // THROW A 92 ON THE END

                ArrayList<String> seriesBuffer = new ArrayList<>();

                //WALK THROUGH THE FILE
                int index = 0;
                for (String line : lines) {

                    if (StringUtils.isBlank(line)) {
                        logEvent().warn("skipping blank line");
                        continue;
                    }

                    String lineTypeStr = StringUtils.trim(line.substring(0, 2));
                    int LineType = 0;

                    try {
                        LineType = Integer.parseInt(lineTypeStr);
                    } catch (NumberFormatException e) {
                        logEvent(e).path(filePath).error("failed to parse value to integer");
                        throw e;
                    }

                    // WHEN WE GET TO A LINE 92 (TIME SERIES BLOCK START)
                    if (LineType == 92) {
                        logEvent().parameter("index", index).info("processing line type 92");
                        if (seriesBuffer.size() > 0) {
                            // PARSE THE BLOCK JUST COLLECTED
                            TimeSeriesObject series = seriesFromStringList(seriesBuffer);

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
                        logEvent().parameter("index", index).info("processing line type other");
                        seriesBuffer.add(line);
                    }
                }
            }
        } catch (Exception e) {
            logEvent(e).path(filePath).error("error while attempting to read CSDB file");
        }

        logEvent().path(filePath).info("read CSDB file completed successfully");
        return timeSeriesDataSet;
    }

    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException {
        logEvent().path(filePath).info("reading CSDB file");

        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        try (
                InputStream initialStream = Files.newInputStream(filePath);
                InputStream inputStream = decryptIfNecessary(initialStream, key);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "cp1252");
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            ArrayList<String> seriesBuffer = new ArrayList<>();
            int index = 0;
            String line = null;

            while ((line = reader.readLine()) != null) {
                int lineType = getLineType(line, index);

                if (lineType < 92) {
                    index++;
                    continue;
                }

                if (lineType == 92 && !seriesBuffer.isEmpty()) {
                    processDataBlock(timeSeriesDataSet, seriesBuffer);
                    seriesBuffer.clear();
                }
                seriesBuffer.add(line);
                index++;
            }

            if (!seriesBuffer.isEmpty()) {
                processDataBlock(timeSeriesDataSet, seriesBuffer);
                seriesBuffer.clear();
            }

        } catch (
                Exception e) {
            logEvent(e).path(filePath).error("error while attempting to read CSDB file");
            throw e;
        }

        logEvent().path(filePath).info("read CSDB file completed successfully");
        return timeSeriesDataSet;
    }

    private void processDataBlock(TimeSeriesDataSet timeSeriesDataSet, ArrayList<String> seriesBuffer) {
        // PARSE THE BLOCK JUST COLLECTED
        TimeSeriesObject series = seriesFromStringList(seriesBuffer);

        // COMBINE IT WITH AN EXISTING SERIES
        if (timeSeriesDataSet.timeSeries.containsKey(series.taxi)) {
            TimeSeriesObject existing = timeSeriesDataSet.timeSeries.get(series.taxi);

            for (TimeSeriesPoint point : series.points.values()) {
                existing.addPoint(point);
            }
            return;

        }
        // OR CREATE A NEW SERIES
        timeSeriesDataSet.addSeries(series);
    }

    private void processLine(TimeSeriesDataSet timeSeriesDataSet, ArrayList<String> seriesBuffer, String line, int index) {
        int lineType = getLineType(line, index);

        if (lineType == 92) {
            if (seriesBuffer.size() > 0) {
                logEvent().parameter("index", index).debug("processing line type 92");

                // PARSE THE BLOCK JUST COLLECTED
                TimeSeriesObject series = seriesFromStringList(seriesBuffer);

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
            //seriesBuffer = new ArrayList<>();
            seriesBuffer.add(line);
        } else if (lineType > 92) {
            logEvent().parameter("index", index).debug("processing line type other");
            seriesBuffer.add(line);
        }
    }


    private InputStream decryptIfNecessary(InputStream stream, SecretKey key) throws IOException {
        if (key == null) {
            logEvent().trace("encryption key null reading file with unencrypted stream");
            return stream;
        } else {
            logEvent().trace("encryption key not null reading file with crypto stream");
            return new Crypto().decrypt(stream, key);
        }
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


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath)) { // NOW LOAD THE FILES
            for (Path entry : stream) {
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
                for (String key : promised.timeSeries.keySet())
                    dataFuture.addSeries(key, promised.timeSeries.get(key));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Now set our processors running to process all the timeseries in the DataFuture
        for (Processor processor : processors) {
            //System.out.println("Processing ");

            logEvent().path(filePath).info("processing");
            processor.process();
        }

        return dataFuture;
    }


    /**
     * CONVERTS A SERIES OF STRINGS READ FROM A CSDB FILE BY THE READFILE METHODS TO A SERIES
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
                logEvent(e).error("failed to parse line type to integer");
            }
        }

        return series;
    }

    private int getLineType(String line, int index) {
        if (StringUtils.isBlank(line)) {
            logEvent().parameter("index", index).error("encountered unexpected blank line while processing CSDB file");
            throw new RuntimeException("encountered unexpected blank line while processing CSDB file");
        }
        if (line.length() < 2) {
            logEvent().parameter("index", index).error("csdb file line less than the expected length");
            throw new RuntimeException("csdb file line less than the expected length");
        }

        String lineTypeStr = line.substring(0, 2);
        if (lineTypeStr.contains(" ")) {
            logEvent().parameter("index", index).warn("line containing space");
        }
        lineTypeStr = lineTypeStr.trim();

        try {
            return Integer.parseInt(lineTypeStr);
        } catch (NumberFormatException e) {
            logEvent(e).parameter("value", lineTypeStr)
                    .parameter("index", index)
                    .error("error attempting to parse CSDB line type to integer");
            throw e;
        }
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
        } else if (mqa.equals("M")) {
            int finalMonth = (startInd + iteration - 2) % 12;
            int yearsTaken = (startInd + iteration - 2) / 12;
            return (year + yearsTaken) + " " + String.format("%02d", finalMonth + 1);
        } else {
            int finalQuarter = (startInd + iteration - 2) % 4;
            int yearsTaken = (startInd + iteration - 2) / 4;
            return String.format("%d Q%d", year + yearsTaken, finalQuarter + 1);
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println(" 1 " + p.matcher(" 1").matches());
        System.out.println("1  " + p.matcher("1 ").matches());
        System.out.println("   " + p.matcher("  ").matches());
        System.out.println("01 " + p.matcher("01").matches());
        System.out.println("22 " + p.matcher("22").matches());
        System.out.println("22 " + p.matcher("22 ").matches());
    }

}
