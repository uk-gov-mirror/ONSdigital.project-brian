package com.github.onsdigital.brian.handlers;

import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.github.onsdigital.brian.logging.Logger.logEvent;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

        Gson g = new GsonBuilder().setPrettyPrinting().create();
        Path p = Paths.get("/Users/dave/Desktop/modified.json");
        Files.write(p, g.toJson(timeSeries).getBytes());

        //return timeSeries;
        return new ArrayList<TimeSeries>();
    }

    public static void main(String[] args) throws Exception {
        Path existing = Paths.get("/Users/dave/Desktop/original.json");
        Path latest = Paths.get("/Users/dave/Desktop/modified.json");

        try (
                InputStream in = Files.newInputStream(existing);
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader exisingR = new BufferedReader(inputStreamReader);

                InputStream in2 = Files.newInputStream(latest);
                InputStreamReader inputStreamReader2 = new InputStreamReader(in2);
                BufferedReader latestR = new BufferedReader(inputStreamReader2);
        ) {
            int i = 1;

            String existingLine = null;
            String modifiedLine = null;

            while ((isNotEmpty(existingLine = exisingR.readLine())) && isNotEmpty(modifiedLine = latestR.readLine())) {
                System.out.println(i);
                if (!StringUtils.equals(existingLine, modifiedLine)) {
                    System.out.println(existingLine);
                    System.out.println(modifiedLine);
                    throw new RuntimeException("line not equal " + i);
                }
                i++;
            }
            System.out.println("complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

