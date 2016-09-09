package com.github.onsdigital.brian.data.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TimeSeriesPointTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldInitialiseWithYear() throws Exception {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        // Given
        //... a yearly time label
        String dateLabel = "2014";

        // When
        //... we initialise a TimeSeriesPoint
        TimeSeriesPoint yearPoint = new TimeSeriesPoint("2014", "100");

        assertEquals(yearPoint.value, "100");
        assertEquals(yearPoint.startDate, df.parse("01/01/2014") );
        assertEquals(yearPoint.timeLabel, "2014");
        assertEquals(yearPoint.period, TimeSeriesPoint.PERIOD_YEARS);
        assertEquals(yearPoint.toString(), "2014: 100");
    }

    @Test
    public void shouldInitialiseWithMonth() throws Exception {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        // Given
        //... a monthly time label
        String dateLabel = "2014 Oct";

        // When
        //... we initialise a TimeSeriesPoint
        TimeSeriesPoint monthPoint = new TimeSeriesPoint(dateLabel, "200");

        // Then
        //... we expect to get some standard values
        assertEquals(monthPoint.value, "200");
        assertEquals(monthPoint.startDate, df.parse("01/10/2014") );
        assertEquals(monthPoint.timeLabel, "2014-10");
        assertEquals(monthPoint.period, TimeSeriesPoint.PERIOD_MONTHS);
        assertEquals(monthPoint.toString(), "2014-10: 200");
    }

    @Test
    public void shouldInitialiseWithQuarter() throws Exception {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        // Given
        //... a quarterly time label
        String dateLabel = "2014 Q3";

        // When
        //... we initialise a TimeSeriesPoint
        TimeSeriesPoint quarterPoint = new TimeSeriesPoint("2014 Q3", "300");

        // Then
        //... we expect to get some standard values
        assertEquals(quarterPoint.value, "300");
        assertEquals(quarterPoint.startDate, df.parse("01/07/2014") );
        assertEquals(quarterPoint.timeLabel, "2014 Q3");
        assertEquals(quarterPoint.period, TimeSeriesPoint.PERIOD_QUARTERS);
        assertEquals(quarterPoint.toString(), "2014 Q3: 300");

    }

    @Test
    public void shouldGiveStandardTimeLabel() throws Exception {
        // Given
        //... time labels of various formats
        String[] labels = {"2014", "2014 Q1", "Q4 2014", "Sep 2014", "2014 December"};

        // When
        //... we want the standard for these strings
        ArrayList<String> returned = new ArrayList<>();
        for(String label : labels) {
            returned.add(TimeSeriesPoint.parseTimeLabel(label));
        }
        // Then
        //... we expect to get standard format time labels
        String[] expected = {"2014", "2014 Q1", "2014 Q4", "2014-09", "2014-12"};
        for(int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], returned.get(i));
        }
    }

    @Test
    public void shouldGiveNextStandardTimeLabel() throws Exception {
        // Given
        //... time labels of various formats
        String[] labels = {"2014", "2014 Q1", "Q4 2014", "Sep 2014", "2014 December"};

        // When
        //... we want the next standard for these strings
        ArrayList<String> returned = new ArrayList<>();
        for(String label : labels) {
            returned.add(TimeSeriesPoint.nextTimeLabel(label));
        }
        // Then
        //... we expect to get standard format time labels
        String[] expected = {"2015", "2014 Q2", "2015 Q1", "2014-10", "2015-01"};
        for(int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], returned.get(i));
        }
    }

}