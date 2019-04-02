package com.github.onsdigital.brian.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * A filter to be applied after a {@link spark.Route#handle(Request, Response)} method is called. Captures and
 * logs request duration metrics, and other useful debugging information.
 */
public class RequestCompleteFilter implements Filter, QuietFilter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        info().endHTTP(response.raw()).log("http request complete");
    }

    @Override
    public void handleQuietly(Request request, Response response) {
        info().endHTTP(response.raw()).log("http request complete");
    }
}
