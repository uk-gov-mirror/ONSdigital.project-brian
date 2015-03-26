package com.github.onsdigital.data;

import com.github.onsdigital.api.Data;

import java.util.HashMap;

/**
 * Created by thomasridd on 26/03/15.
 */
public class DataCube {
    HashMap<String, DataDimension> dimensions = new HashMap<>();
    Double values[] = null;

    public void addValue(HashMap<String, String> dimensionValues, double value) {

    }

    public double getValue(HashMap<String, String> dimensions) {
        return 1;
    }

    public void setDimension(DataDimension dimension) {
        dimensions.put(dimension.name, dimension);
    }

}
