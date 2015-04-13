package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.data.DataCubeQuery;
import com.github.onsdigital.data.objects.DataCubeSet;
import com.github.onsdigital.data.objects.DataDimension;
import org.apache.http.NameValuePair;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 01/04/15.
 */
public class DataCubeWriterJSON {

    /**
     * Converts a dataCubeSet to a JSON object with specific use as a summary object
     *
     * For each data cube includes
     * 1. name
     * 2. list of dimension names
     *
     * @param dataCubeSet
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String dataCubeSetAsJSON(DataCubeSet dataCubeSet) throws ExecutionException, InterruptedException {
        List<Object> cubes = new ArrayList<>();

        // For each datacube
        for(String key: dataCubeSet.cubes.keySet()) {

            DataCube cube = dataCubeSet.cubes.get(key).get();

            HashMap<String, Object> cubeObject = new HashMap<>();

            cubeObject.put("name", cube.name);

            List<String> dimensions = new ArrayList<>();
            for(DataDimension dimension: cube.dimensions.values()) {
                dimensions.add(dimension.name);
            }
            cubeObject.put("dimensions", dimensions);

            cubes.add(cubeObject);
        }

        HashMap<String, Object> cubeSet = new HashMap<>();
        cubeSet.put("cubes", cubes);

        return Serialiser.serialise(cubeSet);
    }

    public static String dataCubeSummaryAsJSON(DataCube dataCube) {
        HashMap<String, Object> cube = new HashMap<>();

        // Add the name
        cube.put("name", dataCube.name);

        // For each dimension
        List<Object> dimensions = new ArrayList<>();
        for(DataDimension dimension: dataCube.dimensions.values()) {

            // Create an object
            HashMap<String, Object> dimensionObj = new HashMap<>();

            dimensionObj.put("name", dimension.name); // Add the name

            List<String> values = new ArrayList<>(); // Add the list of values
            for(String value: dimension.values.keySet()) {
                values.add(value);
            }
            Collections.sort(values);

            dimensionObj.put("values", values);

            dimensions.add(dimensionObj);
        }
        cube.put("dimensions", dimensions);

        return Serialiser.serialise(cube);
    }

    public static String dataCubeAsJSON(DataCube dataCube) {
        return Serialiser.serialise(dataCube);
    }

    public static String dataCubeSlicedAsJSON(DataCube dataCube, List<NameValuePair> slice) {
        DataCubeQuery dataCubeQuery = new DataCubeQuery(dataCube);

        for(NameValuePair pair: slice) {
            dataCubeQuery.addFilter(pair.getName(), pair.getValue());
        }
        DataCube cube = dataCubeQuery.run();

        return dataCubeAsJSON(cube);
    }
}
