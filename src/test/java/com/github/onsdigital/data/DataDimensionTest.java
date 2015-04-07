package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.DataDimension;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataDimensionTest {
    static String[] values = {"Mr", "Sandman", "bring", "me", "a", "dream"};

    @Test
    public void testIndexOf() throws Exception {
        // When
        // I create a new Dimension
        DataDimension dimension = new DataDimension("dimension");
        for(String value: values) {
            dimension.addValue(value);
        }

        // We expect
        // The dimension to return to return the index of each value
        for(int i = 0; i < values.length; i++) {
            assertEquals(i, dimension.indexOf(values[i]));
        }
    }

    @Test
    public void testNoDuplication() throws Exception {
        // Given a new Dimension
        DataDimension dimension = new DataDimension("dimension");
        for(String value: values) {
            dimension.addValue(value);
        }

        // When I add I repeat value
        dimension.addValue("Sandman");

        // I expect
        // The dimension not to expand
        assertEquals(dimension.values.size(), values.length);

        // And the indices to remain the same
        for(int i = 0; i < values.length; i++) {
            assertEquals(i, dimension.indexOf(values[i]));
        }
    }

}