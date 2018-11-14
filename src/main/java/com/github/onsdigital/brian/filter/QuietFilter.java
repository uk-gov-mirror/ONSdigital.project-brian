package com.github.onsdigital.brian.filter;

import spark.Request;
import spark.Response;

/**
 * Exactly the same as a {@link spark.Filter} except that its method signature does not declare a checked exception.
 */
@FunctionalInterface
public interface QuietFilter {

    /**
     * Apply the filter logic
     */
    void handleQuietly(Request request, Response response);
}
