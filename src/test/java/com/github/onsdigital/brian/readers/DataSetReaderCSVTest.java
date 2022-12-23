package com.github.onsdigital.brian.readers;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;

import com.github.onsdigital.brian.exception.BadFileException;
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

    @Test(expected = BadFileException.class)
    public void testReadNonTimeSeriesCSVFile() throws Exception {
        // Given a valid CSV file containing unexpected data
        URL resource = TimeSeriesDataSet.class.getResource("/examples/invalid/nonsense.csv");
        Path badExample = Paths.get(resource.toURI());
        DataSetReaderCSV dataSetReaderCSV = new DataSetReaderCSV();

        // When
        dataSetReaderCSV.readFile(badExample, null);
    }

    @Test(expected = BadFileException.class)
    public void testReadInvalidFile() throws Exception {
        // Given an invalid CSV file
        URL resource = TimeSeriesDataSet.class.getResource("/examples/invalid/invalid.csv");
        Path badExample = Paths.get(resource.toURI());
        DataSetReaderCSV dataSetReaderCSV = new DataSetReaderCSV();

        // When
        dataSetReaderCSV.readFile(badExample, null);
    }
}
