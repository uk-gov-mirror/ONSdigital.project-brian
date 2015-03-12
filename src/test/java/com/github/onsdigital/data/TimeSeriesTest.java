package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.TimeSeriesPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimeSeriesTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldCreateUnbrokenTimeSeries() throws Exception {
        TimeSeries series = new TimeSeries();

        // Given
        //... a time series with holes in
        series.addPoint(new TimeSeriesPoint("2014 Jan", "100"));
        series.addPoint(new TimeSeriesPoint("2014 Oct", "100"));
        series.addPoint(new TimeSeriesPoint("2013 Nov", "100"));
        series.fillInTheBlanks();

        // When
        //... we search for points we didn't add explicitly
        String[] filledPoints = {"2013 Dec", "2014 Feb"};

        // Then
        //... we expect them to exist
        for(String timeLabel: filledPoints) {
            assertNotNull("Expected point missing for: " + timeLabel, series.getPoint(timeLabel));
        }
    }

    @Test
    public void testGetPoint() throws Exception {
        // Given
        //... a time series
        TimeSeries series = new TimeSeries();
        series.addPoint(new TimeSeriesPoint("2014 Jan", "300"));
        series.addPoint(new TimeSeriesPoint("2014 Q1", "200"));
        series.addPoint(new TimeSeriesPoint("2013", "100"));

        // When
        //... we ask for a point
        assertEquals("100", series.getPoint("2013").value);
        assertEquals("200", series.getPoint("2014 Q1").value);
        assertEquals("300", series.getPoint("2014 Jan").value);

    }
}