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

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class DataBlockExceptionHandler implements ExceptionHandler<DataBlockException> {

    private QuietFilter postHandleFilter;
    private Gson g;

    public DataBlockExceptionHandler(QuietFilter postHandleFilter) {
        this.postHandleFilter = postHandleFilter;
        this.g = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void handle(DataBlockException e, Request req, Response resp) {
        logEvent(e).error("error while attempting to parse CSDB file");
        resp.type("application/json");
        resp.status(HttpStatus.BAD_REQUEST_400);
        resp.body(g.toJson(new Message(e.getMessage())));
        postHandleFilter.handleQuietly(req, resp);
    }
}
