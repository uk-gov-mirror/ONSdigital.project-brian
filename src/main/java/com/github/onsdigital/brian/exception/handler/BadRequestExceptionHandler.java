package com.github.onsdigital.brian.exception.handler;

import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.filter.QuietFilter;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpStatus;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class BadRequestExceptionHandler implements ExceptionHandler<BadRequestException> {

    private QuietFilter postHandleFilter;

    public BadRequestExceptionHandler(QuietFilter postHandleFilter) {
        this.postHandleFilter = postHandleFilter;
    }

    @Override
    public void handle(BadRequestException e, Request req, Response resp) {
        logEvent(e).error(e.getMessage());
        resp.body(new Gson().toJson(new Message(e.getMessage())));
        resp.type("application/json");
        resp.status(HttpStatus.BAD_REQUEST_400);

        postHandleFilter.handleQuietly(req, resp);
    }
}
