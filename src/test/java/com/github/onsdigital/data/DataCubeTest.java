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

        String d1[] = {"Tom", "David", "Kane", "Pastor", "Onkar"};
        String d2[] = {"Travel", "Subsistence", "Housing"};



        DataDimension people = new DataDimension("people", d1);
        DataDimension expenses = new DataDimension("people", d2);

        HashMap<String, DataDimension> dimensions = new HashMap<>();
        dimensions.put("people", people);
        dimensions.put("expenses", expenses);

        this.cube = new DataCube(dimensions);
    }

    @Test
    public void shouldInitialiseWithCorrectSize() throws Exception {
        //Given
        // the example cube from setUp

        //When
        // initialised

        // We expect
        // exactly 15 elements
        assertEquals(15, cube.values.length);
    }

    @Test
    public void putShouldChangeSomething() throws Exception {
        //Given
        // the empty cube from setUp
        HashMap<String, String> index = new HashMap<>();
        index.put("people", "Kane");
        index.put("expenses", "Housing");

        //When
        // we add to that cube

        this.cube.put(index, 1.00);

        // We expect
        // something to be changed
        boolean changed = false;
        for(int i = 0; i < cube.values.length; i++) {
            if(this.cube.values[i] > 0) {
                changed = true;
                break;
            }
        }

        assertEquals(changed, true);
    }

    @Test
    public void getShouldReturnWhatYouPut() throws Exception {
        //Given
        // the empty cube from setUp and an index
        HashMap<String, String> index = new HashMap<>();
        index.put("people", "Kane");
        index.put("expenses", "Housing");

        //When
        // we add to that cube
        double addedValue = 1.00;

        this.cube.put(index, addedValue);

        // We expect
        // something to be changed

        double gotValue = this.cube.get(index);

        assertTrue(addedValue == gotValue);
    }
}