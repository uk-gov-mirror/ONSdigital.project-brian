package com.github.onsdigital.data.objects;

import java.util.HashMap;

/**
 * Created by thomasridd on 26/03/15.
 *
 * DataDimension provides an easy mechanism to convert
 */
public class DataDimension {

    public HashMap<String, Integer> values = new HashMap<>();
    public String name = null;
    public int tag = 0;

    public DataDimension(String dimension) {
        this.name = dimension;
    }
    public DataDimension(String dimension, String someValues[]) {
        this.name = dimension;
        this.addValues(someValues);
    }

    public void addValue(String value) {
        if(values.containsKey(value) == false) {
            values.put(value, values.size());
        }
    }
    public void addValues(String [] addValues) {
        for(String value: addValues) {
            if (values.containsKey(value) == false) {
                values.put(value, values.size());
            }
        }
    }

    public int indexOf(String value) {
        return values.get(value);
    }
}
