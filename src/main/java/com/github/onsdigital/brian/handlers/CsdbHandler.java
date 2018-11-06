package com.github.onsdigital.brian.handlers;

import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class CsdbHandler implements Route {

    private TimeSeriesService service;
    private FileUploadHelper fileUploadHelper;
    private Supplier<SecretKey> encryptionKeySupplier;
    private DataSetReader dataSetReader;

    public CsdbHandler(FileUploadHelper fileUploadHelper, TimeSeriesService service,
                       Supplier<SecretKey> encryptionKeySupplier, DataSetReader dataSetReader) {
        this.fileUploadHelper = fileUploadHelper;
        this.service = service;
        this.encryptionKeySupplier = encryptionKeySupplier;
        this.dataSetReader = dataSetReader;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        SecretKey key = encryptionKeySupplier.get();
        Path uploadFilePath = fileUploadHelper.getFileUploadPath(request.raw(), key);

        List<TimeSeries> timeSeries = service.convert(uploadFilePath, dataSetReader, key);
        logEvent().path(uploadFilePath).info("handle CSDB request completed successfully");
        return timeSeries;
    }
}

