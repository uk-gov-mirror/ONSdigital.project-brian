package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.readers.DataSetReaderCSDB;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by thomasridd on 16/03/15.
 */
public class Upload {

    /** POST
     *
     *
     * @param request
     * JSON update manifest
     *
     * @param response
     * result
     *
     * @throws java.io.IOException
     */
    @POST
    public boolean runUpload(HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        boolean result = false;

        // Convert and merge the change set:
        // 1. Read in the CSV
        // 2.

        try {
            Root.master = DataSetReaderCSDB.readDirectory("/imports/csdb");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Change the status code if necessary:
        if (!result) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
        }

        return result;
    }
}
