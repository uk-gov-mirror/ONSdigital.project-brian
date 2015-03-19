package com.github.onsdigital.database;

import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.generators.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FlatFileJSONControllerTest {

    @Before
    public void setUp() throws Exception {
        FlatFileJSONController.initialise();
    }
    @After
    public void shutDown() throws Exception {
        if(FlatFileJSONController.exists("RAND00001")) {
            FlatFileJSONController.delete("RAND00001");
        }
    }

    @Test
    public void shouldCreateTimeSeriesFile() throws Exception {

        // Given
        //... a time series
        TimeSeries series = Sample.quickWalk(1);

        // When
        //... we create a record in our flat file database
        FlatFileJSONController.create(series);

        // Then
        //... we expect them to exist
        assertTrue(FlatFileJSONController.exists(series.taxi));
    }

    @Test
    public void shouldGetSeriesFile() throws Exception {

        // Given
        //... a time series
        TimeSeries series = Sample.quickWalk(1);

        // When
        //... we create a record in our flat file database
        String taxi = series.taxi;
        FlatFileJSONController.create(series);

        TimeSeries series2 = FlatFileJSONController.get(taxi);
        // Then
        //... we expect them to exist
        assertTrue(series2 != null);
    }
}