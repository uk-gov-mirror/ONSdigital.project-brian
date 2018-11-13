package com.github.onsdigital.brian.readers.csdb;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import static com.github.onsdigital.brian.logging.Logger.logEvent;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_TYPE_INT_PARSE_ERROR;

public class DataBlockParser {

    private static final int TYPE_92 = 92;
    private static final int TYPE_93 = 93;
    private static final int TYPE_96 = 96;
    private static final int TYPE_97 = 97;

    private int startIndex = 0;
    private List<String> entries;
    private DateLabel dateLabel;
    private TimeSeriesObject series;
    private boolean isOpen;

    public DataBlockParser() {
        this.series = new TimeSeriesObject();
        this.isOpen = false;
    }

    /**
     * Parse a line of a CSDB file. If the line type is 92 (indicating the start of a block of data) and this
     * block is already open then the line is not processed and true is returned to indicate that the end of the time
     * series block has been reached. Otherwise the line is processed and returns false (the block is not yet
     * compelete).
     */
    public boolean parseLine(String line, int index) throws IOException {
        int lineType = getLineType(line, index);

        switch (lineType) {
            case TYPE_92:
                return parseLineType92(line, index);
            case TYPE_93:
                parseLineType93(line, index);
                return false;
            case TYPE_96:
                parseLineType96(line, index);
                return false;
            case TYPE_97:
                parseLineType97(line, index);
                return false;
            default:
                // we are not interested in this line so do nothing with it.
                return false;
        }
    }


    public void complete(TimeSeriesDataSet timeSeriesDataSet)
            throws IOException {
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

    private int getLineType(String line, int lineIndex) throws IOException {
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

    private boolean parseLineType92(String line, int startIndex) throws IOException {
        if (isOpen) {
            return true;
        }

        if (line.length() < 6) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_92, 6, line.length(), startIndex);
        }

        this.series.taxi = line.substring(2, 6);
        this.isOpen = true;
        return false;
    }

    private void parseLineType93(String line, int lineIndex) throws IOException {
        if (line.length() < 2) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_93, 2, line.length(), lineIndex);
        }
        this.series.name = line.substring(2);
    }


    private void parseLineType96(String line, int lineIndex) throws IOException {
        if (line.length() < 11) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_96, 11, line.length(), lineIndex);
        }

        String mqa = line.substring(2, 3);
        int year = Integer.parseInt(line.substring(4, 8));
        int startInd = Integer.parseInt(line.substring(9, 11).trim());

        this.dateLabel = new DateLabel(year, startInd, mqa);
    }

    private void parseLineType97(String line, int lineIndex)
            throws IOException {
        if (line.length() < 9) {
            throw new CSDBLineParseException(LINE_LENGTH_ERROR, TYPE_97, 9, line.length(), lineIndex);
        }
        String values = line.substring(2);
        while (values.length() > 9) {
            String oneValue = values.substring(0, 10).trim();

            this.series.addPoint(new TimeSeriesPoint(dateLabel.getNextIteration(), oneValue));
            values = values.substring(10);
        }
    }
}
