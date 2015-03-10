package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.helpers.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom.Ridd on 09/03/15.
 */
public class Convert {

    /**
     * Methods that convert structured csv to json or vice versa
     * <p/>
     * Partly necessary as helpers but also
     * <p/>
     * PATH /Convert/ {From format} / {To format}
     *
     * @param request  File to convert
     * @param response Converted file
     * @throws java.io.IOException
     */
    @GET
    public Object get(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {


        Path requestPath = Path.newInstance(request);
        List<String> segments = new ArrayList<>(requestPath.segments());

        return null;
    }

}
