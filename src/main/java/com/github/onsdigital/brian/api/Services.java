package com.github.onsdigital.brian.api;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.davidcarboni.cryptolite.Keys;
import com.github.davidcarboni.encryptedfileupload.EncryptedFileItemFactory;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.content.util.ContentUtil;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.publishers.TimeSeriesPublisher;
import com.github.onsdigital.brian.readers.DataSetReaderCSDB;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomasridd on 08/06/15.
 */
@Api
public class Services {
    @POST
    public void postToServices(HttpServletRequest request,
                      HttpServletResponse response) throws IOException, FileUploadException {

        String[] segments = request.getPathInfo().split("/");

        if (segments.length > 2 && segments[2].equalsIgnoreCase("ConvertCSDB")) {
            //
            convertCSDB(request, response);
        }
    }

    private void convertCSDB(HttpServletRequest request,
                             HttpServletResponse response) throws IOException, FileUploadException {

        // Get the input file
        SecretKey key = Keys.newSecretKey();
        Path csdbFile = getFileFromMultipartRequest(request, key);

        if (csdbFile != null) {
            // Convert it to dataSet
            TimeSeriesDataSet timeSeriesDataSet = DataSetReaderCSDB.readFile(csdbFile, key);

            // Write to response
            List<TimeSeriesObject> brianSeries = new ArrayList<TimeSeriesObject>(timeSeriesDataSet.timeSeries.values());

            List<TimeSeries> contentSeries = new ArrayList<>();
            for (TimeSeriesObject series: brianSeries) {
                contentSeries.add(TimeSeriesPublisher.convertToContentLibraryTimeSeries(series));
            }

            String dataSetJson = ContentUtil.serialise(contentSeries);
            try(InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = response.getOutputStream()) {
                IOUtils.copy(inputStream, output);
            }

            if (System.getenv("exportLog") != null) {
                Path logPath = Paths.get(System.getenv("exportLog")).resolve("convertCSDB.json");
                try (InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = FileUtils.openOutputStream(logPath.toFile())) {
                    IOUtils.copy(inputStream, output);
                }
            }
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
        }
    }

    private Path getFileFromMultipartRequest(HttpServletRequest request, SecretKey key) throws IOException, FileUploadException {
        if(!ServletFileUpload.isMultipartContent(request)) {return null;}

        // Create a factory for disk-based file items
        EncryptedFileItemFactory factory = new EncryptedFileItemFactory();

        // Configure a repository (to ensure a secure temp location is used)
        File repository = Files.createTempDirectory("csdb").toFile();
        factory.setRepository(repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Read the request to a temporary file and return
        List<FileItem> items = upload.parseRequest(request);
        for(FileItem item: items) {
            if (!item.isFormField()) {
                Path tempFile = Files.createTempFile("csdb",".csdb");
                try(OutputStream stream = new Crypto().encrypt(Files.newOutputStream(tempFile), key)) {
                    IOUtils.copy(item.getInputStream(), stream);
                }
                return tempFile;
            }
        }
        return null;
    }
}
