package com.github.onsdigital.publishers;


import com.github.davidcarboni.ResourceUtils;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.data.TimeSeriesObject;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.readers.DataSetReaderCSDB;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Created by thomasridd on 09/06/15.
 */
public class TimeSeriesPublisherTest {


    @Test
    public void testTimeSeriesObjectAsTimeSeries() throws Exception {
        // Given
        // a time series
        TimeSeriesObject ts = Sample.randomWalk(100, 1000, 100, 1980, 1990, true, true, true);

        // When
        // we convert to a publishable series
        TimeSeries series = TimeSeriesPublisher.convertToContentLibraryTimeSeries(ts);

        // Then
        // we expect a result
        assertNotNull(series);
        assertEquals(ts.taxi, series.getDescription().getCdid());
    }

    @Test
    public void testDataSetReaderKicksOutTheCorrectYearsForQuartersAndMonths() throws Exception {
        // Given
        // a time series
        String resourceName = "/examples/CSDB";

        // When
        // we read it to a csdb file
        Path path = ResourceUtils.getPath(resourceName);
        TimeSeriesDataSet csdb = DataSetReaderCSDB.readFile(path);

        // Then
        // we expect a the correct number of time series to be returned
        assertDatasetCorrespondsToCSDB(csdb);
        // and the date points are appropriate
        for (TimeSeriesObject timeSeries: csdb.timeSeries.values()) {
            if (timeSeries.hasQuarterly || timeSeries.hasMonthly) {
                for (String point: timeSeries.points.keySet()) {
                    assertNotEquals("2015 Q4", point);
                    assertNotEquals("2015 DEC", point);
                }
            }
        }
    }

    @Test
    public void testDataSetReaderKicksOutTheCorrectFinalNumberForTimeSeries() throws Exception {
        // Given
        // a time series
        String resourceName = "/examples/SmallExample";
        Path path = ResourceUtils.getPath(resourceName);
        TimeSeriesDataSet csdb = DataSetReaderCSDB.readFile(path);
        TimeSeriesObject series = csdb.getSeries("DS38");

        // When
        // we convert it to the return type
        TimeSeries contentLibraryTimeSeries = TimeSeriesPublisher.convertToContentLibraryTimeSeries(series);

        // Then
        // we expect the number will be returned
        assertEquals("2120", contentLibraryTimeSeries.getDescription().getNumber());
    }

    @Test
    public void testDataSetReaderKicksOutTheCorrectFinalDateForTimeSeries() throws Exception {
        // Given
        // a time series
        String resourceName = "/examples/SmallExample";
        Path path = ResourceUtils.getPath(resourceName);
        TimeSeriesDataSet csdb = DataSetReaderCSDB.readFile(path);
        TimeSeriesObject series = csdb.getSeries("DS38");

        // When
        // we convert it to the return type
        TimeSeries contentLibraryTimeSeries = TimeSeriesPublisher.convertToContentLibraryTimeSeries(series);

        // Then
        // we expect the number will be returned
        assertEquals("2014 Q3", contentLibraryTimeSeries.getDescription().getDate());
    }

    public void assertDatasetCorrespondsToCSDB(TimeSeriesDataSet dataSet) {
        assertEquals(128, dataSet.timeSeries.size());
    }
}