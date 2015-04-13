package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.data.objects.DataCubeSet;
import com.github.onsdigital.readers.DataCubeReaderWDA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class DataCubeWriterJSONTest {
    public static DataCubeSet dataCubeSet;
    public static List<String> EXPECTED_CUBE_NAMES;
    public static List<String> EXPECTED_DIMENSIONS;
    @Before
    public void setupTestCubeSet() throws IOException, URISyntaxException {
        dataCubeSet = DataCubeReaderWDA.readDirectory("/examples/wdaTestSet/");
        EXPECTED_CUBE_NAMES = new ArrayList<String>(Arrays.asList("fertility", "prodcomms"));
        EXPECTED_DIMENSIONS = new ArrayList<String>(Arrays.asList("Age", "ProjectionType", "Time Period", "Geography"));
    }

    @Test
    public void testDataCubeSetLoaded() throws Exception {
        // Given...
        // the before routine has run

        // We expect
        assertTrue(dataCubeSet.cubes.size() == 2); // 2
        for(String key: dataCubeSet.cubes.keySet()) {
            assertTrue(EXPECTED_CUBE_NAMES.contains(dataCubeSet.cubes.get(key).get().name));
        }
    }

    @Test
    public void testDataCubeSetJSON() throws ExecutionException, InterruptedException {
        // Given...
        // the before routine has run

        // When...
        // we generate JSON for the collected set
        String JSON = DataCubeWriterJSON.dataCubeSetAsJSON(dataCubeSet);

        // We expect...
        // the structure of the JSON to reflect what went in
        JsonParser parser = new JsonParser();

        // Json should return something
        JsonObject object = (JsonObject) parser.parse(JSON);
        assertNotNull(object);

        // The top object should be an array of cubes
        JsonArray cubes = object.getAsJsonArray("cubes");
        assertNotNull(object);

        // The sub objects should have
        for(int i = 0; i < cubes.size(); i++) {
            JsonObject cube = (JsonObject) cubes.get(i); // 1. A name from our standard list
            assertNotNull(cube);
            assertTrue(EXPECTED_CUBE_NAMES.contains(cube.get("name").getAsString()));

            JsonArray dimensions = cube.getAsJsonArray("dimensions");
            assertNotNull(dimensions);
            assertTrue(dimensions.size() >= 1);
        }
    }

    @Test
    public void testDataCubeSummaryJSON() throws ExecutionException, InterruptedException {

        // Given...
        // the first data cube from the test set
        DataCube cube = dataCubeSet.cubes.get("fertility").get();

        // When...
        // we generate JSON for the cube
        String JSON = DataCubeWriterJSON.dataCubeSummaryAsJSON(cube);

        // We expect...
        // the structure of the JSON to reflect what went in
        JsonParser parser = new JsonParser();

        // Json should return something
        JsonObject object = (JsonObject) parser.parse(JSON);
        assertNotNull(object);

        // The name should be the
        String name = object.get("name").getAsString();
        assertNotNull(name);
        assertEquals("fertility", name);

        // The sub objects should be dimensions
        JsonArray dimensions = object.getAsJsonArray("dimensions");
        assertNotNull(dimensions);
        for(int i = 0; i < dimensions.size(); i++) {
            JsonObject dimObj = dimensions.get(i).getAsJsonObject();
            assertNotNull(dimObj);

            name = dimObj.get("name").getAsString(); // They should have a name
            assertTrue(EXPECTED_DIMENSIONS.contains(name));

            JsonArray dimValues = dimObj.getAsJsonArray("values"); // And a list of values
            assertNotNull(dimValues);
            assertTrue(dimValues.size() >= 2); // With a minimum of two values
        }
        System.out.println(JSON);
    }

    @Test
    public void testDataCubeSlicedJSON() throws ExecutionException, InterruptedException {
        // Given...
        // the first data cube from the test set
        DataCube cube = dataCubeSet.cubes.get("fertility").get();
        String query = "Age=15&Geography=Wales";
        List<NameValuePair> slice = URLEncodedUtils.parse(query, Charset.forName("utf8"));
        // When...
        // we generate JSON for the cube
        String JSON = DataCubeWriterJSON.dataCubeSlicedAsJSON(cube, slice);

        // We expect...
        assertNotNull(JSON);
    }
}