package com.github.onsdigital.brian.filter;

import org.slf4j.MDC;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * A filter to be applied after a {@link spark.Route#handle(Request, Response)} method is called. Captures and
 * logs request duration metrics, and other useful debugging information.
 */
public class AfterHandleFilter implements Filter, QuietFilter {

    private static final String REQ_RECEIVED_KEY = "requestRecieved";

    @Override
    public void handle(Request request, Response response) throws Exception {
        handleQuietly(request, response);
    }

    public void handleQuietly(Request request, Response response) {
        LocalDateTime start = LocalDateTime.parse(MDC.get(REQ_RECEIVED_KEY));
        LocalDateTime end = LocalDateTime.now();
        Duration requestTime = Duration.between(start, end);

        logEvent().parameter("requestMethod", request.requestMethod())
                .parameter("uri", request.uri())
                .parameter("responseStatus", response.status())
                .parameter("responseTimeMillis", requestTime.toMillis())
                .info("completed request details");
    }
}
