package com.github.onsdigital.brian.readers.csdb;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_TYPE_INT_PARSE_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.BLOCK_ENDED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.BLOCK_STARTED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.COMPLETED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.NOT_STARTED;

/**
 * Object to parse lines of a CSDB file into {@link TimeSeriesObject}. Each line of a .csdb file is passed to the
 * {@link DataBlockParser#parseLine(String, int)} which will process the data based on the line type returning true
 * if the current "data block" is completed or else returns false.
 * <p/>
 * When {@link DataBlockParser#parseLine(String, int)} returns true the caller should then invoke
 * {@link DataBlockParser#complete(TimeSeriesDataSet)} to retreive the {@link TimeSeriesDataSet} generated from this
 * block - this instance should then be discarded and a new one instatitated for the next block.
 */
public class DataBlockParser {

    /* 92 indicates the start of a data block & the end of the current one. Contains the series taxi value */
    static final int TYPE_92 = 92;

    /* 93 contains the series name */
    static final int TYPE_93 = 93;

    /* 96 contains the series date label data */
    static final int TYPE_96 = 96;

    /* 97 contains the time series point values */
    static final int TYPE_97 = 97;

    private TimeSeriesPointGenerator timeSeriesPointGenerator;
    private DateLabel dateLabel;
    private TimeSeriesObject series;
    private State state;

    /**
     * Construct a new CSDBDataBlockParser.
     */
    public DataBlockParser() {
        this(new TimeSeriesObject(), null, (dateLabel, val) -> new TimeSeriesPoint(dateLabel.getNextIteration(), val));
    }

    DataBlockParser(TimeSeriesObject timeSeriesObject, DateLabel dateLabel, TimeSeriesPointGenerator timeSeriesPointGenerator) {
        this.timeSeriesPointGenerator = timeSeriesPointGenerator;
        this.series = timeSeriesObject;
        this.dateLabel = dateLabel;
        this.state = NOT_STARTED;
    }

