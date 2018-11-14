package com.github.onsdigital.brian.exception.handler;

import com.github.onsdigital.brian.filter.QuietFilter;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

public class InternalServerErrorHandler implements Route {

    static final Message INTERNAL_SERVER_ERROR = new Message("internal server error yo");

    private QuietFilter quietFilter;
    private Gson g;

    public InternalServerErrorHandler(QuietFilter quietFilter) {
        this.quietFilter = quietFilter;
        this.g = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        response.status(500);
        quietFilter.handleQuietly(request, response);
        return g.toJson(INTERNAL_SERVER_ERROR);
    }
}
