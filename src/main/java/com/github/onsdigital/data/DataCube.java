package com.github.onsdigital.data;

import com.github.onsdigital.api.Data;

import java.util.HashMap;

/**
 * Created by thomasridd on 26/03/15.
 *
 * Datacube is structured as an implementation of JSON-STAT
 */
public class DataCube {
    public HashMap<String, DataDimension> dimensions = new HashMap<>();
    double [] values;
    String [] tags;

    /**
     * Adds a value to the datacube using its position
     *
     * @param dimensionValues
     * @param value
     */
    public void put(HashMap<String, String> dimensionValues, double value) {
        int index = indexFor(dimensionValues);
        values[index] = value;
        tags[index] = "";
    }
    public void put(HashMap<String, String> dimensionValues, double value, String tag) {
        int index = indexFor(dimensionValues);
        values[index] = value;
        tags[index] = tag;
    }

    public double get(HashMap<String, String> dimensionValues) {
        return values[indexFor(dimensionValues)];
    }

    public String tag(HashMap<String, String> dimensionValues) {
        return tags[indexFor(dimensionValues)];
    }

    private int indexFor(HashMap<String, String> dimensionValues) {
        int unit = 1;
        int index = 0;
        for(String key: dimensionValues.keySet()) {
            DataDimension dimension = dimensions.get(key);
            String dimensionValue = dimensionValues.get(key);

            int subIndex = dimension.values.get(dimensionValue);
            index += subIndex * unit;
            unit *= dimension.values.size();
        }
        return index;
    }


    public DataCube(HashMap<String, DataDimension> dimensions) {
        this.dimensions = dimensions;

        int size = 1;
        for(String key: dimensions.keySet()) {
            size = size * dimensions.get(key).values.size();
        }

        values = new double[size];
        tags = new String[size];
    }



}
