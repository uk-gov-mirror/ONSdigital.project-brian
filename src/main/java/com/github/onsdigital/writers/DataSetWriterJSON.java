package com.github.onsdigital.writers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomasridd on 11/03/15.
 */
public class DataSetWriterJSON {

    public static String dataSetAsJSON(DataSet dataSet, boolean prettyPrinting) {
        ArrayList<HashMap<String, String>> ds = new ArrayList<>();
        for(TimeSeries series: dataSet.timeSeries.values()) {
            HashMap<String, String> s = new HashMap<>();
            s.put("taxi", series.taxi);
            s.put("name", series.name.trim());
            ds.add(s);
        }

        if(prettyPrinting == true) {
            Serialiser.getBuilder().setPrettyPrinting();
        }
        return Serialiser.serialise(ds);
    }
}
