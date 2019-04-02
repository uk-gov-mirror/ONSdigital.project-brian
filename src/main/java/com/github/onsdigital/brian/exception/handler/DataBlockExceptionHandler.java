package com.github.onsdigital.brian.exception.handler;

import com.github.onsdigital.brian.filter.QuietFilter;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.github.onsdigital.brian.readers.csdb.DataBlockException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.http.HttpStatus;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Exception handler for {@link DataBlockException} creates a JSON response using the exception message, sets the
 * response status as 400 and send passes the request & response through an 'after handle filter' to capture & log
 * response details before returning to the caller.
 */
public class DataBlockExceptionHandler implements ExceptionHandler<DataBlockException> {

    private QuietFilter postHandleFilter;
    private Gson g;

    /**
     * Construct a new handler instance.
     *
     * @param postHandleFilter The {@link QuietFilter} to apply before returning to the caller.
     */
    public DataBlockExceptionHandler(QuietFilter postHandleFilter) {
        this.postHandleFilter = postHandleFilter;
        this.g = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void handle(DataBlockException e, Request req, Response resp) {
        error().exception(e).log("error while attempting to parse CSDB file");
        resp.type("application/json");
        resp.status(HttpStatus.BAD_REQUEST_400);
        resp.body(g.toJson(new Message(e.getMessage())));
        postHandleFilter.handleQuietly(req, resp);
    }
}
