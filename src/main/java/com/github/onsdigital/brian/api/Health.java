package com.github.onsdigital.brian.api;

import com.github.davidcarboni.restolino.framework.Api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;

import org.eclipse.jetty.http.HttpStatus;

@Api
public class Health {

    @GET
    public void get(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.OK_200);
    }
}
