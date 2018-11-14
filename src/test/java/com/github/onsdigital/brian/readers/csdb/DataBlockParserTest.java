package com.github.onsdigital.brian.readers.csdb;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;

import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_TYPE_INT_PARSE_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.BLOCK_ENDED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.BLOCK_STARTED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.COMPLETED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.State.NOT_STARTED;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_92;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_93;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_96;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_97;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataBlockParserTest {

    static final String LINE_TYPE_92 = "92A8E3AU2014   0 0            BERD1                      1  0  0  1  6";
    static final String LINE_TYPE_93 = "93Expenditure on R&D permormed in UK Businesses - Constant Price";
    static final String LINE_TYPE_96 = "96AI1981  120171114   36             710 0";
    static final String LINE_TYPE_97 = "97     11211         0     10793         0     11781     13118     13188";

    static final String SERIES_TAXI = "A8E3"; // line type 92 substring 2 - 6
    static final String SERIES_NAME = "Expenditure on R&D permormed in UK Businesses - Constant Price";

    @Mock
    private TimeSeriesObject timeSeriesObject;

    @Mock
    private DateLabel dateLabel;

    @Mock
    private TimeSeriesPointGenerator timeSeriesPointGenerator;

    @Mock
    private TimeSeriesDataSet dataSet;

    private DataBlockParser parser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parser = new DataBlockParser();
    }

    @Test(expected = DataBlockException.class)
    public void getLineTypeShouldThrowExceptionIfLineNull() throws IOException {
        try {
            parser.getLineType(null, 1);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, 1), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void getLineTypeShouldThrowExceptionIfLineBlank() throws IOException {
        try {
            parser.getLineType("", 1);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, 1), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void getLineTypeShouldThrowExceptionIfLineLengthLessThan2() throws IOException {
        try {
            parser.getLineType("1", 1);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 1, 1), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void getLineTypeShouldThrowExceptionIfLineTypeNotInt() throws IOException {
        try {
            parser.getLineType("XX", 1);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_TYPE_INT_PARSE_ERROR, "XX", 1), e);
        }
    }

    @Test
    public void getLineTypeSuccessful() throws IOException {
        int result = parser.getLineType(LINE_TYPE_92, 1);
        assertThat(result, equalTo(TYPE_92));

        result = parser.getLineType(LINE_TYPE_93, 1);
        assertThat(result, equalTo(TYPE_93));


        result = parser.getLineType(LINE_TYPE_96, 1);
        assertThat(result, equalTo(TYPE_96));

        result = parser.getLineType(LINE_TYPE_97, 1);
        assertThat(result, equalTo(TYPE_97));
    }

    @Test
    public void testParseLineType92Success() throws IOException {
        boolean isComplete = parser.parseLine(LINE_TYPE_92, 0);

        assertFalse(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat(parser.getSeries().taxi, equalTo("A8E3"));
    }

    /**
     * Minimum length of a line type 92 is 6.
     */
    @Test(expected = DataBlockException.class)
    public void testParseLineType92InvalidLength() throws IOException {
        try {
            parser.parseLine("92---", 0);
        } catch (DataBlockException e) {
            assertThat(parser.getState(), equalTo(NOT_STARTED));
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR, 92, 6, 5, 0), e);
        }
    }

    @Test
    public void testParseLineType92Completed() throws IOException {
        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertThat(parser.getSeries().taxi, equalTo(SERIES_TAXI));
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));

        // Add the next line type 92 to end the current block.
        assertTrue(parser.parseLine(LINE_TYPE_92, 0));
        assertThat(parser.getState(), equalTo(BLOCK_ENDED));
        assertThat(parser.getSeries().taxi, equalTo(SERIES_TAXI));
    }

    @Test
    public void testParseLineType93Success() throws IOException {
        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertFalse(parser.parseLine(LINE_TYPE_93, 1));

        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat("series name did not match the expected value", parser.getSeries().name, equalTo(SERIES_NAME));
    }

    @Test
    public void testParseLineType93EmptyName() throws IOException {
        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertFalse(parser.parseLine("93", 1));

        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat("series name did not match the expected value", parser.getSeries().name, equalTo(""));
    }

    @Test
    public void testParseLineType96Success() throws IOException {
        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertFalse(parser.parseLine(LINE_TYPE_96, 1));

        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat("incorrect DateLabel generated", parser.getDateLabel(), equalTo(new DateLabel(1981, 1, "A")));
    }

    /**
     * Minimum length of a line type 96 is 11.
     */
    @Test(expected = DataBlockException.class)
    public void testParseLineType96InvalidLength() throws IOException {
        try {
            assertFalse(parser.parseLine(LINE_TYPE_92, 0));
            parser.parseLine("96--------", 1);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR, TYPE_96, 11, 10, 1), e);
        }
    }

    @Test
    public void testParseLineType97Success() throws IOException {
        TimeSeriesPoint point = mock(TimeSeriesPoint.class);

        when(timeSeriesPointGenerator.create(any(DateLabel.class), anyString()))
                .thenReturn(point);

        parser = new DataBlockParser(timeSeriesObject, dateLabel, timeSeriesPointGenerator);

        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertFalse(parser.parseLine(LINE_TYPE_97, 1));
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));

        // verify for each for the values in the the LINE_TYPE_97 constant.
        verify(timeSeriesPointGenerator, times(1)).create(dateLabel, "11211");
        verify(timeSeriesPointGenerator, times(2)).create(dateLabel, "0");
        verify(timeSeriesPointGenerator, times(1)).create(dateLabel, "10793");
        verify(timeSeriesPointGenerator, times(1)).create(dateLabel, "11781");
        verify(timeSeriesPointGenerator, times(1)).create(dateLabel, "13118");
        verify(timeSeriesPointGenerator, times(1)).create(dateLabel, "13188");

    }

    @Test(expected = DataBlockException.class)
    public void testParseLineWithNull() throws IOException {
        try {
            parser.parseLine(null, 0);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, 0), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void testParseLineInvalidLength() throws IOException {
        try {
            parser.parseLine("1", 0);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 1, 0), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void testParseLineInvalidLineType() throws IOException {
        try {
            parser.parseLine("XY", 0);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_TYPE_INT_PARSE_ERROR, "XY", 0), e);
        }
    }

    @Test
    public void testParseLineSuccess() throws IOException {
        TimeSeriesPoint point = mock(TimeSeriesPoint.class);

        when(timeSeriesPointGenerator.create(any(DateLabel.class), anyString()))
                .thenReturn(point);

        DateLabel expectedDateLabel = new DateLabel(1981, 1, "A");

        parser = new DataBlockParser(timeSeriesObject, null, timeSeriesPointGenerator);

        boolean isComplete = parser.parseLine(LINE_TYPE_92, 1);
        assertFalse(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat(parser.getSeries().taxi, equalTo(SERIES_TAXI));

        isComplete = parser.parseLine(LINE_TYPE_93, 2);
        assertFalse(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat(parser.getSeries().name, equalTo(SERIES_NAME));

        isComplete = parser.parseLine(LINE_TYPE_96, 3);
        assertFalse(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        assertThat(parser.getDateLabel(), equalTo(expectedDateLabel));

        isComplete = parser.parseLine(LINE_TYPE_97, 4);
        assertFalse(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_STARTED));
        verify(timeSeriesPointGenerator, times(7)).create(any(DateLabel.class), anyString());

        isComplete = parser.parseLine(LINE_TYPE_92, 5);
        assertTrue(isComplete);
        assertThat(parser.getState(), equalTo(BLOCK_ENDED));
    }

    @Test(expected = DataBlockException.class)
    public void testCompleteNotOpen() throws IOException {
        try {
            parser.complete(null);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException("complete invoked on data block that has not been started"), e);
        }
    }

    @Test(expected = DataBlockException.class)
    public void testCompleteAlreadyCompleted() throws IOException {
        try {
            assertFalse(parser.parseLine(LINE_TYPE_92, 0));
            assertFalse(parser.parseLine(LINE_TYPE_93, 0));
            assertFalse(parser.parseLine(LINE_TYPE_96, 0));
            assertFalse(parser.parseLine(LINE_TYPE_97, 0));
            assertTrue(parser.parseLine(LINE_TYPE_92, 0));
            parser.complete(new TimeSeriesDataSet());

            parser.complete(new TimeSeriesDataSet());
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException("complete invoked on data block that has already been competed, " +
                    "series: %s", SERIES_TAXI), e);
        }
    }

    @Test
    public void testCompeleteSuccesss() throws IOException {
        when(dataSet.getTimeSeries())
                .thenReturn(new HashMap<>());

        parser = new DataBlockParser(timeSeriesObject, null, timeSeriesPointGenerator);

        assertFalse(parser.parseLine(LINE_TYPE_92, 0));
        assertFalse(parser.parseLine(LINE_TYPE_93, 0));
        assertFalse(parser.parseLine(LINE_TYPE_96, 0));
        assertFalse(parser.parseLine(LINE_TYPE_97, 0));
        assertTrue(parser.parseLine(LINE_TYPE_92, 0));
        parser.complete(dataSet);

        assertThat(parser.getState(), equalTo(COMPLETED));
        verify(dataSet, times(1)).addSeries(timeSeriesObject);
    }

    private DataBlockException assertAndThrow(DataBlockException expected, DataBlockException actual) {
        assertThat("incorrect error details", expected.getMessage(), equalTo(actual.getMessage()));
        return actual;
    }
}
