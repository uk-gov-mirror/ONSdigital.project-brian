package com.github.onsdigital.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.github.onsdigital.async.TableProcessor;
import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.data.TimeSeriesDataSet;
import com.github.onsdigital.data.objects.DataCubeSet;
import com.github.onsdigital.data.objects.DataDimension;
import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 30/03/15.
 */
public class DataCubeReaderTable {
    public static String defaultValue = "All";

    /**
     * Reads a datacube from file
     *
     * @param filePath - the file
     * @return - the datacube
     * @throws IOException
     */
    public static DataCube readFile(Path filePath) throws IOException, URISyntaxException {

        //
        List<String> headers = readDimensionNamesFromHeader(filePath); // Read dimension names
        List<Integer> indices = readDimensionIndicesFromHeader(filePath); // Determine dimension indices
        List<DataDimension> dimensions = readDimensionsFromStream(filePath, indices, headers, true); // Construct dimensions
        dimensions = removeTrivialDimensions(dimensions); // Remove irrelevant dimensions

        HashMap<String, DataDimension> dimensionHashMap = new HashMap<>(); // Build the cube object
        for(DataDimension dimension: dimensions) { dimensionHashMap.put(dimension.name, dimension); }
        DataCube cube = new DataCube(dimensionHashMap);

        setCubeName(cube, filePath); // Grab name from filePath

        cube = readValuesToCube(cube, filePath, true); // Read values

        return cube;
    }


    public static DataCube readFile(String resourceName) throws IOException, URISyntaxException {
        URL resource = TimeSeriesDataSet.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        return readFile(filePath);
    }

    public static void setCubeName(DataCube cube, Path filePath) {
        cube.name = FilenameUtils.getBaseName(filePath.toString());
    }

