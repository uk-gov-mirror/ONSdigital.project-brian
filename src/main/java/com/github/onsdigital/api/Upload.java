package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.readers.DataSetReaderCSDB;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Created by thomasridd on 16/03/15.
 */

@Api
public class Upload {

    /**
     * API Access to the site
     *
     *
     * @param request
     * List of data series required. Filter options
     *
     * @param response
     * Initially will return json only
     *
     * @throws java.io.IOException
     *
     */
    @POST
    public Object get(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {

        try {
            Root.master = DataSetReaderCSDB.readDirectory("/imports/csdb");
            response.getWriter().println("Success");
            response.setStatus(HttpStatus.OK_200);
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        response.setStatus(HttpStatus.NOT_FOUND_404);
        return null;
    }

}
