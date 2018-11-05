package com.github.onsdigital.brian.filter;

import spark.Request;
import spark.Response;

@FunctionalInterface
public interface QuietFilter {

    void handleQuietly(Request request, Response response);
}