    /**
     * Once the cube has been set up with dimensions loads the actual values
     *
     * @param cube - A cube with correctly formatted dimensions
     * @param stream - A csv file stream
     * @param hasHeader
     * @return
     * @throws IOException
     */
    static DataCube readValuesToCube(DataCube cube, CSVReader stream, boolean hasHeader) throws IOException {
        if(hasHeader) { stream.readNext(); } // remove header
        String [] cells = stream.readNext();

        int valueIndex = cells.length - 1;

        while(cells != null) {
            try {
                HashMap<String, String> cubeIndex = new HashMap<>();
                for (DataDimension dimension : cube.dimensions.values()) {
                    if(cells[dimension.tag].equals("")) {
                        cubeIndex.put(dimension.name, defaultValue);
                    } else {
                        cubeIndex.put(dimension.name, cells[dimension.tag]);
                    }
                }

                if(cells[valueIndex].equals("") == false) { // case where the value is numeric
                    double value = Double.parseDouble(cells[valueIndex]);
                    cube.put(cubeIndex, value, "");
                }
                else {
                    cube.put(cubeIndex, 0.0, "nan");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            } catch (NumberFormatException e) {
            }
            cells = stream.readNext();
        }
        return cube;
    }
    static DataCube readValuesToCube(DataCube cube, String resourceName, boolean hasHeader) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
            return readValuesToCube(cube, stream, hasHeader);
        }
    }
    static DataCube readValuesToCube(DataCube cube, Path filePath, boolean hasHeader) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(filePath)) {
            return readValuesToCube(cube, stream, hasHeader);
        }
    }
    /**
     * Convenience method for opening files using resource names
     * @param resourceName
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    static CSVReader openStream(String resourceName) throws IOException, URISyntaxException {
        // FIRST THINGS FIRST - GET THE FILE
        URL resource = TimeSeriesDataSet.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        return new CSVReader(new FileReader(filePath.toFile()));
    }
    static CSVReader openStream(Path filePath) throws FileNotFoundException {
        return new CSVReader(new FileReader(filePath.toFile()));
    }

    /**
     *
     * @param resourceName
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    static List<Integer> readDimensionIndicesFromHeader(String resourceName) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
            return readDimensionIndicesFromHeader(stream);
        }
    }
    static List<Integer> readDimensionIndicesFromHeader(Path filePath) throws IOException {
        try(CSVReader stream = openStream(filePath)) {
            return readDimensionIndicesFromHeader(stream);
        }
    }
    static List<Integer> readDimensionIndicesFromHeader(CSVReader stream) throws IOException {

        // Add standard fields
        List<Integer> dimensionIndices = new ArrayList<>();

        String [] cells = stream.readNext();
        for(Integer i = 0; i < cells.length - 1; i++) {
            dimensionIndices.add(i);
        }

        return dimensionIndices;
    }

    /**
     * Identify the columns
     * @param stream
     * @return
     * @throws IOException
     */
    static List<String> readDimensionNamesFromHeader(CSVReader stream) throws IOException {
        // Extract dimension names from the header

        String IS_NAME = "dimension_Label_Eng";

        // Add standard fields
        List<String> dimensionNames = new ArrayList<>();
        String [] headers = stream.readNext(); // Header line

        for(int i = 0; i < headers.length - 1; i++) { dimensionNames.add(headers[i]); }

        return dimensionNames;
    }
    static List<String> readDimensionNamesFromHeader(String resourceName) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
            return readDimensionNamesFromHeader(stream);
        }
    }
    static List<String> readDimensionNamesFromHeader(Path filePath) throws IOException {
        try(CSVReader stream = openStream(filePath)) {
            return readDimensionNamesFromHeader(stream);
        }
    }

    /**
     * Creates dimensions by walking through massive files to find unique values
     *
     * @param stream a csv stream to read from
     * @param indices the cell indices of the dimensions
     * @param names the names for the dimensions
     * @param hasHeader true if the stream has a row at the top which needs ignoring
     * @return
     * @throws IOException
     */
    static List<DataDimension> readDimensionsFromStream(CSVReader stream, List<Integer> indices, List<String> names, boolean hasHeader) throws IOException {
        if(hasHeader) { stream.readNext(); }

        // Create the new dimensions
        List<DataDimension> dimensions = new ArrayList<>();

        for(int i = 0; i < indices.size(); i++) {
            DataDimension dimension = new DataDimension(names.get(i));
            dimension.tag = indices.get(i);

            dimensions.add(dimension);
        }

        String [] cells = stream.readNext();
        // Walk through the file
        while(cells != null)
        {
            for(int i = 0; i < indices.size(); i++) {
                int index = indices.get(i);
                if((index < cells.length) ) {
                    if (cells[index].equals("")) {
                        dimensions.get(i).addValue(defaultValue);
                    } else {
                        dimensions.get(i).addValue(cells[index]);
                    }
                }
            }

            cells = stream.readNext();
        }
        return dimensions;
    }
    static List<DataDimension> readDimensionsFromStream(String resourceName, List<Integer> indices, List<String> names, boolean hasHeader) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
            return readDimensionsFromStream(stream, indices, names, hasHeader);
        }
    }
    static List<DataDimension> readDimensionsFromStream(Path filePath, List<Integer> indices, List<String> names, boolean hasHeader) throws IOException {
        try(CSVReader stream = openStream(filePath)) {
            return readDimensionsFromStream(stream, indices, names, hasHeader);
        }
    }

    /**
     * Removes dimensions that have 1 or 0 values
     *
     * @param originalDimensions
     * @return
     */
    static List<DataDimension> removeTrivialDimensions(List<DataDimension> originalDimensions) {
        List<DataDimension> nonTrivial = new ArrayList<>();
        for(DataDimension dimension: originalDimensions) {
            if(dimension.values.size() > 1) {
                nonTrivial.add(dimension);
            }
        }
        return nonTrivial;
    }

    /**
     * Creates a set of datacube promises from
     *
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static DataCubeSet readDirectory(String resourceName) throws IOException, URISyntaxException {

        // Get the path
        URL resource = DataCubeSet.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        DataCubeSet dataCubeSet = new DataCubeSet();

        // Processors exist to walk through each file
        List<TableProcessor> processors = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath)){ // NOW LOAD THE FILES
            for(Path entry: stream) {
                // System.out.println("Creating processor for " + entry);
                TableProcessor processor = new TableProcessor(entry);

                String cubeName = FilenameUtils.getBaseName(entry.toString());

                // Process each dataset
                dataCubeSet.cubes.put(cubeName, processor.getCube());

                // Add the processor our list of jobs to be completed
                processors.add(processor);
            }
        }

        return dataCubeSet;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        DataCubeSet dataCubeSet = readDirectory("/imports/tabular/");

        long startTime = System.nanoTime();
        System.out.println("Reading 4 files of 400 megs");

        for(Future<DataCube> dataCubeFuture: dataCubeSet.cubes.values()){
            DataCube cube = dataCubeFuture.get();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Completed in " + duration + " milliseconds");

        TableProcessor.shutdown();

    }
}
