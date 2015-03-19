package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.generators.Sample;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomasridd on 11/03/15.
 */
public class DataSetWriterJSON {

    public static String dataSetAsJSON(DataSet dataSet, boolean prettyPrinting) {
        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(dataSet);
    }

    public static void main(String[] args) {
        DataSet dataSet = Sample.quickWalks(10, 1);
        System.out.println(dataSetAsJSON(dataSet,true));
    }

}

