package com.github.onsdigital.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.data.DataDimension;
import com.github.onsdigital.data.DataSet;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomasridd on 30/03/15.
 */
public class DataCubeReaderWDA {


    /**
     * Reads a datacube from file
     *
     * @param resourceName - the file
     * @return - the datacube
     * @throws IOException
     */
    public static DataCube readFile(String resourceName) throws IOException, URISyntaxException {

        //
        List<String> headers = readDimensionNamesFromHeader(resourceName); // Read dimension names
        List<Integer> indices = readDimensionIndicesFromHeader(resourceName); // Determine dimension indices
        List<DataDimension> dimensions = readDimensionsFromStream(resourceName, indices, headers, true); // Construct dimensions
        dimensions = removeTrivialDimensions(dimensions); // Remove irrelevant dimensions

        HashMap<String, DataDimension> dimensionHashMap = new HashMap<>();
        for(DataDimension dimension: dimensions) { dimensionHashMap.put(dimension.name, dimension); }

        DataCube cube = new DataCube(dimensionHashMap);
        cube = readValuesToCube(cube, resourceName, true);

        return cube;
    }

    static DataCube readValuesToCube(DataCube cube, CSVReader stream, boolean hasHeader) throws IOException {
        if(hasHeader) { stream.readNext(); } // remove header
        String [] cells = stream.readNext();

        while(cells != null) {
            try {
                HashMap<String, String> cubeIndex = new HashMap<>();
                for (DataDimension dimension : cube.dimensions.values()) {
                    cubeIndex.put(dimension.name, cells[dimension.tag]);
                }

                String tag = cells[1];
                if(cells[0].equals("") == false) { // case where the value is numeric
                    double value = Double.parseDouble(cells[0]);
                    cube.put(cubeIndex, value, tag);
                }
                else {
                    cube.put(cubeIndex, 0.0, tag);
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

    static CSVReader openStream(String resourceName) throws IOException, URISyntaxException {
        // FIRST THINGS FIRST - GET THE FILE
        URL resource = DataSet.class.getResource(resourceName);
        Path filePath = Paths.get(resource.toURI());

        return new CSVReader(new FileReader(filePath.toFile()));
    }

    static List<Integer> readDimensionIndicesFromHeader(String resourceName) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
            return readDimensionIndicesFromHeader(stream);
        }
    }
    static List<Integer> readDimensionIndicesFromHeader(CSVReader stream) throws IOException {

        //
        String IS_INDEX = "dimension_Item_Label_Eng";

        // Add standard fields
        List<Integer> dimensionIndices = new ArrayList<>();
        dimensionIndices.add(14); // Geography
        dimensionIndices.add(18); // Time
        dimensionIndices.add(23); // Statistical Population

        String [] cells = stream.readNext();
        for(Integer i = 24; i < cells.length; i++) {
            if( cells[i].contains(IS_INDEX) ) {
                dimensionIndices.add(i);
            }
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
        // Extract anything that looks like a column heading from the header

        String IS_NAME = "dimension_Label_Eng";

        // Add standard fields
        List<String> dimensionNames = new ArrayList<>();
        dimensionNames.add("Geography");
        dimensionNames.add("Time Period");
        dimensionNames.add("Statistical Population");

        String [] headers = stream.readNext(); // Header line
        String [] firstRow = stream.readNext(); // First row

        for(Integer i = 23; i < headers.length; i++) {
            if( headers[i].contains(IS_NAME) ) {
                dimensionNames.add(firstRow[i]);
            }
        }

        return dimensionNames;
    }
    static List<String> readDimensionNamesFromHeader(String resourceName) throws IOException, URISyntaxException {
        try(CSVReader stream = openStream(resourceName)) {
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
                if((index < cells.length) && (cells[index].equals("") == false)) {
                    dimensions.get(i).addValue(cells[index]);
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

}
