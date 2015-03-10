package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.readers.TimeSeriesReaderCSV;

import java.io.IOException;

/**
 * Created by thomasridd on 09/03/15.
 */

public class Root implements Startup {

    static DataSet master;

    @Override
    public void init() {
        try {
            master = TimeSeriesReaderCSV.readSingleTableDataSet("/imports/Published.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Startup");
        System.out.println(master);
    }
}
