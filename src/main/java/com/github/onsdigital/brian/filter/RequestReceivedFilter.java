package com.github.onsdigital.brian.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * A Filter to pass requests through before invoking the target {@link spark.Route#handle(Request, Response)} method.
 * Populates the request ID if its does not exist, Captures the time the request was received (used later in metrics
 * logging) and logs other useful debugging information.
 */
public class RequestReceivedFilter implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        info().beginHTTP(request.raw()).log("http request received");
    }
}
