package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.readers.DataSetReaderCSDB;

import java.io.IOException;

/**
 * Created by thomasridd on 09/03/15.
 */

public class Root implements Startup {

    public static DataSet master;

    @Override
    public void init() {
        try {
//            master = DataSetReaderCSDB.readDirectory("/imports/csdb");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Startup");
    }

    //TODO Replace this with code worth the effort
    public static TimeSeries getTimeSeries(String taxi) {
        return master.timeSeries.get(taxi);
    }
}
