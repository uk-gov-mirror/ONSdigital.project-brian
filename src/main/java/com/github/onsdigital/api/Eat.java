package com.github.onsdigital.api;

import com.github.onsdigital.readers.Csv;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * Created by Tom.Ridd on 09/03/15.
 *
 * Provides methods to
 */

public class Eat {

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
    public boolean eatThis(HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        boolean result = false;

        // Convert and merge the change set:
        // 1. Read in the CSV
        // 2.

        // Change the status code if necessary:
        if (!result) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
        }

        return result;
    }
}
