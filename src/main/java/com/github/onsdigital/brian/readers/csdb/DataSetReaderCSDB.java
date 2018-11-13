package com.github.onsdigital.brian.readers.csdb;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import com.github.onsdigital.brian.readers.DataSetReader;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.onsdigital.brian.logging.Logger.logEvent;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_TYPE_INT_PARSE_ERROR;

/**
 * Created by thomasridd on 10/03/15.
 * <p>
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 */
public class DataSetReaderCSDB implements DataSetReader {

    private static final Pattern p = Pattern.compile("^[0-9]{2}$");
    private static final String CSDB_FILE_CHAR_SET = "cp1252";
    private static final int TYPE_92 = 92;
    private static final int TYPE_93 = 93;
    private static final int TYPE_96 = 96;
    private static final int TYPE_97 = 97;

    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = null;

        try (
                InputStream fis = Files.newInputStream(filePath);
                InputStream decryptedIS = decryptIfNecessary(fis, key);
                InputStreamReader isReader = new InputStreamReader(decryptedIS, CSDB_FILE_CHAR_SET);
                BufferedReader bufR = new BufferedReader(isReader);
        ) {
            logEvent().path(filePath).info("generating Time series dataset from CSDB file");

            //timeSeriesDataSet = parseCSDBFile(bufR, filePath);
            timeSeriesDataSet = parseCSDBFileNEW(bufR);

            logEvent().path(filePath).info("time series dataset successfully generated from CSDB file");
            return timeSeriesDataSet;

        } catch (Exception e) {
            logEvent(e).path(filePath).error("error while attempting to parse CSDB file");
            throw e;
        }
    }


    private TimeSeriesDataSet parseCSDBFileNEW(BufferedReader reader) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        DataBlockParser dataBlockParser = new DataBlockParser();

        int index = 1; // lines in a file are indexed from 1...
        String line = null;

        while ((line = reader.readLine()) != null) {
            boolean blockCompleted = dataBlockParser.parseLine(line, index);
            if (blockCompleted) {
                dataBlockParser.complete(timeSeriesDataSet);

                dataBlockParser = new DataBlockParser();
                dataBlockParser.parseLine(line, index);
            }

            index++;
        }
        // flush the last entry
        dataBlockParser.complete(timeSeriesDataSet);

        return timeSeriesDataSet;
    }


    private TimeSeriesDataSet parseCSDBFile(BufferedReader reader, Path filePath) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        List<String> seriesBuffer = new ArrayList<>();

        int index = 1; // lines in a file are indexed from 1...
        String line = null;

        while ((line = reader.readLine()) != null) {
            int lineType = getLineType(line, index);

            if (lineType < TYPE_92) {
                index++;
                continue;
            }

            // If its type 92 and the buffere isn't empty - its the end of the a data block - so we proccess it.
            if (lineType == TYPE_92 && !seriesBuffer.isEmpty()) {
                processDataBlock(timeSeriesDataSet, seriesBuffer, index);
                seriesBuffer.clear();
            }

            // if line type is greater than 92 its part of the data block so we add it to the buffer.
            seriesBuffer.add(line);
            index++;
        }

        // flush the buffer - process the file data block.
        if (!seriesBuffer.isEmpty()) {
            processDataBlock(timeSeriesDataSet, seriesBuffer, index);
            seriesBuffer.clear();
        }

        return timeSeriesDataSet;
    }

    private void processDataBlock(TimeSeriesDataSet timeSeriesDataSet, List<String> seriesBuffer, int lineIndex)
            throws IOException {
        // PARSE THE BLOCK JUST COLLECTED
        TimeSeriesObject series = seriesFromStringList2(seriesBuffer, lineIndex);

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
     * CONVERTS A SERIES OF STRINGS READ FROM A CSDB FILE BY THE READFILE METHODS TO A SERIES
     *
     * @param lines
     * @return
     */
    private TimeSeriesObject seriesFromStringList2(List<String> lines, int blockStartIndex) throws IOException {
        TimeSeriesObject series = new TimeSeriesObject();
        int lineIndex = blockStartIndex;
        DateLabel dateLabel = null;

        for (String line : lines) {
            switch (getLineType(line, lineIndex)) {
                case TYPE_92:
                    series.taxi = processLineType92(line, lineIndex);
                    break;
                case TYPE_93:
                    series.name = processLineType93(line, lineIndex);
                    break;
                case TYPE_96:
                    dateLabel = processLineType96(line, lineIndex);
                    break;
                case TYPE_97:
                    List<TimeSeriesPoint> points = processLineType97(line, dateLabel, lineIndex);
                    points.stream().forEach(p -> series.addPoint(p));
                    break;
            }
            lineIndex++;
        }
        return series;
    }

    /**
     * Determined the line type.
     */
    int getLineType(String line, int lineIndex) throws IOException {
        if (StringUtils.isBlank(line)) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, lineIndex);
        }

        if (line.length() < 2) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, line.length(), lineIndex);
        }

        String lineTypeStr = line.substring(0, 2);
        if (lineTypeStr.contains(" ")) {
            logEvent().warn("line containing space");
        }
        lineTypeStr = lineTypeStr.trim();

        try {
            return Integer.parseInt(lineTypeStr);
        } catch (NumberFormatException e) {
            throw new CSDBLineParseException(LINE_TYPE_INT_PARSE_ERROR, lineTypeStr, lineIndex);
        }
    }

    String processLineType92(String line, int lineIndex) throws IOException {
        if (line.length() < 6) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_92, 6, line.length(), lineIndex);
        }
        return line.substring(2, 6);
    }

    String processLineType93(String line, int lineIndex) throws IOException {
        if (line.length() < 2) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_93, 2, line.length(), lineIndex);
        }
        return line.substring(2);
    }

    DateLabel processLineType96(String line, int lineIndex) throws IOException {
        if (line.length() < 11) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_96, 11, line.length(), lineIndex);
        }

        String mqa = line.substring(2, 3);
        int year = Integer.parseInt(line.substring(4, 8));
        int startInd = Integer.parseInt(line.substring(9, 11).trim());

        return new DateLabel(year, startInd, mqa);
    }

    List<TimeSeriesPoint> processLineType97(String line, DateLabel dateLabel, int lineIndex)
            throws IOException {
        if (line.length() < 9) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_97, 9, line.length(), lineIndex);
        }

        List<TimeSeriesPoint> points = new ArrayList<>();
        String values = line.substring(2);
        while (values.length() > 9) {
            String oneValue = values.substring(0, 10).trim();

            points.add(new TimeSeriesPoint(dateLabel.getNextIteration(), oneValue));
            values = values.substring(10);
        }
        return points;
    }
}
