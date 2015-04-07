package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.helpers.Path;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.generators.Sample;
import com.github.onsdigital.writers.DataCubeWriterJSON;
import com.github.onsdigital.writers.DataSetWriterJSON;
import com.github.onsdigital.writers.SeriesWriterJSON;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


@Api
public class Cubes {
    /**
     * API Access to cross dimensional data sets
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
                      HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {

        Path requestPath = Path.newInstance(request);
        String cubeName = "";

        // Top level lists all cubes
        System.out.println(requestPath);
        if(requestPath.segments().size() == 1) {
            response.setCharacterEncoding("UTF8");
            response.setContentType("application/json");

            response.getWriter().println(DataCubeWriterJSON.dataCubeSetAsJSON(Root.cubeMaster));
            response.setStatus(HttpStatus.OK_200);
        }

        // Second level lists details of a single cube
        else if (requestPath.segments().size() == 2) {
            cubeName = requestPath.segments().get(1);
            if(Root.cubeMaster.cubes.containsKey(cubeName)) {
                response.setCharacterEncoding("UTF8");
                response.setContentType("application/json");
                response.getWriter().println(DataCubeWriterJSON.dataCubeSummaryAsJSON(Root.cubeMaster.cubes.get(cubeName).get()));
            }
            return null;
        }

        // Third level lists
        else if (requestPath.segments().size() == 3) {
            cubeName = requestPath.segments().get(1);
            if(Root.cubeMaster.cubes.containsKey(cubeName)) {
                String requestType = requestPath.segments().get(2);
                if(requestType.equalsIgnoreCase("data")) {
                    response.setCharacterEncoding("UTF8");
                    response.setContentType("application/json");

                    response.getWriter().println(DataCubeWriterJSON.dataCubeAsJSON(Root.cubeMaster.cubes.get(cubeName).get()));
                }
            }
            return null;
        }

        response.setStatus(HttpStatus.NOT_FOUND_404);
        return null;
    }
}