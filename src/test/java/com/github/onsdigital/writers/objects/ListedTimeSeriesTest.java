package com.github.onsdigital.writers.objects;

import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.generators.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ListedTimeSeriesTest {

    @Test
    public void listedTimeSeriesShouldBeCreatedEqualToNormal() {

        // Given
        //... a random time series
        TimeSeries test = Sample.quickWalk(12345);


        // When
        //... we convert it to a listed time series
        ListedTimeSeries listed = new ListedTimeSeries(test);


        // Then
        //... we expect no loss of points
        assertEquals(test.points.size(), listed.points.size());
        for(TimeSeriesPoint point: listed.points) {
            assertTrue(test.points.containsKey(point.timeLabel));
            TimeSeriesPoint testPoint = test.points.get(point.timeLabel);

            assertEquals(point.timeLabel, testPoint.timeLabel);
            assertEquals(point.value, testPoint.value);
        }
    }

    @Test
    public void listedTimeSeriesShouldConvertBackToNormal() throws Exception {
        // Given
        //... a random time series that we have turned into listed time series
        TimeSeries test1 = Sample.quickWalk(12345);
        ListedTimeSeries listed = new ListedTimeSeries(test1);

        // When
        //... we convert it to a back to a time series
        TimeSeries test2 = listed.toTimeSeries();

        // Then
        //... we expect the converted series to be the identical to the original
        assertEquals(test1.points.size(), test2.points.size());
        for(TimeSeriesPoint point: test1.points.values()) {
            assertTrue(test2.points.containsKey(point.timeLabel));
            TimeSeriesPoint testPoint = test2.points.get(point.timeLabel);

            assertEquals(point.timeLabel, testPoint.timeLabel);
            assertEquals(point.value, testPoint.value);
        }
    }
}