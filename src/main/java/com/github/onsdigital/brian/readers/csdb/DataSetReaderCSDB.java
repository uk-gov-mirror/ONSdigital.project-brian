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
import static java.lang.String.format;

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

            timeSeriesDataSet = parseCSDBFile(bufR, filePath);

            logEvent().path(filePath).info("time series dataset successfully generated from CSDB file");
            return timeSeriesDataSet;

        } catch (Exception e) {
            logEvent(e).path(filePath).error("error while attempting to parse CSDB file");
            throw e;
        }
    }

    private TimeSeriesDataSet parseCSDBFile(BufferedReader reader, Path filePath) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        List<String> seriesBuffer = new ArrayList<>();

        int index = 1; // lines in a file are indexed from 1...
        String line = null;

        while ((line = reader.readLine()) != null) {
            int lineType = getLineType(line, index);

            if (lineType < TYPE_92) {
                logEvent().path(filePath).index(index).debug("skipping line as line type < 92");
                index++;
                continue;
            }

            // If its type 92 and the buffere isn't empty - its the end of the a data block - so we proccess it.
            if (lineType == TYPE_92 && !seriesBuffer.isEmpty()) {
                logEvent().path(filePath).index(index).info("processing time series data block");
                processDataBlock(timeSeriesDataSet, seriesBuffer, index);
                seriesBuffer.clear();
            }

            // if line type is greater than 92 its part of the data block so we add it to the buffer.
            seriesBuffer.add(line);
            index++;
        }

        // flush the buffer - process the file data block.
        if (!seriesBuffer.isEmpty()) {
            logEvent().path(filePath).index(index).info("processing time series data block");
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
        int lineType = 0;
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
                    processLineType97(series, line, dateLabel, lineIndex);
                    break;
            }
            lineIndex++;
        }
        return series;
    }

    /**
     * Determined the line type.
     */
    private int getLineType(String line, int lineIndex) throws IOException {
        if (StringUtils.isBlank(line) || StringUtils.isBlank(line)) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, line.length(), lineIndex);
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

    private String processLineType92(String line, int lineIndex) throws IOException {
        if (line.length() < 6) {
            throw new CSDBLineParseException(format(LINE_LENGTH_ERROR, TYPE_92, 6, line.length(), lineIndex));
        }
        return line.substring(2, 6);
    }

    private String processLineType93(String line, int lineIndex) throws IOException {
        if (line.length() < 2) {
            throw new CSDBLineParseException(format(LINE_LENGTH_ERROR, TYPE_93, 2, line.length(), lineIndex));
        }
        return line.substring(2);
    }

    private DateLabel processLineType96(String line, int lineIndex) throws IOException {
        if (line.length() < 11) {
            throw new CSDBLineParseException(format(LINE_LENGTH_ERROR, TYPE_96, 11, line.length(), lineIndex));
        }

        int startInd = Integer.parseInt(line.substring(9, 11).trim());
        String mqa = line.substring(2, 3);
        int year = Integer.parseInt(line.substring(4, 8));

        return new DateLabel(year, startInd, mqa);
    }

    private void processLineType97(TimeSeriesObject series, String line, DateLabel dateLabel, int lineIndex)
            throws IOException {
        if (line.length() < 9) {
            throw new CSDBLineParseException(format(LINE_LENGTH_ERROR, TYPE_97, 9, line.length(), lineIndex));
        }

        String values = line.substring(2);
        while (values.length() > 9) {
            String oneValue = values.substring(0, 10).trim();

            TimeSeriesPoint point = new TimeSeriesPoint(dateLabel.getNextIteration(), oneValue);
            series.addPoint(point);

            values = values.substring(10);
        }
    }
}
