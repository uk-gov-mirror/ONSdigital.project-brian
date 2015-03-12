package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.writers.DataSetWriterJSON;
import com.github.onsdigital.writers.SeriesWriterJSON;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

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
        System.out.println(requestPath);
        if(requestPath.segments().size() == 1) {
            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.getWriter().println(DataSetWriterJSON.dataSetAsJSON(Root.master, true));
            response.setStatus(HttpStatus.OK_200);

        } else {
            TimeSeries series = Root.master.timeSeries.get(requestPath.lastSegment());

            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.getWriter().println(SeriesWriterJSON.seriesAsJSON(series, true));
            response.setStatus(HttpStatus.OK_200);
        }
        response.setStatus(HttpStatus.NOT_FOUND_404);
        return null;
    }

    private Object getSeriesList() {
        return null;
    }

    private Object getSeries() {
        return null;
    }
}