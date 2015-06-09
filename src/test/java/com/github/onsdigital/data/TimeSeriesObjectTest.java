package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.TimeSeriesPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimeSeriesObjectTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldCreateUnbrokenTimeSeries() throws Exception {
        TimeSeriesObject series = new TimeSeriesObject();

        // Given
        //... a time series with holes in
        series.addPoint(new TimeSeriesPoint("2014 Jan", "100"));
        series.addPoint(new TimeSeriesPoint("2014 Oct", "100"));
        series.addPoint(new TimeSeriesPoint("2013 Nov", "100"));
        series.fillInTheBlanks();

        // When
        //... we search for points we didn't add explicitly
        String[] filledPoints = {"2013-12", "2014-02"};

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
        TimeSeriesObject series = new TimeSeriesObject();
        series.addPoint(new TimeSeriesPoint("2014-01", "300"));
        series.addPoint(new TimeSeriesPoint("2014 Q1", "200"));
        series.addPoint(new TimeSeriesPoint("2013", "100"));

        // When
        //... we ask for a point
        assertEquals("100", series.getPoint("2013").value);
        assertEquals("200", series.getPoint("2014 Q1").value);
        assertEquals("300", series.getPoint("2014-01").value);

    }

    @Test
    public void canMergeSeries() throws Exception {
        // Given
        //... two overlapping time series
        TimeSeriesObject left = new TimeSeriesObject();
        left.addPoint(new TimeSeriesPoint("2013", "100"));
        left.addPoint(new TimeSeriesPoint("2014", "200"));
        left.addPoint(new TimeSeriesPoint("2015", "400"));

        TimeSeriesObject right = new TimeSeriesObject();
        right.addPoint(new TimeSeriesPoint("2014", "400"));
        right.addPoint(new TimeSeriesPoint("2017", "400"));

        // When
        //... we run a merge
        TimeSeriesObject leftPrecedent = TimeSeriesObject.merge(left, right, true);
        TimeSeriesObject rightPrecedent = TimeSeriesObject.merge(left, right, false);

        // Then
        //... we expect 5 points in each series
        assertEquals(5, leftPrecedent.points.size());
        assertEquals(5, rightPrecedent.points.size());

        //... identical points are consistent
        assertEquals("100", leftPrecedent.getPoint("2013").value);
        assertEquals("100", rightPrecedent.getPoint("2013").value);

        //... gaps have been filled
        assertEquals("", leftPrecedent.getPoint("2016").value);
        assertEquals("", rightPrecedent.getPoint("2016").value);

        //... and conflicts are sorted out
        assertEquals("200", leftPrecedent.getPoint("2014").value);
        assertEquals("400", rightPrecedent.getPoint("2014").value);

    }

    @Test
    public void canMergeDisjointSeries() throws Exception {
        // Given
        //... two overlapping time series
        TimeSeriesObject left = new TimeSeriesObject();
        left.addPoint(new TimeSeriesPoint("2000", "100"));
        left.addPoint(new TimeSeriesPoint("2002", "200"));
        left.addPoint(new TimeSeriesPoint("2012", "400"));

        TimeSeriesObject right = new TimeSeriesObject();
        right.addPoint(new TimeSeriesPoint("2014", "400"));
        right.addPoint(new TimeSeriesPoint("2017", "400"));

        // When
        //... we run a merge
        TimeSeriesObject leftPrecedent = TimeSeriesObject.merge(left, right, true);
        TimeSeriesObject rightPrecedent = TimeSeriesObject.merge(left, right, false);

        // Then
        //... we expect 18 points in each series
        assertEquals(18, leftPrecedent.points.size());
        assertEquals(18, rightPrecedent.points.size());

        //... identical points are consistent
        assertEquals("400", leftPrecedent.getPoint("2014").value);
        assertEquals("400", rightPrecedent.getPoint("2014").value);

        //... gaps have been filled
        assertEquals("", leftPrecedent.getPoint("2016").value);
        assertEquals("", rightPrecedent.getPoint("2016").value);

    }
}