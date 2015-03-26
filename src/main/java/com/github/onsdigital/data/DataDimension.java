package com.github.onsdigital.data;

import java.util.HashMap;

/**
 * Created by thomasridd on 26/03/15.
 *
 * DataDimension provides an easy mechanism to convert
 */
public class DataDimension {

    public HashMap<String, Integer> values = new HashMap<>();
    public String name = null;

    public DataDimension(String dimension) {
        this.name = dimension;
    }

    public void addValue(String value) {
        if(values.containsKey(value) == false) {
            values.put(value, values.size());
        }
    }

    public int indexOf(String value) {
        return values.get(value);
    }
}
