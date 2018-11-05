package com.github.onsdigital.brian.filter;

import org.slf4j.MDC;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class AfterHandleFilter implements Filter, QuietFilter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        handleQuietly(request, response);
    }

    public void handleQuietly(Request request, Response response) {
        LocalDateTime start = LocalDateTime.parse(MDC.get("requestRecieved"));
        LocalDateTime end = LocalDateTime.now();
        Duration requestTime = Duration.between(start, end);

        logEvent().parameter("requestMethod", request.requestMethod())
                .parameter("uri", request.uri())
                .parameter("responseStatus", response.status())
                .parameter("responseTimeMillis", requestTime.toMillis())
                .info("completed request details");
    }
}
