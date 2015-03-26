package com.github.onsdigital.data;

import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;

import static org.junit.Assert.*;

public class DataCubeTest {
    DataCube cube;

    @Before
    public void setUp() throws Exception {
        cube = new DataCube();


        HashMap<String, String> dimensionValues = new HashMap<>();
        dimensionValues.put("Person", "Pastor");
        dimensionValues.put("Expense", "Lunch");
        dimensionValues.put("Date", "26-03-2015");
        double value = 1.50;
        cube.addValue(dimensionValues, value);

        dimensionValues = new HashMap<>();
        dimensionValues.put("Person", "David");
        dimensionValues.put("Expense", "Travel");
        dimensionValues.put("Date", "26-03-2015");
        value = 100;
        cube.addValue(dimensionValues, value);

        dimensionValues = new HashMap<>();
        dimensionValues.put("Person", "David");
        dimensionValues.put("Expense", "Lunch");
        dimensionValues.put("Date", "26-03-2015");
        value = 4.50;
        cube.addValue(dimensionValues, value);

        dimensionValues = new HashMap<>();
        dimensionValues.put("Person", "Pastor");
        dimensionValues.put("Expense", "Travel");
        dimensionValues.put("Date", "26-03-2015");
        value = 80;
        cube.addValue(dimensionValues, value);
    }

    @Test
    public void testAddValue() throws Exception {

    }
}