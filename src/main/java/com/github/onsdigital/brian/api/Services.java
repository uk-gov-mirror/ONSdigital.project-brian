package com.github.onsdigital.brian.api;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.davidcarboni.cryptolite.Keys;
import com.github.davidcarboni.encryptedfileupload.EncryptedFileItemFactory;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.publishers.TimeSeriesPublisher;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.brian.readers.csdb.DataSetReaderCSDB;
import com.github.onsdigital.brian.readers.DataSetReaderCSV;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomasridd on 08/06/15.
 */
//@Api
public class Services {

    //  @POST
    public List<TimeSeries> postToServices(HttpServletRequest request,
                                           HttpServletResponse response) throws IOException, FileUploadException {

        String[] segments = request.getPathInfo().split("/");

        if (segments.length > 2 && segments[2].equalsIgnoreCase("ConvertCSDB")) {

            // Convert with CSDB Reader
            return convert(request, response, new DataSetReaderCSDB());
        } else if (segments.length > 2 && segments[2].equalsIgnoreCase("ConvertCSV")) {

            // Convert with CSV Reader
            return convert(request, response, new DataSetReaderCSV());
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            return null;
        }
    }


    /**
     * Respond to a Service endpoint using a specific DataSetReader
     *
     * @param request  a MultiPartRequest to the Service endpoint with a file upload
     * @param response the response (to which we write a List<TimeSeries> object)
     * @param reader   a reader used to run the conversion
     * @throws IOException
     * @throws FileUploadException
     */
    private List<TimeSeries> convert(HttpServletRequest request,
                                     HttpServletResponse response,
                                     DataSetReader reader) throws IOException, FileUploadException {
        // Get the input file
        SecretKey key = Keys.newSecretKey();
        Path dataFile = getFileFromMultipartRequest(request, key);

        if (dataFile != null) {

            List<TimeSeries> convertedSeries = getTimeSeries(reader, key, dataFile);

//            String dataSetJson = ContentUtil.serialise(convertedSeries);
//            try(InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = response.getOutputStream()) {
//                IOUtils.copy(inputStream, output);
//            }
            return convertedSeries;


        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            return null;
        }
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

//
//    private void convertCSDB(HttpServletRequest request,
//                             HttpServletResponse response) throws IOException, FileUploadException {
//
//        // Get the input file
//        SecretKey key = Keys.newSecretKey();
//        Path csdbFile = getFileFromMultipartRequest(request, key);
//
//        if (csdbFile != null) {
//            // Convert it to dataSet
//            List<TimeSeries> contentSeries = getTimeSeries(new DataSetReaderCSDB(), key, csdbFile);
//
//            String dataSetJson = ContentUtil.serialise(contentSeries);
//            try(InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = response.getOutputStream()) {
//                IOUtils.copy(inputStream, output);
//            }
//
////            if (System.getenv("exportLog") != null) {
////                Path logPath = Paths.get(System.getenv("exportLog")).resolve("convertCSDB.json");
////                try (InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = FileUtils.openOutputStream(logPath.toFile())) {
////                    IOUtils.copy(inputStream, output);
////                }
////            }
//        } else {
//            response.setStatus(HttpStatus.BAD_REQUEST_400);
//        }
//    }
//
//    private void convertCSV(HttpServletRequest request,
//                             HttpServletResponse response) throws IOException, FileUploadException {
//// Get the input file
//        SecretKey key = Keys.newSecretKey();
//        Path csdbFile = getFileFromMultipartRequest(request, key);
//
//        if (csdbFile != null) {
//            // Convert it to dataSet
//            List<TimeSeries> contentSeries = getTimeSeries(new DataSetReaderCSV(), key, csdbFile);
//
//            String dataSetJson = ContentUtil.serialise(contentSeries);
//            try(InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = response.getOutputStream()) {
//                IOUtils.copy(inputStream, output);
//            }
//
//        } else {
//            response.setStatus(HttpStatus.BAD_REQUEST_400);
//        }
//    }

    private Path getFileFromMultipartRequest(HttpServletRequest request, SecretKey key) throws IOException, FileUploadException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }

        // Create a factory for disk-based file items
        EncryptedFileItemFactory factory = new EncryptedFileItemFactory();

        // Configure a repository (to ensure a secure temp location is used)
        File repository = Files.createTempDirectory("csdb").toFile();
        factory.setRepository(repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Read the request to a temporary file and return
        List<FileItem> items = upload.parseRequest(request);
        for (FileItem item : items) {
            if (!item.isFormField()) {
                Path tempFile = Files.createTempFile("csdb", ".csdb");
                try (OutputStream stream = new Crypto().encrypt(Files.newOutputStream(tempFile), key)) {
                    IOUtils.copy(item.getInputStream(), stream);
                }
                return tempFile;
            }
        }
        return null;
    }
}
