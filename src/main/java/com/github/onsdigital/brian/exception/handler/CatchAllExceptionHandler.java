package com.github.onsdigital.brian.exception.handler;

import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

public class CatchAllExceptionHandler implements ExceptionHandler<Exception> {

    @Override
    public void handle(Exception e, Request request, Response response) {
        response.type("application/json");
        response.status(500);
        response.body("internal server error");
        error().exception(e).endHTTP(response.raw()).log("request complete");
    }
}
