package com.github.onsdigital.data;

import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.generators.Sample;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataSetTest {


    @Test
    public void shouldShallowCopy() throws Exception {
        // Given
        //... a data set
        DataSet master = Sample.quickWalks(5, 12345);

        // When
        //... we clone it
        DataSet clone = master.clone();

        // We expect
        //... all the properties to be identical
        assertEquals(master.name, clone.name); // CHECK BASIC PROPERTIES
        assertEquals(master.source, clone.source);

        //... and the series to be shallow copies
        for(TimeSeries series: master.timeSeries.values()) { // CHECK SERIES EXIST
            assertTrue(clone.timeSeries.containsKey(series.taxi) == true);
            assertEquals(series, clone.timeSeries.get(series.taxi));
        }
    }

    @Test
    public void shouldAddSeries() throws Exception {
        // Given
        //... a series and a data set
        TimeSeries series = Sample.quickWalk(12345);
        DataSet dataSet = Sample.quickWalks(5, 12340);

        // When
        //... we add the series to the dataset
        dataSet.addSeries(series);

        // We expect
        //... the series to be added
        assertTrue(dataSet.timeSeries.containsKey(series.taxi));
    }

    @Test
    public void shouldMergeDisjoint() throws Exception {
        // Given
        //... two disjoint data sets
        DataSet dataSet1 = Sample.quickWalks(5, 12345);
        DataSet dataSet2 = Sample.quickWalks(5, 12350);

        // When
        //... we merge the two data sets
        DataSet dataSet = dataSet1.mergeWith(dataSet2, true);

        // We expect
        //... all the series from our initial datasets to be present
        dataSet1 = Sample.quickWalks(5, 12345);
        dataSet2 = Sample.quickWalks(5, 12350);

        for(TimeSeries series: dataSet1.timeSeries.values()) {
            assertTrue(dataSet.timeSeries.containsKey(series.taxi));
        }

        for(TimeSeries series: dataSet2.timeSeries.values()) {
            assertTrue(dataSet.timeSeries.containsKey(series.taxi));
        }
    }

    @Test
    public void shouldMergeNewData() throws Exception {
        // Given
        //... intersecting data sets
        DataSet dataSet1 = Sample.randomWalks(5, 12345, 100, 1, 1991, 2010, false, false, true);
        DataSet dataSet2 = Sample.randomWalks(5, 12346, 100, 1, 1996, 2015, false, false, true);
        //... where at least one piece of data is conflicting
        dataSet2.timeSeries.get("RAND12346").points.get("2010").value = "1000";


        // When
        //... we merge the two data sets
        DataSet dataSet = dataSet1.mergeWith(dataSet2, true);

        // We expect
        //... there to be a total of 6 series
        assertEquals(dataSet.timeSeries.size(), 6);

        //... disjoint timeseries to be present but not affected
        TimeSeries shortEarlySeries = dataSet.timeSeries.get("RAND12345");
        TimeSeries shortLateSeries = dataSet.timeSeries.get("RAND12350");
        assertEquals(shortEarlySeries.points.size(), 20);
        assertEquals(shortLateSeries.points.size(), 20);

        //... series with additions to be lengthened
        TimeSeries longSeries = dataSet.timeSeries.get("RAND12347");
        assertEquals(longSeries.points.size(), 25);

        //... series with conflicts to be lengthened and with the clash updated
        TimeSeries conflictingSeries = dataSet.timeSeries.get("RAND12346");
        assertEquals(conflictingSeries.points.size(), 25);
        assertEquals(conflictingSeries.points.get("2010").value, "1000");

    }


}