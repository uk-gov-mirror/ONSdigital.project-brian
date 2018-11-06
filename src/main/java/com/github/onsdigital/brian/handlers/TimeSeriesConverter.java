package com.github.onsdigital.brian.handlers;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.davidcarboni.cryptolite.Keys;
import com.github.davidcarboni.encryptedfileupload.EncryptedFileItemFactory;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.publishers.TimeSeriesPublisher;
import com.github.onsdigital.brian.readers.DataSetReader;
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

import static com.github.onsdigital.brian.logging.Logger.logEvent;

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
     * Respond to a Service endpoint using a specific DataSetReader
     *
     * @param request  a MultiPartRequest to the Service endpoint with a file upload
     * @param response the response (to which we write a List<TimeSeries> object)
     * @param reader   a reader used to run the conversion
     * @throws IOException
     * @throws FileUploadException
     */
/*    public List<TimeSeries> convert(HttpServletRequest request,
                                    HttpServletResponse response,
                                    DataSetReader reader) throws IOException, FileUploadException {
        // Get the input file
        SecretKey key = Keys.newSecretKey();
        Path dataFile = getFileFromMultipartRequest(request, key);

        if (dataFile != null) {
            List<TimeSeries> convertedSeries = getTimeSeries(reader, key, dataFile);
            return convertedSeries;
        }
        logEvent().info("failed to generate timeseries data as data file null");
        response.setStatus(HttpStatus.BAD_REQUEST_400);
        return null;
    }*/

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
