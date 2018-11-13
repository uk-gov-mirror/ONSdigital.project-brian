package com.github.onsdigital.brian.readers.csdb;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.DataBlockException.LINE_TYPE_INT_PARSE_ERROR;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_92;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_93;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_96;
import static com.github.onsdigital.brian.readers.csdb.DataBlockParser.TYPE_97;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DataBlockParserTest {

    static final String LINE_TYPE_92 = "92A8E3AU2014   0 0            BERD1                      1  0  0  1  6";
    static final String LINE_TYPE_93 = "93Expenditure on R&D permormed in UK Businesses - Constant Price";
    static final String LINE_TYPE_96 = "96AI1981  120171114   36             710 0";
    static final String LINE_TYPE_97 = "97     11211         0     10793         0     11781     13118     13188";

    private DataBlockParser parser;

    @Before
    public void setUp() {
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
        boolean isComplete = parser.parseLineType92(LINE_TYPE_92, 0);

        assertFalse(isComplete);
        assertTrue(parser.isOpen());
        assertThat(parser.getSeries().taxi, equalTo("A8E3"));
    }

    /**
     * Minimum length of a line type 92 is 6.
     */
    @Test(expected = DataBlockException.class)
    public void testParseLineType92InvalidLength() throws IOException {
        try {
            parser.parseLineType92("12345", 0);
        } catch (DataBlockException e) {
            throw assertAndThrow(new DataBlockException(LINE_LENGTH_ERROR, 92, 6, 5, 0), e);
        }
    }

    @Test
    public void testParseLineType92Completed() throws IOException {
        assertFalse(parser.parseLineType92(LINE_TYPE_92, 0));
        assertThat(parser.getSeries().taxi, equalTo("A8E3"));
        assertTrue(parser.isOpen());
        assertFalse(parser.isComplete());

        // Add the next line type 92 to end the current block.
        assertTrue(parser.parseLineType92(LINE_TYPE_92, 0));
        assertTrue(parser.isComplete());
        assertFalse(parser.isOpen());
        assertThat(parser.getSeries().taxi, equalTo("A8E3"));
    }

    private DataBlockException assertAndThrow(DataBlockException expected, DataBlockException actual) {
        assertThat(expected.getMessage(), equalTo(actual.getMessage()));
        return actual;
    }
}
