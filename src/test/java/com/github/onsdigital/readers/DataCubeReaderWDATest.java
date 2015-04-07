package com.github.onsdigital.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.data.objects.DataDimension;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class DataCubeReaderWDATest {

    String smallPath = "/examples/wda/smallWDA.csv";
    String bigPath = "/examples/wda/bigWDA.csv";

    @Test
    public void shouldOpenFile() {
        // When we open a file
        boolean success = false;
        try(CSVReader b = DataCubeReaderWDA.openStream(smallPath)) {
            success = true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We shouldn't throw errors
        assertTrue(success);
    }

    @Test
    public void shouldIdentifyDimensions() {
        // Given
        // the small file

        // When
        // We try to read the initial indices from the file
        List<Integer> indices = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try(CSVReader stream = DataCubeReaderWDA.openStream(smallPath)) {
            indices = DataCubeReaderWDA.readDimensionIndicesFromHeader(stream);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(CSVReader stream = DataCubeReaderWDA.openStream(smallPath)) {
            headers = DataCubeReaderWDA.readDimensionNamesFromHeader(stream);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We expect
        // A standard list of indices
        List<Integer> EXPECTED = new ArrayList<Integer>(Arrays.asList(14, 18, 23, 39, 47));
        List<String> EXPECTED_HEADERS = new ArrayList<String>(Arrays.asList("Geography", "Time Period", "Statistical Population", "NACE07", "Prodcom Breakdown"));

        assertArrayEquals(EXPECTED.toArray(), indices.toArray());
        assertArrayEquals(EXPECTED_HEADERS.toArray(), headers.toArray());
    }

    @Test
    public void shouldReadDimensions() {
        // Given...
        // The small file & known indices
        List<Integer> indices = new ArrayList<Integer>(Arrays.asList(14, 18, 23, 39, 47));
        List<String> names = new ArrayList<String>(Arrays.asList("Geography", "Time Period", "Statistical Population", "NACE07", "Prodcom Breakdown"));
        List<DataDimension> dimensions = null;

        // When...
        // We attempt to extract a populated list of dimensions
        try(CSVReader stream = DataCubeReaderWDA.openStream(smallPath)) {
            dimensions = DataCubeReaderWDA.readDimensionsFromStream(stream, indices, names, true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We expect...
        // our object to be not null
        assertNotNull(dimensions);

        // the dimensions should be as above
        List<String> EXPECTED_DIMENSIONS = new ArrayList<String>(Arrays.asList("Geography", "Time Period", "Statistical Population", "NACE07", "Prodcom Breakdown"));
        List<String> dimensionNames = new ArrayList<>();
        for(DataDimension dimension: dimensions) {
            dimensionNames.add(dimension.name);
        }
        assertArrayEquals(EXPECTED_DIMENSIONS.toArray(), dimensionNames.toArray());

        // the geography dimension should be {Top, Middle, Bottom}
        String [] EXPECTED_GEOGRAPHY = {"Bottom", "Middle", "Top"};
        Object [] dimensionValues = {};
        for(DataDimension dimension: dimensions) {
            if(dimension.name.equals("Geography")) {
                dimensionValues = dimension.values.keySet().toArray();

            }
        }
        Arrays.sort(dimensionValues);
        assertArrayEquals(EXPECTED_GEOGRAPHY, dimensionValues);
    }

    @Test
    public void shouldRemoveTrivialDimensions() {
        // Given...
        // The small file & known indices
        List<Integer> indices = new ArrayList<Integer>(Arrays.asList(14, 18, 23, 39, 47));
        List<String> names = new ArrayList<String>(Arrays.asList("Geography", "Time Period", "Statistical Population", "NACE07", "Prodcom Breakdown"));
        List<DataDimension> dimensions = null;
        try(CSVReader stream = DataCubeReaderWDA.openStream(smallPath)) {
            dimensions = DataCubeReaderWDA.readDimensionsFromStream(stream, indices, names, true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // When...
        // We remove the irrelevant dimensions
        dimensions = DataCubeReaderWDA.removeTrivialDimensions(dimensions);

        // We expect
        // the dimensions should be as above
        List<String> EXPECTED_DIMENSIONS = new ArrayList<String>(Arrays.asList("Geography", "NACE07", "Prodcom Breakdown", "Time Period"));
        List<String> dimensionNames = new ArrayList<>();
        for(DataDimension dimension: dimensions) {
            dimensionNames.add(dimension.name);
        }

        Object [] sorted = dimensionNames.toArray();
        Arrays.sort(sorted);

        assertArrayEquals(EXPECTED_DIMENSIONS.toArray(), sorted);
    }

    @Test
    public void shouldReturnDataCubeValues() throws IOException, URISyntaxException {
        // Given...
        // a test file
        String path = smallPath;

        // When...
        // we load a cube
        DataCube cube = DataCubeReaderWDA.readFile(path);

        // Expect...
        // a non null result
        assertNotNull(cube);

        HashMap<String, String> cubeIndex = new HashMap<>();
        cubeIndex.put("Geography", "Middle");
        cubeIndex.put("Time Period", "2013");
        cubeIndex.put("NACE07", "27904010 - Particle accelerators (Kilogram)");
        cubeIndex.put("Prodcom Breakdown", "Value");

        Double value = cube.get(cubeIndex);
        Double expected = 1239.0;

        assertEquals(expected, value);
    }

    @Test
    public void shouldReturnDataCubeTags() throws IOException, URISyntaxException {
        // Given...
        // a test file
        String path = smallPath;

        // When...
        // we load a cube
        DataCube cube = DataCubeReaderWDA.readFile(path);

        // Expect...
        // a non null result
        assertNotNull(cube);

        // And an expected
        HashMap<String, String> cubeIndex = new HashMap<>();
        cubeIndex.put("Geography", "Middle");
        cubeIndex.put("Time Period", "2012");
        cubeIndex.put("NACE07", "3011 - Building of ships and floating structures");
        cubeIndex.put("Prodcom Breakdown", "Value");

        String value = cube.tag(cubeIndex);
        String expected = "XXX";

        assertEquals(expected, value);
    }

    @Test
    public void shouldSaveCubeNameFromFile() throws IOException, URISyntaxException {
        // Given...
        // a test file
        String path = smallPath;

        // When...
        // we load a cube
        DataCube cube = DataCubeReaderWDA.readFile(path);

        // Expect...
        // a non null result
        assertEquals("smallWDA", cube.name);
    }

    @Test
    public void shouldReturnBigDataCube() throws IOException, URISyntaxException {
        // Given...
        // a test file
        String path = bigPath;

        // When...
        // we load a cube
        long startTime = System.nanoTime();
        System.out.println("Reading 180 megs");

        DataCube cube = DataCubeReaderWDA.readFile(path);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Completed in " + duration + " milliseconds");
        // Expect...
        // a non null result
        assertNotNull(cube);
    }


}