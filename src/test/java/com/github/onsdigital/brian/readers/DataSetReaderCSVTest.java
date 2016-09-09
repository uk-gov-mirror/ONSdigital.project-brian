package com.github.onsdigital.brian.readers;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by thomasridd on 2/3/16.
 */
public class DataSetReaderCSVTest {
    Path example;

    @Before
    public void setUp() throws Exception {
        URL resource = TimeSeriesDataSet.class.getResource("/examples/POP.csv");
        example = Paths.get(resource.toURI());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testReadFile() throws Exception {
        // Given
        DataSetReaderCSV dataSetReaderCSV = new DataSetReaderCSV();

        // When
        TimeSeriesDataSet timeSeriesDataSet = dataSetReaderCSV.readFile(example, null);

        // Then
        assertNotNull(timeSeriesDataSet);
    }
}