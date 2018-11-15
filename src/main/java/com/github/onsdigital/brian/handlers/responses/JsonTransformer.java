package com.github.onsdigital.brian.handlers.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

import static com.github.onsdigital.brian.configuration.Config.getConfig;

/**
 * Implementation of the Spark {@link JsonTransformer}. Used by the Spark framework to convert response objects to JSON.
 */
public class JsonTransformer implements ResponseTransformer {

    private static final JsonTransformer instance = new JsonTransformer();

    private Gson gson;

    /**
     * Singleton instance - use {@link #getInstance()} to obtain it.
     */
    private JsonTransformer() {
        GsonBuilder builder = new GsonBuilder();

        if (getConfig().isPrettyJSONFormat()) {
            builder.setPrettyPrinting();
        }

        this.gson = builder.create();
    }

    @Override
    public String render(Object o) throws Exception {
        return gson.toJson(o);
    }

    /**
     * @return a singleton instance of the Transformer.
     */
    public static JsonTransformer getInstance() {
        return instance;
    }
}
