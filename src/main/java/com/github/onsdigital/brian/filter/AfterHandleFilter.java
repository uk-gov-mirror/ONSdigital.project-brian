package com.github.onsdigital.brian.filter;

import org.slf4j.MDC;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.onsdigital.brian.filter.FilterKeys.REQ_COMPLETE_MSG;
import static com.github.onsdigital.brian.filter.FilterKeys.REQ_METHOD_KEY;
import static com.github.onsdigital.brian.filter.FilterKeys.REQ_RECEIVED_KEY;
import static com.github.onsdigital.brian.filter.FilterKeys.RESP_TIME_KEY;
import static com.github.onsdigital.brian.filter.FilterKeys.STATUS_KEY;
import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * A filter to be applied after a {@link spark.Route#handle(Request, Response)} method is called. Captures and
 * logs request duration metrics, and other useful debugging information.
 */
public class AfterHandleFilter implements Filter, QuietFilter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        handleQuietly(request, response);
    }

    public void handleQuietly(Request request, Response response) {
        LocalDateTime start = LocalDateTime.parse(MDC.get(REQ_RECEIVED_KEY));
        LocalDateTime end = LocalDateTime.now();
        Duration requestTime = Duration.between(start, end);

        logEvent().uri(request.uri())
                .parameter(REQ_METHOD_KEY, request.requestMethod())
                .parameter(STATUS_KEY, response.status())
                .parameter(RESP_TIME_KEY, requestTime.toMillis())
                .info(REQ_COMPLETE_MSG);
    }
}