    /**
     * Parse a line of a CSDB file. If the line type is 92 (indicating the start of a block of data) and this
     * block is already open then the line is not processed and true is returned to indicate that the end of the time
     * series block has been reached. Otherwise the line is processed and returns false (the block is not yet
     * compelete).
     */
    public boolean parseLine(String line, int index) throws IOException {
        if (this.state == BLOCK_ENDED || this.state == COMPLETED) {
            throw new DataBlockException("complete invoked on data block that has been completed");
        }

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
                // we are not interested in this line type so do nothing with it.
                return false;
        }
    }

    /**
     * Call only once the CSDB data block is compeleted. Will add the parsed time series data to the provided
     * TimeSeriesDataSet.
     */
    public void complete(TimeSeriesDataSet timeSeriesDataSet) throws IOException {
        if (this.state == NOT_STARTED) {
            throw new DataBlockException("complete invoked on data block that has not been started");
        }

        if (this.state == COMPLETED) {
            throw new DataBlockException("complete invoked on data block that has already been competed, " +
                    "series: %s", series.taxi);
        }
        addToTimeSeriesDataSet(timeSeriesDataSet);
        logEvent().parameter("series", series.name)
                .parameter("taxi", series.taxi)
                .info("csdb data block completed");
    }


    /**
     * Process any remaining data that might be held in the parser.
     */
    public void flush(TimeSeriesDataSet timeSeriesDataSet) throws IOException {
        // if there is an open block that isn't complete - invoke complete to flush the remaining data.
        if (this.state == BLOCK_STARTED || this.state == BLOCK_ENDED) {
            logEvent().parameter("series", series.name).parameter("taxi", series.taxi).info("flushing dataBlockParser");
            complete(timeSeriesDataSet);
        }
    }

    /**
     * Return the line type of the CSDB file line.
     */
    public int getLineType(String line, int lineIndex) throws IOException {
        if (StringUtils.isBlank(line)) {
            throw new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, lineIndex);
        }

        if (line.length() < 2) {
            throw new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, line.length(), lineIndex);
        }

        String lineTypeStr = line.substring(0, 2);
        lineTypeStr = lineTypeStr.trim();

        try {
            return Integer.parseInt(lineTypeStr);
        } catch (NumberFormatException e) {
            throw new DataBlockException(LINE_TYPE_INT_PARSE_ERROR, lineTypeStr, lineIndex);
        }
    }

    private void addToTimeSeriesDataSet(TimeSeriesDataSet timeSeriesDataSet) {
        try {
            // combine it with an existing series
            if (timeSeriesDataSet.getTimeSeries().containsKey(series.taxi)) {
                TimeSeriesObject existing = timeSeriesDataSet.timeSeries.get(series.taxi);

                for (TimeSeriesPoint point : series.points.values()) {
                    existing.addPoint(point);
                }
                return;

            }
            // or create a new series
            timeSeriesDataSet.addSeries(series);
        } finally {
            logEvent().parameter("taxi", series.taxi)
                    .parameter("name", series.name)
                    .trace("completed parsing time series data block");

            this.state = COMPLETED;
        }
    }

    /**
     * parse a line type '92' - extract the series taxi value from the line. If open return true to indicate the
     * current block is completed and a new block should begin.
     */
    private boolean parseLineType92(String line, int startIndex) throws IOException {
        if (BLOCK_STARTED == this.state) {
            // If the block parsing is started and we encourter another line typw 92 - then we have reached the end
            // of the current block.
            this.state = BLOCK_ENDED;
            return true;
        }

        if (line.length() < 6) {
            throw new DataBlockException(LINE_LENGTH_ERROR, TYPE_92, 6, line.length(), startIndex);
        }

        this.series.taxi = line.substring(2, 6);
        this.state = BLOCK_STARTED;
        return false;
    }

    /**
     * Parse a line type '93' - extract the series name from the line.
     */
    private void parseLineType93(String line, int lineIndex) throws IOException {
        if (line.length() < 2) {
            throw new DataBlockException(LINE_LENGTH_ERROR, TYPE_93, 2, line.length(), lineIndex);
        }
        if (line.length() < 3) {
            logEvent().index(lineIndex).parameter("line", line).parameter("lineType", TYPE_93)
                    .warn("time series object has no name value - please check if this is expected");
        }
        this.series.name = line.substring(2);
    }

    /**
     * Parse a line type '96' - extract the series date label from the line.
     */
    private void parseLineType96(String line, int lineIndex) throws IOException {
        if (line.length() < 11) {
            throw new DataBlockException(LINE_LENGTH_ERROR, TYPE_96, 11, line.length(), lineIndex);
        }

        String mqa = line.substring(2, 3);
        int year = Integer.parseInt(line.substring(4, 8));
        int startInd = Integer.parseInt(line.substring(9, 11).trim());

        this.dateLabel = new DateLabel(year, startInd, mqa);
    }

    /**
     * Parse a line type '97' - extract the series point values from the line.
     */
    private void parseLineType97(String line, int lineIndex)
            throws IOException {
        if (line.length() < 9) {
            throw new DataBlockException(LINE_LENGTH_ERROR, TYPE_97, 9, line.length(), lineIndex);
        }
        String values = line.substring(2);
        while (values.length() > 9) {
            String oneValue = values.substring(0, 10).trim();

            series.addPoint(timeSeriesPointGenerator.create(dateLabel, oneValue));
            values = values.substring(10);
        }
    }

    public State getState() {
        return state;
    }

    DateLabel getDateLabel() {
        return dateLabel;
    }

    TimeSeriesObject getSeries() {
        return series;
    }

    /**
     * Constants representing the possible states of {@link DataBlockParser}
     */
    enum State {

        /**
         * Initial state - a data block is not started until a line type 92 is provided.
         */
        NOT_STARTED,

        /**
         * Once an inital line type 92 is passed to the parser then the parse moves to this state to indicate it is
         * currently processing a block of data.
         */
        BLOCK_STARTED,

        /**
         * If the parser is in {@link State#BLOCK_STARTED} and it receieves another line type 92 - this indicates the
         * end current block of data. While in this state the parse will reject any further input.
         */
        BLOCK_ENDED,

        /**
         * The final state - once the data block as been added to the parent time series data set the state is set to
         * completed - the parser will no longer do anything.
         */
        COMPLETED,
    }
}
