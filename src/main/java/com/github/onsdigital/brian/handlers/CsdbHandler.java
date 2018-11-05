package com.github.onsdigital.brian.handlers;

import com.github.davidcarboni.cryptolite.Keys;
import com.github.onsdigital.brian.readers.DataSetReaderCSDB;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.List;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class CsdbHandler implements Route {

    private TimeSeriesService service;
    private FileUploadHelper fileUploadHelper;

    public CsdbHandler(FileUploadHelper fileUploadHelper, TimeSeriesService service) {
        this.fileUploadHelper = fileUploadHelper;
        this.service = service;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        SecretKey key = Keys.newSecretKey();
        Path uploadFilePath = fileUploadHelper.getFileUploadPath(request.raw(), key);
        List<TimeSeries> timeSeries = service.convert(uploadFilePath, new DataSetReaderCSDB(), key);
        logEvent().info("handle CSDB request completed successfully");
        return timeSeries;
    }
}

