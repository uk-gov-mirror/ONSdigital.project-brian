package com.github.onsdigital.publishers;


import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.data.TimeSeriesObject;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.readers.DataSetReaderCSDB;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        String resourcePath = TimeSeriesPublisherTest.class.getResource(resourceName).getPath();
        TimeSeriesDataSet csdb = DataSetReaderCSDB.readFile(Paths.get(resourcePath));

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

    public void assertDatasetCorrespondsToCSDB(TimeSeriesDataSet dataSet) {
        assertEquals(88, dataSet.timeSeries.size());
    }
}