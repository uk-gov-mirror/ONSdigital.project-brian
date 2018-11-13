package com.github.onsdigital.brian.readers.csdb;

import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_LENGTH_ERROR_UNKNOWN_TYPE;
import static com.github.onsdigital.brian.readers.csdb.CSDBLineParseException.LINE_TYPE_INT_PARSE_ERROR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DataSetReaderCSDBTest {

    static final String LINE_TYPE_92 = "92A8E3AU2014   0 0            BERD1                      1  0  0  1  6";
    static final String LINE_TYPE_93 = "93Expenditure on R&D permormed in UK Businesses - Constant Price";
    static final String LINE_TYPE_96 = "96AI1981  120171114   36             710 0";
    static final String LINE_TYPE_97 = "97     11211         0     10793         0     11781     13118     13188";

    private DataSetReaderCSDB reader;

    @Before
    public void setUp() {
        reader = new DataSetReaderCSDB();
    }

    @Test(expected = CSDBLineParseException.class)
    public void getLineTypeShouldThrowExceptionIfLineNull() throws IOException {
        try {
            reader.getLineType(null, 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, 1), e);
        }
    }

    @Test(expected = CSDBLineParseException.class)
    public void getLineTypeShouldThrowExceptionIfLineBlank() throws IOException {
        try {
            reader.getLineType("", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 0, 1), e);
        }
    }

    @Test(expected = CSDBLineParseException.class)
    public void getLineTypeShouldThrowExceptionIfLineLengthLessThan2() throws IOException {
        try {
            reader.getLineType("1", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR_UNKNOWN_TYPE, 2, 1, 1), e);
        }
    }

    @Test(expected = CSDBLineParseException.class)
    public void getLineTypeShouldThrowExceptionIfLineTypeNotInt() throws IOException {
        try {
            reader.getLineType("XX", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_TYPE_INT_PARSE_ERROR, "XX", 1), e);
        }
    }

    @Test
    public void getLineTypeSuccessful() throws IOException {
        int result = reader.getLineType("92", 1);
        assertThat(result, equalTo(92));
    }

    @Test
    public void processLineType92Success() throws IOException {
        assertThat(reader.processLineType92(LINE_TYPE_92, 1), equalTo("A8E3"));
    }

    @Test(expected = CSDBLineParseException.class)
    public void processLineType92LineLessThan6() throws IOException {
        try {
            reader.processLineType92("92123", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR, 92, 6, 5, 1), e);
        }
    }

    @Test
    public void processLineType93Success() throws IOException {
        String expected = "Expenditure on R&D permormed in UK Businesses - Constant Price";
        assertThat(reader.processLineType93(LINE_TYPE_93, 1), equalTo(expected));
    }

    @Test(expected = CSDBLineParseException.class)
    public void processLineType93LineLengthInvalid() throws IOException {
        try {
            reader.processLineType93("1", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR, 93, 2, 1, 1), e);
        }
    }

    @Test
    public void processLineType96Success() throws IOException {
        DateLabel actual = reader.processLineType96(LINE_TYPE_96, 1);
        DateLabel expected = new DateLabel(1981, 1, "A");
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = CSDBLineParseException.class)
    public void processLineType96LineLengthInvalid() throws IOException {
        try {
            reader.processLineType96("1234567890", 1);
        } catch (CSDBLineParseException e) {
            throw assertException(new CSDBLineParseException(LINE_LENGTH_ERROR, 96, 11, 10, 1), e);
        }
    }




    @Test
    public void processLineType97Success() throws IOException {
        DateLabel dl = new DateLabel(1981, 1, "A");
        List<TimeSeriesPoint> points = reader.processLineType97(LINE_TYPE_97, dl, 1);
    }

    private CSDBLineParseException assertException(CSDBLineParseException expected, CSDBLineParseException actual) {
        assertThat(expected.getMessage(), equalTo(actual.getMessage()));
        return actual;
    }
}
