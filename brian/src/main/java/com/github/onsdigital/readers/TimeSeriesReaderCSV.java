package com.github.onsdigital.readers;

import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;
import com.github.onsdigital.data.objects.TimeSeriesTable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;

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

    static final String FLAT_FILE_NAME = "numbers.json";

    /**
     * Returns a DataSet from a .csv spreadsheet
     * <p/>
     * Dataset contains multiple time series and a single table
     *
     * @param resourceName The internal source path
     * @return The constructed dataset
     */
    public static DataSet readFile(String resourceName) throws IOException {
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
                dataset.source = row[1];
                dataset.name = row[1];
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
                    try {
                        if(data.equals("") == false) { // IGNORE MISSING VALUES (FOR NOW)
                            TimeSeriesPoint point = new TimeSeriesPoint(rowTitle, data);
                            series.addPoint(point);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            //TODO: FILL IN THE BLANKS (BETWEEN TIME SERIES START AND END DATES)

            table.addSeries(series);
            dataset.timeSeries.put(series.taxi, series);
        }

        /*
         * ADD TABLE - (BUT ONLY IF THE DATE COLUMN CHECKS OUT)
         */
        //TODO APPROPRIATE ERROR CATCHING

        dataset.timeTables.add(table);


        return dataset;
    }


    /**
     * Returns a DataSet from a .csv spreadsheet
     * <p/>
     * Dataset contains multiple time series and a single table
     *
     * @param resourcePath The internal source path
     * @return The constructed dataset
     */
    public static DataSet readFlatFileDataSet(String resourcePath) throws IOException {
        return null;
    }

    public static void main(String[] args) throws IOException {
        DataSet dataset = TimeSeriesReaderCSV.readFile("/imports/Published.csv");

        try {
            assert dataset.timeTables.size() > 0 : "table not read";
            assert dataset.timeTables.get(0).taxi.equals("BUS.CON.001") : "reading all fine";
        } catch (AssertionError e) {
            System.out.println(e.toString());
        }

    }

}
