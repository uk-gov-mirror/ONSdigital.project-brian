package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.helpers.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;



@Api
public class Data {
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
    @GET
    public Object get(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {

        Path requestPath = Path.newInstance(request);

        return requestPath.lastSegment();
    }

    private Object getSeriesList() {
        return null;
    }

    private Object getSeries() {
        return null;
    }
}