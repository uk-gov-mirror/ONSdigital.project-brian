package com.github.onsdigital.data;

import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.objects.DataDimension;
import com.github.onsdigital.readers.DataCubeReaderWDA;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class DataCubeQueryTest {
    public static DataCube dataCube;


    @Before
    public void setupTestCube() throws IOException, URISyntaxException {
        dataCube = DataCubeReaderWDA.readFile("/examples/wdaTestSet/fertility.csv");
    }

    @Test
    public void testAddFilters() throws Exception {
        // Given ...
        // our example datacube
        DataCubeQuery query = new DataCubeQuery(dataCube);

        // When ...
        // we add filters using various styles
        query.addFilter("ProjectionType", "Low fertility");

        query.addFilter("Age", new ArrayList<String>(Arrays.asList("15", "16", "17", "18", "19", "20")));

        DataDimension dimension = new DataDimension("Geography");
        dimension.addValue("UK");
        query.addFilter(dimension);

        // We expect
        // our filters to exist
        assertNotNull(query.filters.get("ProjectionType"));
        assertEquals(query.filters.get("ProjectionType").values.size(), 1);

        assertNotNull(query.filters.get("Age"));
        assertEquals(query.filters.get("Age").values.size(), 6);

        assertNotNull(query.filters.get("Geography"));
        assertEquals(query.filters.get("Geography").values.size(), 1);
    }

    @Test
    public void shouldResolveFilters() {
        // Give ...
        // a query built from our example datacube
        DataCubeQuery query = new DataCubeQuery(dataCube);
        // with some filters applied
        query.addFilter("ProjectionType", "Low fertility");
        query.addFilter("Geography", "UK");
        query.addFilter("Age", new ArrayList<String>(Arrays.asList("15", "16")));

        // When ...
        // we begin the process of filtering
        DataCube cube = query.createNewDataCube();

        // We expect
        // a data cube
        assertNotNull(cube);

        // the Geography and ProjectionType dimensions should be gone
        assertFalse(cube.dimensions.containsKey("Geography"));

        // the Time Period dimension should be untouched
        assertTrue(cube.dimensions.containsKey("Time Period"));

        // the Age dimension should be cut down to 2 values
        assertTrue(cube.dimensions.get("Age").values.size() == 2);
    }

    @Test
    public void shouldFillCube() {
        // Give ...
        // a query built from our example datacube
        DataCubeQuery query = new DataCubeQuery(dataCube);
        // with some filters applied
        query.addFilter("ProjectionType", "Low fertility");
        query.addFilter("Geography", "UK");
        query.addFilter("Age", new ArrayList<String>(Arrays.asList("15", "16")));

        // When ...
        // we filter
        query.createNewDataCube();
        DataCube cube = query.fillNewDataCube();

        // We expect
        // a data cube
        assertNotNull(cube);
        // with expected values
        HashMap<String,String> cubeIndex = new HashMap<>();
        cubeIndex.put("Age", "16");
        cubeIndex.put("Time Period", "2020");

        HashMap<String,String> dataCubeIndex = new HashMap<>();
        dataCubeIndex.put("Age", "16");
        dataCubeIndex.put("Time Period", "2020");
        dataCubeIndex.put("Geography", "UK");
        dataCubeIndex.put("ProjectionType", "Low fertility");

        assertEquals(dataCube.get(dataCubeIndex), cube.get(cubeIndex), 0.00001);
    }
}