package com.github.onsdigital.publishers;


import com.github.onsdigital.content.page.statistics.data.TimeSeries;
import com.github.onsdigital.data.TimeSeriesObject;
import com.github.onsdigital.generators.Sample;
import org.junit.Before;
import org.junit.Test;

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
        assertEquals(ts.taxi, series.cdid);
    }
}