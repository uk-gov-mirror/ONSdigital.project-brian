package com.github.onsdigital.api;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.Manifest;
import com.github.onsdigital.data.Mapping;
import com.github.onsdigital.readers.TimeSeriesReaderCSV;
import com.sun.tools.corba.se.idl.constExpr.Times;

import java.io.IOException;

/**
 * Created by thomasridd on 06/03/15.
 */
public class testMethods {


    public static void main(String[] args) throws IOException {
        DataSet published = TimeSeriesReaderCSV.readSingleTableDataSet("/imports/Published.csv");
        DataSet updates = TimeSeriesReaderCSV.readSingleTableDataSet("/imports/Updates.csv");
        Mapping mapping = Mapping.getSuggestedMapping(updates, published);
        Manifest manifest = new Manifest(mapping);
    }
}
