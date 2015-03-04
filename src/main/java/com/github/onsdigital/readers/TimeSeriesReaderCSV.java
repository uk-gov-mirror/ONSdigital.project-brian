package com.github.onsdigital.readers;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.TimeSeriesPoint;
import com.github.onsdigital.data.TimeSeriesTable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by Tom.Ridd on 03/03/15.
 */


public class TimeSeriesReaderCSV {
    static final String META_WORKBOOK_NAME = "workbook name";
    static final String META_WORKSHEET_NAME = "worksheet name";
    static final String META_TABLE_NAME = "table name";
    static final String META_TABLE_CODE = "table code";

    static final String SERIES_NAMES = "series names";
    static final String SERIES_TAXIS = "series ids";


    /**
     * Returns a DataSet from a .csv spreadsheet
     * <p/>
     * Dataset contains multiple time series and a single table
     *
     * @param resourceName The internal source path
     * @return The constructed dataset
     */
    public static DataSet readSingleTableDataSet(String resourceName) throws IOException {
        // 1. Read from the source
        Csv source = new Csv(resourceName);
        source.read(0);

        DataSet dataset = new DataSet();
        TimeSeriesTable table = new TimeSeriesTable();

        int rowNumber = 0;

        // SECTION ONE :- READ META DATA VALUES
        // GO THROUGH THE FIRST ROWS OF THE TABLE TO SEARCH FOR METADATA
        // TODO ERROR CATCHING AND HANDLING
        boolean findMetaData = true;
        while (findMetaData & (rowNumber < source.size())) {
            String[] row = source.row(rowNumber);
            String meta = StringUtils.lowerCase(row[0]);

            if (meta.equals(META_TABLE_CODE)) {
                table.taxi = row[1];
                rowNumber += 1;
            } else if (meta.equals(META_TABLE_NAME)) {
                table.name = row[1];
                rowNumber += 1;
            } else if (meta.equals(META_WORKBOOK_NAME)) {
                table.source = row[1];
                rowNumber += 1;
            } else if (meta.equals(META_WORKSHEET_NAME)) {
                table.source2 = row[1];
                rowNumber += 1;
            } else if (meta.equals("")) {
                rowNumber += 1;
            } else {
                findMetaData = false;
            }
        }

        // TODO CREATE DEFAULT VALUES FOR MISSING DATA

        // READ TIME SERIES
        int seriesCount = source.row(rowNumber).length - 1;
        for (int seriesColumn = 1; seriesColumn <= seriesCount; seriesColumn++) {
            TimeSeries series = new TimeSeries();

            for (int seriesRow = rowNumber; seriesRow < source.size(); seriesRow++) {
                String[] row = source.row(seriesRow);
                String rowTitle = StringUtils.lowerCase(row[0]);

                if (rowTitle.equals(SERIES_NAMES)) {
                    series.name = row[seriesColumn];
                } else if (rowTitle.equals(SERIES_TAXIS)) {
                    series.taxi = row[seriesColumn];
                } else {
                    String data = row[seriesColumn];
                    TimeSeriesPoint point = new TimeSeriesPoint(rowTitle, data);
                    series.data.add(point);
                }
            }

            table.series.add(series);
            dataset.timeSeries.add(series);
        }


        dataset.timeTables.add(table);
        return dataset;
    }

    public static void main(String[] args) throws IOException {
        DataSet dataset = TimeSeriesReaderCSV.readSingleTableDataSet("/imports/Construction 1.csv");

        try {
            assert dataset.timeTables.size() > 0 : "table not read";
            assert dataset.timeTables.get(0).taxi.equals("BUS.CON.001") : "reading all fine";
        } catch (AssertionError e) {
            System.out.println(e.toString());
        }

    }

}
