package com.github.onsdigital.brian.handlers;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.publishers.TimeSeriesPublisher;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TimeSeriesConverter {

    public List<TimeSeries> convert(Path dataFile, DataSetReader reader, SecretKey secretKey)
            throws IOException, BadRequestException {
        if (dataFile != null) {
            List<TimeSeries> convertedSeries = getTimeSeries(reader, secretKey, dataFile);
            return convertedSeries;
        }
        throw new BadRequestException("data file required but was null");
    }

    /**
     * Get the timeseries list from a data file
     *
     * @param reader   the reader used to extract a dataset
     * @param key      the secretkey used to encrypt the data file on disk
     * @param dataFile the datafile
     * @return
     * @throws IOException
     */
    private List<TimeSeries> getTimeSeries(DataSetReader reader, SecretKey key, Path dataFile) throws IOException {

        // Convert datafile to dataSet using favoured reader
        TimeSeriesDataSet timeSeriesDataSet = reader.readFile(dataFile, key);

        // Extract as brian timeseries
        List<TimeSeriesObject> brianSeries = new ArrayList<TimeSeriesObject>(timeSeriesDataSet.timeSeries.values());

        // Convert to zebedee timeseries
        List<TimeSeries> contentSeries = TimeSeriesPublisher.convertToContentLibraryTimeSeriesList(brianSeries);

        return contentSeries;
    }
}
