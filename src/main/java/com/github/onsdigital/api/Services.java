package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.readers.DataSetReaderCSDB;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path csdbFile = getFileFromMultipartRequest(request);

        if (csdbFile != null) {
            // Convert it to dataSet
            DataSet dataSet = DataSetReaderCSDB.readFile(csdbFile);

            // Write to response
            List<TimeSeries> series = new ArrayList<TimeSeries>(dataSet.timeSeries.values());
            System.out.println(series);
            String dataSetJson = Serialiser.serialise(series);
            try(InputStream inputStream = IOUtils.toInputStream(dataSetJson); OutputStream output = response.getOutputStream()) {
                IOUtils.copy(inputStream, output);
            }

        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
        }
    }

    private Path getFileFromMultipartRequest(HttpServletRequest request) throws IOException, FileUploadException {
        if(!ServletFileUpload.isMultipartContent(request)) {return null;}

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

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
                try(OutputStream stream = Files.newOutputStream(tempFile)) {
                    IOUtils.copy(item.getInputStream(), stream);
                }
                return tempFile;
            }
        }
        return null;
    }
}
