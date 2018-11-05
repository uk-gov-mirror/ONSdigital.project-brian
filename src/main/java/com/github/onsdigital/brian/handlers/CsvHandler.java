package com.github.onsdigital.brian.handlers;

import com.github.davidcarboni.cryptolite.Keys;
import com.github.onsdigital.brian.readers.DataSetReaderCSV;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.List;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class CsvHandler implements Route {

    private TimeSeriesService service;
    private FileUploadHelper fileUploadHelper;

    public CsvHandler(FileUploadHelper fileUploadHelper, TimeSeriesService service) {
        this.fileUploadHelper = fileUploadHelper;
        this.service = service;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        SecretKey key = Keys.newSecretKey();
        Path dataFile = fileUploadHelper.getFileUploadPath(request.raw(), key);

        List<TimeSeries> timeSeries = service.convert(dataFile, new DataSetReaderCSV(), key);
        logEvent().info("handle CSV request completed successfully");
        return timeSeries;
    }
}
