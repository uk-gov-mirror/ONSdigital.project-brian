package com.github.onsdigital.brian.exception.handler;

import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.filter.QuietFilter;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.http.HttpStatus;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * Exception handler for {@link BadRequestException} creates a JSON response using the exception message, sets the
 * response status as 400 and send passes the request & response through a post handle filter to capture & log
 * response details before returning to the caller.
 */
public class BadRequestExceptionHandler implements ExceptionHandler<BadRequestException> {

    private QuietFilter afterHandleFilter;
    private Gson g;

    /**
     * Construct a new handler.
     *
     * @param afterHandleFilter the filter to pass the request/response through before returning to the caller.
     */
    public BadRequestExceptionHandler(QuietFilter afterHandleFilter) {
        this.afterHandleFilter = afterHandleFilter;
        this.g = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void handle(BadRequestException e, Request req, Response resp) {
        logEvent(e).error(e.getMessage());
        resp.body(g.toJson(new Message(e.getMessage())));
        resp.type("application/json");
        resp.status(HttpStatus.BAD_REQUEST_400);

        afterHandleFilter.handleQuietly(req, resp);
    }
}
