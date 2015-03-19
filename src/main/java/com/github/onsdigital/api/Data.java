package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.writers.DataSetWriterJSON;
import com.github.onsdigital.writers.SeriesWriterJSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Objects;
import java.util.SortedMap;


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
        if(requestPath.segments().size() == 1) { // RETURN ALL DATA
            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            response.getWriter().println("Temporarily not available pending Future");
            //response.getWriter().println(DataSetWriterJSON.dataSetAsJSON(Root.master, true));
            response.setStatus(HttpStatus.OK_200);

        } else if (returnSeries(requestPath, response)) {

            response.setStatus(HttpStatus.OK_200);
            return null;
        } else if (returnRandom(requestPath, response)) {
            response.setStatus(HttpStatus.OK_200);
            return null;
        }

        response.setStatus(HttpStatus.NOT_FOUND_404);
        return null;
    }

    private boolean returnSeries(Path requestPath, HttpServletResponse response) {
        TimeSeries series = Root.getTimeSeries(requestPath.segments().get(1));
        if (series == null) { return false; }

        response.setCharacterEncoding("UTF8");
        response.setContentType("application/json");
        try {
            response.getWriter().println(SeriesWriterJSON.seriesAsSortedJSON(series, true));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean returnRandom(Path requestPath, HttpServletResponse response) {
        String isRand = requestPath.segments().get(1).toUpperCase();
        if(StringUtils.startsWith(isRand,"RAND")) {
            long seed = Long.parseLong(StringUtils.substring(isRand, 4));
            TimeSeries series = Sample.randomWalk(seed, 100, 1, 1997, 2014, true, true, true);

            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");
            try {
                response.getWriter().println(SeriesWriterJSON.seriesAsSortedJSON(series, true));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private Object getSeriesList() {
        return null;
    }

    private Object getSeries() {
        return null;
    }
}