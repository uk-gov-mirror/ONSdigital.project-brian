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

    private TimeSeriesConverter converter;
    private FileUploadHelper fileUploadHelper;
    private Supplier<SecretKey> encryptionKeySupplier;
    private DataSetReader dataSetReader;

    /**
     * Constuct a new CSDB API handler.
     *
     * @param fileUploadHelper      a helper to take care of getting the uploaded file from the HTTP request.
     * @param converter             the services that generates the Timesseries from the CSDB file.
     * @param encryptionKeySupplier provides {@link SecretKey} to use.
     * @param dataSetReader         a parser responsible for reading the uploaded CSDB file.
     */
    public CsdbHandler(FileUploadHelper fileUploadHelper, TimeSeriesConverter converter,
                       Supplier<SecretKey> encryptionKeySupplier, DataSetReader dataSetReader) {
        this.fileUploadHelper = fileUploadHelper;
        this.converter = converter;
        this.encryptionKeySupplier = encryptionKeySupplier;
        this.dataSetReader = dataSetReader;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        SecretKey key = encryptionKeySupplier.get();
        Path uploadFilePath = fileUploadHelper.getFileUploadPath(request.raw(), key);

        List<TimeSeries> timeSeries = converter.convert(uploadFilePath, dataSetReader, key);
        logEvent().path(uploadFilePath).info("handle CSDB request completed successfully");
        return timeSeries;
    }
}

