package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.DataDimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomasridd on 01/04/15.
 *
 * Builds a slice by specifying filters in terms of DataDimensions
 */
public class DataCubeQuery {
    DataCube original = null;
    DataCube result = null;

    HashMap<String, DataDimension> filters = new HashMap<>();
    HashMap<String, String> fixed = new HashMap<>();

    /**
     * Initialise
     *
     * @param original - the cube we are going to query
     */
    public DataCubeQuery(DataCube original) {
        this.original = original;
    }

    /**
     * Filters added to the query
     *
     * If a dimension is given with limited values the filter will strip down to those values
     *
     * By default all values from dimensions not included are included
     *
     * @param filter
     */
    public void addFilter(DataDimension filter) {
        filters.put(filter.name, filter);
    }
    public void addFilter(String dimensionName, String filterValue) {
        DataDimension dimension = new DataDimension(dimensionName);
        dimension.addValue(filterValue);

        addFilter(dimension);
    } // convenience method for creating quick slice by single value
    public void addFilter(String dimensionName, List<String> filterValues) {
        DataDimension dimension = new DataDimension(dimensionName);
        dimension.addValues(filterValues.toArray(new String[filterValues.size()]));

        addFilter(dimension);
    } // convenience method for creating quick slice by String list

    public DataCube run() {
        createNewDataCube(); // Begin by setting up the new data cube
        return fillNewDataCube();
    }

    /**
     * Step one of the run() procedure
     *
     * Takes filter dimensions and creates an appropriate DataCube Object
     *
     * @return
     */
    DataCube createNewDataCube() {

        HashMap<String, DataDimension> dimensions = new HashMap<>();

        // Iterate through our original dimensions
        for(String key: original.dimensions.keySet()) { // If we have set a filter for the item either ...
            if( filters.containsKey(key) ) {
                if( filters.get(key).values.size() > 1) { // Add the filtered dimension for a cut down choice
                    dimensions.put(key, filters.get(key));
                } else { // or totally ignore the dimension if it is a thin slice.
                    DataDimension slice = filters.get(key);
                    for(String value: slice.values.keySet()) {
                        fixed.put(key, value);
                    }
                }
            } else {
                dimensions.put(key, original.dimensions.get(key)); // If no filtration then add the original dimension
            }
        }

        result = new DataCube(dimensions); // There's no particular reason to return but it is good to test
        return result;
    }
    /**
     * Step two of the run() procedure
     *
     * Populates the cube from step one with data
     *
     * @return
     */
    DataCube fillNewDataCube() {
        // Initialise index
        HashMap<String, String> index = new HashMap<>();

        // Initialise index fixed dimensions
        for(String key: fixed.keySet()) { index.put(key, fixed.get(key)); }

        // Initialise arrays for free dimensions
        List<String> dimNames = new ArrayList<>();
        List<List<String>> dimValues = new ArrayList<>();
        for(DataDimension dimension: result.dimensions.values()) {
            dimNames.add(dimension.name);
            dimValues.add(new ArrayList<String>(dimension.values.keySet()));
        }

        // Initialise index for free dimensions
        for(int i = 0; i < dimNames.size(); i++) {
            index.put(dimNames.get(i), dimValues.get(i).get(0));
        }

        // Initialise walk variables
        int [] position = new int[dimNames.size()];

        int [] maxPosition = new int[dimNames.size()];
        for(int i = 0; i < dimValues.size(); i++) { maxPosition[i] = dimValues.get(i).size() - 1; }

        int curDigit = 0;
        boolean stop = false;

        while(stop == false) {
            // Set the result
            result.put(index, original.get(index));

            stop = true;

            innerloop:
            for (int digit = 0; digit < position.length; digit++ ) {
                if( position[digit] < maxPosition[digit] ) {
                    position[digit]++;
                    stop = false;
                } else {
                    position[digit] = 0;
                }

                index.put(dimNames.get(digit), dimValues.get(digit).get(position[digit]));

                if(stop == false) {break innerloop;}
            }
        }

        return result;
    }
}
